/*
 * Copyright 2024 Wultra s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wultra.push.service;

import com.wultra.security.powerauth.client.v4.PowerAuthClient;
import com.wultra.security.powerauth.client.model.enumeration.ActivationStatus;
import com.wultra.security.powerauth.client.model.error.PowerAuthClientException;
import com.wultra.security.powerauth.client.model.response.v4.GetActivationStatusResponse;
import com.wultra.push.errorhandling.exceptions.PushServerException;
import com.wultra.push.model.enumeration.ApnsEnvironment;
import com.wultra.push.model.enumeration.MobilePlatform;
import com.wultra.push.model.request.CreateDeviceForActivationsRequest;
import com.wultra.push.model.request.CreateDeviceRequest;
import com.wultra.push.model.request.UpdateDeviceStatusRequest;
import com.wultra.push.repository.PushDeviceRepository;
import com.wultra.push.repository.model.AppCredentialsEntity;
import com.wultra.push.repository.model.Platform;
import com.wultra.push.repository.model.PushDeviceRegistrationEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Service managing registration and persistence of push devices.
 *
 * @author Jan Pesek, jan.pesek@wultra.com
 */
@Component
@AllArgsConstructor
@Slf4j
public class DeviceRegistrationService {

    private final PushDeviceRepository pushDeviceRepository;
    private final PowerAuthClient powerAuthClient;

    @Retryable(retryFor = DataIntegrityViolationException.class,
            maxAttemptsExpression = "${powerauth.push.service.registration.retry.maxAttempts:2}",
            backoff = @Backoff(delayExpression = "${powerauth.push.service.registration.retry.backoff:100}"))
    @Transactional
    public void createOrUpdateDevice(final CreateDeviceRequest requestObject, final AppCredentialsEntity appCredentials) throws PushServerException {
        final String appId = requestObject.getAppId();
        final String pushToken = requestObject.getToken();
        final MobilePlatform platform = requestObject.getPlatform();
        final ApnsEnvironment environment = requestObject.getEnvironment();
        final String activationId = requestObject.getActivationId();

        final List<PushDeviceRegistrationEntity> devices = lookupDeviceRegistrations(appId, activationId, pushToken);
        final PushDeviceRegistrationEntity device;
        if (devices.isEmpty()) {
            // The device registration is new, create a new entity.
            logger.info("Creating new device registration: app ID: {}, activation ID: {}, platform: {}", requestObject.getAppId(), requestObject.getActivationId(), platform);
            device = initDeviceRegistrationEntity(appCredentials, pushToken);
        } else if (devices.size() == 1) {
            // An existing row was found by one of the lookup methods, update this row. This means that either:
            // 1. A row with same activation ID and push token is updated, in this case only the last registration timestamp changes.
            // 2. A row with same activation ID but different push token is updated. A new push token has been issued by Google or Apple for an activation.
            // 3. A row with same push token but different activation ID is updated. The user removed an activation and created a new one, the push token remains the same.
            logger.info("Updating existing device registration: app ID: {}, activation ID: {}, platform: {}", requestObject.getAppId(), requestObject.getActivationId(), platform);
            device = devices.get(0);
            updateDeviceRegistrationEntity(device, appCredentials, pushToken);
        } else {
            // Multiple existing rows have been found. This can only occur during lookup by push token.
            // Push token can be associated with multiple activations only when associated activations are enabled.
            // Push device registration must be done using /push/device/create/multi endpoint in this case.
            logger.info("Multiple device registrations found: app ID: {}, activation ID: {}, platform: {}", requestObject.getAppId(), requestObject.getActivationId(), platform);
            throw new PushServerException("Multiple device registrations found for push token. Use the /push/device/create/multi endpoint for this scenario.");
        }
        device.setTimestampLastRegistered(new Date());
        device.setPlatform(convert(platform));
        device.setEnvironment(environment != null ? environment.getKey() : null);

        if (requestObject.getActivationStatus() == null || requestObject.getUserId() == null) {
            logger.debug("Request does not contain details about activation");
            fetchAndUpdateActivationForDevice(device, activationId);
        } else {
            logger.debug("Request contains details about activation");
            final ActivationDetail activationDetail = ActivationDetail.builder()
                    .activationId(activationId)
                    .activationStatus(requestObject.getActivationStatus())
                    .userId(requestObject.getUserId())
                    .build();
            assignActivationDetails(device, activationDetail);
        }

        pushDeviceRepository.save(device);
    }

