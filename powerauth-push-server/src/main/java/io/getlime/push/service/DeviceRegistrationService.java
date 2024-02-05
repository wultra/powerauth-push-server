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

package io.getlime.push.service;

import com.wultra.security.powerauth.client.PowerAuthClient;
import com.wultra.security.powerauth.client.model.enumeration.ActivationStatus;
import com.wultra.security.powerauth.client.model.error.PowerAuthClientException;
import com.wultra.security.powerauth.client.model.response.GetActivationStatusResponse;
import io.getlime.push.errorhandling.exceptions.PushServerException;
import io.getlime.push.model.enumeration.MobilePlatform;
import io.getlime.push.model.request.CreateDeviceForActivationsRequest;
import io.getlime.push.model.request.CreateDeviceRequest;
import io.getlime.push.model.request.UpdateDeviceStatusRequest;
import io.getlime.push.repository.PushDeviceRepository;
import io.getlime.push.repository.model.AppCredentialsEntity;
import io.getlime.push.repository.model.Platform;
import io.getlime.push.repository.model.PushDeviceRegistrationEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

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
    public void createOrUpdateDevice(final CreateDeviceRequest requestObject, final AppCredentialsEntity appCredentials) throws PushServerException {
        final String appId = requestObject.getAppId();
        final String pushToken = requestObject.getToken();
        final MobilePlatform platform = requestObject.getPlatform();
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
        updateActivationForDevice(device, activationId);
        pushDeviceRepository.save(device);
    }

    public void createOrUpdateDevices(final CreateDeviceForActivationsRequest request, final AppCredentialsEntity appCredentials) throws PushServerException {
        final String appId = request.getAppId();
        final String pushToken = request.getToken();
        final MobilePlatform platform = request.getPlatform();
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
                updateActivationForDevice(device, activationId);
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

    public void updateStatus(final UpdateDeviceStatusRequest request) throws PushServerException {
        final String activationId = request.getActivationId();

        final List<PushDeviceRegistrationEntity> device = pushDeviceRepository.findByActivationId(activationId);

        final ActivationStatus activationStatus = request.getActivationStatus() == null ? fetchActivationStatus(activationId) : request.getActivationStatus();

        for (PushDeviceRegistrationEntity registration : device) {
            registration.setActive(activationStatus == ActivationStatus.ACTIVE);
            pushDeviceRepository.save(registration);
        }
    }

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
    private PushDeviceRegistrationEntity initDeviceRegistrationEntity(AppCredentialsEntity app, String pushToken) {
        PushDeviceRegistrationEntity device = new PushDeviceRegistrationEntity();
        device.setAppCredentials(app);
        device.setPushToken(pushToken);
        return device;
    }

    /**
     * Update a device registration entity with given app ID and push token.
     * @param app AppCredentialsEntity instance.
     * @param pushToken Push token.
     */
    private void updateDeviceRegistrationEntity(PushDeviceRegistrationEntity device, AppCredentialsEntity app, String pushToken) {
        device.setAppCredentials(app);
        device.setPushToken(pushToken);
    }

    /**
     * Update activation for given device in case activation exists in PowerAuth server and it is not in REMOVED state.
     * Otherwise fail the device registration because registration could not be associated with an activation.
     * @param device Push device registration entity.
     * @param activationId Activation ID.
     * @throws PushServerException Throw in case communication with PowerAuth server fails.
     */
    private void updateActivationForDevice(PushDeviceRegistrationEntity device, String activationId) throws PushServerException {
        try {
            final GetActivationStatusResponse activation = powerAuthClient.getActivationStatus(activationId);
            if (activation != null && !ActivationStatus.REMOVED.equals(activation.getActivationStatus())) {
                device.setActivationId(activationId);
                device.setActive(activation.getActivationStatus().equals(ActivationStatus.ACTIVE));
                device.setUserId(activation.getUserId());
                return;
            }
            throw new PushServerException("Device registration failed because associated activation is not ACTIVE");
        } catch (PowerAuthClientException ex) {
            logger.warn(ex.getMessage(), ex);
            throw new PushServerException("Device registration failed because activation status is unknown");
        }
    }

    private ActivationStatus fetchActivationStatus(final String activationId) throws PushServerException {
        try {
            return powerAuthClient.getActivationStatus(activationId).getActivationStatus();
        } catch (PowerAuthClientException ex) {
            logger.warn(ex.getMessage(), ex);
            throw new PushServerException("Update device status failed because activation status is unknown");
        }
    }

    private static Platform convert(final MobilePlatform source) {
        return switch (source) {
            case IOS -> Platform.IOS;
            case ANDROID -> Platform.ANDROID;
        };
    }

}