    @Transactional
    public void createOrUpdateDevices(final CreateDeviceForActivationsRequest request, final AppCredentialsEntity appCredentials) throws PushServerException {
        final String appId = request.getAppId();
        final String pushToken = request.getToken();
        final MobilePlatform platform = request.getPlatform();
        final ApnsEnvironment environment = request.getEnvironment();
        final List<String> activationIds = request.getActivationIds();

        // Initialize loop variables.
        final AtomicBoolean registrationFailed = new AtomicBoolean(false);
        final Set<Long> usedDeviceRegistrationIds = new HashSet<>();

        activationIds.stream().distinct().forEach(activationId -> {
            try {
                final List<PushDeviceRegistrationEntity> devices = lookupDeviceRegistrations(appId, activationId, pushToken);
                PushDeviceRegistrationEntity device;
                if (devices.isEmpty()) {
                    // The device registration is new, create a new entity.
                    device = initDeviceRegistrationEntity(appCredentials, pushToken);
                } else if (devices.size() == 1) {
                    device = devices.get(0);
                    if (usedDeviceRegistrationIds.contains(device.getId())) {
                        // The row has already been used within this request. Create a new row instead.
                        device = initDeviceRegistrationEntity(appCredentials, pushToken);
                    } else {
                        // Update existing row.
                        updateDeviceRegistrationEntity(device, appCredentials, pushToken);
                    }
                } else {
                    // Multiple existing rows have been found. This can only occur during lookup by push token.
                    // It is not clear how original rows should be mapped to new rows because they were not looked up
                    // using an activation ID. Delete existing rows (unless they were already used in this request)
                    // and create a new row.
                    devices.stream().filter(existingDevice -> !usedDeviceRegistrationIds.contains(existingDevice.getId())).forEach(pushDeviceRepository::delete);
                    device = initDeviceRegistrationEntity(appCredentials, pushToken);
                }
                device.setTimestampLastRegistered(new Date());
                device.setPlatform(convert(platform));
                device.setEnvironment(environment != null ? environment.getKey() : null);
                fetchAndUpdateActivationForDevice(device, activationId);
                PushDeviceRegistrationEntity registeredDevice = pushDeviceRepository.save(device);
                usedDeviceRegistrationIds.add(registeredDevice.getId());
            } catch (PushServerException ex) {
                logger.error(ex.getMessage(), ex);
                registrationFailed.set(true);
            }
        });

        if (registrationFailed.get()) {
            throw new PushServerException("Device registration failed");
        }
    }

    @Transactional
    public void updateStatus(final UpdateDeviceStatusRequest request) throws PushServerException {
        final String activationId = request.getActivationId();

        final List<PushDeviceRegistrationEntity> device = pushDeviceRepository.findByActivationId(activationId);

        final ActivationStatus activationStatus = request.getActivationStatus() == null ? fetchActivationStatus(activationId) : request.getActivationStatus();

        for (PushDeviceRegistrationEntity registration : device) {
            registration.setActive(activationStatus == ActivationStatus.ACTIVE);
            pushDeviceRepository.save(registration);
        }
    }

    @Transactional
    public void delete(final String appId, final String pushToken) {
        final List<PushDeviceRegistrationEntity> devices = pushDeviceRepository.findByAppCredentialsAppIdAndPushToken(appId, pushToken);
        pushDeviceRepository.deleteAll(devices);
    }

    /**
     * Lookup device registrations using app ID, activation ID and push token.
     * <br/>
     * The query priorities are ranging from most exact to least exact match:
     * <ul>
     *     <li>Lookup by activation ID and push token.</li>
     *     <li>Lookup by activation ID.</li>
     *     <li>Lookup by application ID and push token.</li>
     * </ul>
     * @param appId Application ID.
     * @param activationId Activation ID.
     * @param pushToken Push token.
     * @return List of found device registration entities.
     */
    private List<PushDeviceRegistrationEntity> lookupDeviceRegistrations(String appId, String activationId, String pushToken) throws PushServerException {
        // At first, lookup the device registrations by match on activationId and pushToken.
        final List<PushDeviceRegistrationEntity> deviceRegistrationsByActivationIdAndToken = pushDeviceRepository.findByActivationIdAndPushToken(activationId, pushToken);
        if (!deviceRegistrationsByActivationIdAndToken.isEmpty()) {
            if (deviceRegistrationsByActivationIdAndToken.size() != 1) {
                throw new PushServerException("Multiple device registrations found during lookup by activation ID and push token. Please delete duplicate rows and make sure database indexes have been applied on push_device_registration table.");
            }
            return deviceRegistrationsByActivationIdAndToken;
        }

        // Second, lookup the device registrations by match on activationId.
        final List<PushDeviceRegistrationEntity> deviceRegistrationsByActivationId = pushDeviceRepository.findByActivationId(activationId);
        if (!deviceRegistrationsByActivationId.isEmpty()) {
            if (deviceRegistrationsByActivationId.size() != 1) {
                throw new PushServerException("Multiple device registrations found during lookup by activation ID. Please delete duplicate rows and make sure database indexes have been applied on push_device_registration table.");
            }
            return deviceRegistrationsByActivationId;
        }

        // Third, lookup the device registration by match on appId and pushToken. Multiple results can be returned in this case, this is a multi-activation scenario.
        // The final result is definitive, either device registrations were found by push token or none were found at all.
        return pushDeviceRepository.findByAppCredentialsAppIdAndPushToken(appId, pushToken);
    }

    /**
     * Initialize a new device registration entity for given app ID and push token.
     * @param app AppCredentialsEntity instance.
     * @param pushToken Push token.
     * @return New device registration entity.
     */
    private static PushDeviceRegistrationEntity initDeviceRegistrationEntity(AppCredentialsEntity app, String pushToken) {
        final PushDeviceRegistrationEntity device = new PushDeviceRegistrationEntity();
        device.setAppCredentials(app);
        device.setPushToken(pushToken);
        return device;
    }

    /**
     * Update a device registration entity with given app ID and push token.
     * @param app AppCredentialsEntity instance.
     * @param pushToken Push token.
     */
    private static void updateDeviceRegistrationEntity(PushDeviceRegistrationEntity device, AppCredentialsEntity app, String pushToken) {
        device.setAppCredentials(app);
        device.setPushToken(pushToken);
    }

    /**
     * Fetch and update activation details associated with the given device.
     *
     * @param device Push device registration entity.
     * @param activationId Activation ID.
     * @throws PushServerException Thrown in case communication with PowerAuth server fails,
     * or there is no such activation, or the matching activation is in REMOVED state.
     */
    private void fetchAndUpdateActivationForDevice(PushDeviceRegistrationEntity device, String activationId) throws PushServerException {
        try {
            final GetActivationStatusResponse activation = powerAuthClient.getActivationStatusWithoutBlob(activationId);
            if (activation == null) {
                throw new PushServerException("Device registration failed because associated activation was not found");
            }

            final ActivationDetail activationDetail = ActivationDetail.builder()
                    .activationId(activation.getActivationId())
                    .activationStatus(activation.getActivationStatus())
                    .userId(activation.getUserId())
                    .build();
            assignActivationDetails(device, activationDetail);
        } catch (PowerAuthClientException ex) {
            logger.warn(ex.getMessage(), ex);
            throw new PushServerException("Device registration failed because activation status is unknown");
        }
    }

    /**
     * Assign activation details associated with the given device.
     * {@link PowerAuthClientException} is thrown in case the activation is in REMOVED state.
     *
     * @param device Push device registration entity.
     * @param activationDetail Details about the activation.
     * @throws PushServerException In case the activation is in REMOVED state.
     */
    private static void assignActivationDetails(PushDeviceRegistrationEntity device, ActivationDetail activationDetail) throws PushServerException {
        if (activationDetail.activationStatus() == ActivationStatus.REMOVED) {
            throw new PushServerException("Device registration failed because associated activation is REMOVED");
        }

        device.setActivationId(activationDetail.activationId());
        device.setActive(activationDetail.activationStatus() == ActivationStatus.ACTIVE);
        device.setUserId(activationDetail.userId());
    }

    private ActivationStatus fetchActivationStatus(final String activationId) throws PushServerException {
        try {
            return powerAuthClient.getActivationStatusWithoutBlob(activationId).getActivationStatus();
        } catch (PowerAuthClientException ex) {
            logger.warn(ex.getMessage(), ex);
            throw new PushServerException("Update device status failed because activation status is unknown");
        }
    }

    private static Platform convert(final MobilePlatform source) {
        return switch (source) {
            case IOS -> Platform.IOS;
            case ANDROID -> Platform.ANDROID;
            case HUAWEI -> Platform.HUAWEI;
            case APNS -> Platform.APNS;
            case FCM -> Platform.FCM;
            case HMS -> Platform.HMS;
        };
    }

    @Builder
    record ActivationDetail(String activationId, String userId, ActivationStatus activationStatus) {}

}
