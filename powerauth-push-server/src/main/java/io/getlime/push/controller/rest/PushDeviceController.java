/*
 * Copyright 2016 Wultra s.r.o.
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
package io.getlime.push.controller.rest;

import com.wultra.security.powerauth.client.PowerAuthClient;
import com.wultra.security.powerauth.client.model.error.PowerAuthClientException;
import com.wultra.security.powerauth.client.v3.ActivationStatus;
import com.wultra.security.powerauth.client.v3.GetActivationStatusResponse;
import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.Response;
import io.getlime.push.configuration.PushServiceConfiguration;
import io.getlime.push.errorhandling.exceptions.PushServerException;
import io.getlime.push.model.request.CreateDeviceForActivationsRequest;
import io.getlime.push.model.request.CreateDeviceRequest;
import io.getlime.push.model.request.DeleteDeviceRequest;
import io.getlime.push.model.request.UpdateDeviceStatusRequest;
import io.getlime.push.model.validator.CreateDeviceRequestValidator;
import io.getlime.push.model.validator.DeleteDeviceRequestValidator;
import io.getlime.push.model.validator.UpdateDeviceStatusRequestValidator;
import io.getlime.push.repository.PushDeviceRepository;
import io.getlime.push.repository.model.PushDeviceRegistrationEntity;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Controller responsible for device registration related business processes.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@RestController
@RequestMapping(value = "push/device")
public class PushDeviceController {

    private static final Logger logger = LoggerFactory.getLogger(PushDeviceController.class);

    private final PushDeviceRepository pushDeviceRepository;
    private final PowerAuthClient client;
    private final PushServiceConfiguration config;

    /**
     * Constructor with autowired dependencies.
     * @param pushDeviceRepository Push device repository.
     * @param client PowerAuth service client.
     * @param config Push service configuration.
     */
    @Autowired
    public PushDeviceController(PushDeviceRepository pushDeviceRepository, PowerAuthClient client, PushServiceConfiguration config) {
        this.pushDeviceRepository = pushDeviceRepository;
        this.client = client;
        this.config = config;
    }

    /**
     * Create a new device registration.
     * @param request Device registration request.
     * @return Device registration status.
     * @throws PushServerException In case request object is invalid.
     */
    @PostMapping(value = "create")
    @Operation(summary = "Create a device",
                  description = "Create a new device push token (platform specific). The call must include an activation ID, so that the token is associated with given user." +
                          "Request body should contain application ID, device token, device's platform and an activation ID. " +
                          "If such device already exist, date on last registration is updated and also platform might be changed\n" +
                          "\n---" +
                          "Note: Since this endpoint is usually called by the back-end service, it is not secured in any way. " +
                          "It's the service that calls this endpoint responsibility to assure that the device is somehow authenticated before the push token is assigned with given activation ID," +
                          " so that there are no incorrect bindings.")
    public Response createDevice(@RequestBody ObjectRequest<CreateDeviceRequest> request) throws PushServerException {
        CreateDeviceRequest requestObject = request.getRequestObject();
        if (requestObject == null) {
            throw new PushServerException("Request object must not be empty");
        }
        logger.info("Received createDevice request, app ID: {}, activation ID: {}, token: {}, platform: {}", requestObject.getAppId(),
                requestObject.getActivationId(), maskPushToken(requestObject.getToken()), requestObject.getPlatform());
        String errorMessage = CreateDeviceRequestValidator.validate(requestObject);
        if (errorMessage != null) {
            throw new PushServerException(errorMessage);
        }
        Long appId = requestObject.getAppId();
        String pushToken = requestObject.getToken();
        String platform = requestObject.getPlatform();
        String activationId = requestObject.getActivationId();
        List<PushDeviceRegistrationEntity> devices = lookupDeviceRegistrations(appId, activationId, pushToken);
        PushDeviceRegistrationEntity device;
        if (devices.isEmpty()) {
            // The device registration is new, create a new entity.
            device = initDeviceRegistrationEntity(appId, pushToken);
        } else if (devices.size() == 1) {
            // An existing row was found by one of the lookup methods, update this row. This means that either:
            // 1. A row with same activation ID and push token is updated, in this case only the last registration timestamp changes.
            // 2. A row with same activation ID but different push token is updated. A new push token has been issued by Google or Apple for an activation.
            // 3. A row with same push token but different activation ID is updated. The user removed an activation and created a new one, the push token remains the same.
            device = devices.get(0);
            updateDeviceRegistrationEntity(device, appId, pushToken);
        } else {
            // Multiple existing rows have been found. This can only occur during lookup by push token.
            // Push token can be associated with multiple activations only when associated activations are enabled.
            // Push device registration must be done using /push/device/create/multi endpoint in this case.
            throw new PushServerException("Multiple device registrations found for push token. Use the /push/device/create/multi endpoint for this scenario.");
        }
        device.setTimestampLastRegistered(new Date());
        device.setPlatform(platform);
        updateActivationForDevice(device, activationId);
        pushDeviceRepository.save(device);
        logger.info("The createDevice request succeeded, app ID: {}, activation ID: {}, platform: {}", requestObject.getAppId(), requestObject.getActivationId(), requestObject.getPlatform());
        return new Response();
    }

    /**
     * Create a new device registration for multiple associated activations.
     * @param request Device registration request.
     * @return Device registration status.
     * @throws PushServerException In case request object is invalid.
     */
    @PostMapping(value = "create/multi")
    @Operation(summary = "Create a device for multiple associated activations",
            description = "Create a new device push token (platform specific). The call must include one or more activation IDs." +
                    "Request body should contain application ID, device token, device's platform and list of activation IDs. " +
                    "If such device already exist, date on last registration is updated and also platform might be changed\n" +
                    "\n---" +
                    "Note: Since this endpoint is usually called by the back-end service, it is not secured in any way. " +
                    "It's the service that calls this endpoint responsibility to assure that the device is somehow authenticated before the push token is assigned with given activation IDs," +
                    " so that there are no incorrect bindings.")
    public Response createDeviceMultipleActivations(@RequestBody ObjectRequest<CreateDeviceForActivationsRequest> request) throws PushServerException {
        CreateDeviceForActivationsRequest requestedObject = request.getRequestObject();
        if (requestedObject == null) {
            throw new PushServerException("Request object must not be empty");
        }
        logger.info("Received createDeviceMultipleActivations request, app ID: {}, activation IDs: {}, token: {}, platform: {}",
                requestedObject.getAppId(), requestedObject.getActivationIds(), maskPushToken(requestedObject.getToken()), requestedObject.getPlatform());
        String errorMessage;
        if (!config.isRegistrationOfMultipleActivationsEnabled()) {
            errorMessage = "Registration of multiple associated activations per device is not enabled.";
        } else {
            errorMessage = CreateDeviceRequestValidator.validate(requestedObject);
        }
        if (errorMessage != null) {
            throw new PushServerException(errorMessage);
        }
        Long appId = requestedObject.getAppId();
        String pushToken = requestedObject.getToken();
        String platform = requestedObject.getPlatform();
        List<String> activationIds = requestedObject.getActivationIds();

        // Initialize loop variables.
        AtomicBoolean registrationFailed = new AtomicBoolean(false);
        Set<Long> usedDeviceRegistrationIds = new HashSet<>();

        activationIds.forEach(activationId -> {
            try {
                List<PushDeviceRegistrationEntity> devices = lookupDeviceRegistrations(appId, activationId, pushToken);
                PushDeviceRegistrationEntity device;
                if (devices.isEmpty()) {
                    // The device registration is new, create a new entity.
                    device = initDeviceRegistrationEntity(appId, pushToken);
                } else if (devices.size() == 1) {
                    device = devices.get(0);
                    if (usedDeviceRegistrationIds.contains(device.getId())) {
                        // The row has already been used within this request. Create a new row instead.
                        device = initDeviceRegistrationEntity(appId, pushToken);
                    } else {
                        // Update existing row.
                        updateDeviceRegistrationEntity(device, appId, pushToken);
                    }
                } else {
                    // Multiple existing rows have been found. This can only occur during lookup by push token.
                    // It is not clear how original rows should be mapped to new rows because they were not looked up
                    // using an activation ID. Delete existing rows (unless they were already used in this request)
                    // and create a new row.
                    devices.stream().filter(existingDevice -> !usedDeviceRegistrationIds.contains(existingDevice.getId())).forEach(pushDeviceRepository::delete);
                    device = initDeviceRegistrationEntity(appId, pushToken);
                }
                device.setTimestampLastRegistered(new Date());
                device.setPlatform(platform);
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
        logger.info("The createDeviceMultipleActivations request succeeded, app ID: {}, activation IDs: {}, platform: {}", requestedObject.getAppId(), requestedObject.getActivationIds(), requestedObject.getPlatform());
        return new Response();
    }

    /**
     * Initialize a new device registration entity for given app ID and push token.
     * @param appId App ID.
     * @param pushToken Push token.
     * @return New device registration entity.
     */
    private PushDeviceRegistrationEntity initDeviceRegistrationEntity(Long appId, String pushToken) {
        PushDeviceRegistrationEntity device = new PushDeviceRegistrationEntity();
        device.setAppId(appId);
        device.setPushToken(pushToken);
        return device;
    }

    /**
     * Update a device registration entity with given app ID and push token.
     * @param appId App ID.
     * @param pushToken Push token.
     */
    private void updateDeviceRegistrationEntity(PushDeviceRegistrationEntity device, Long appId, String pushToken) {
        device.setAppId(appId);
        device.setPushToken(pushToken);
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
    private List<PushDeviceRegistrationEntity> lookupDeviceRegistrations(Long appId, String activationId, String pushToken) throws PushServerException {
        List<PushDeviceRegistrationEntity> deviceRegistrations;
        // At first, lookup the device registrations by match on activationId and pushToken.
        deviceRegistrations = pushDeviceRepository.findByActivationIdAndPushToken(activationId, pushToken);
        if (!deviceRegistrations.isEmpty()) {
            if (deviceRegistrations.size() != 1) {
                throw new PushServerException("Multiple device registrations found during lookup by activation ID and push token. Please delete duplicate rows and make sure database indexes have been applied on push_device_registration table.");
            }
            return deviceRegistrations;
        }
        // Second, lookup the device registrations by match on activationId.
        deviceRegistrations = pushDeviceRepository.findByActivationId(activationId);
        if (!deviceRegistrations.isEmpty()) {
            if (deviceRegistrations.size() != 1) {
                throw new PushServerException("Multiple device registrations found during lookup by activation ID. Please delete duplicate rows and make sure database indexes have been applied on push_device_registration table.");
            }
            return deviceRegistrations;
        }
        // Third, lookup the device registration by match on appId and pushToken. Multiple results can be returned in this case, this is a multi-activation scenario.
        deviceRegistrations = pushDeviceRepository.findByAppIdAndPushToken(appId, pushToken);
        // The final result is definitive, either device registrations were found by push token or none were found at all.
        return deviceRegistrations;
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
            final GetActivationStatusResponse activation = client.getActivationStatus(activationId);
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

    /**
     * Update activation status for given device registration.
     * @param request Status update request.
     * @return Status update response.
     * @throws PushServerException In case request object is invalid.
     */
    @RequestMapping(value = "status/update", method = {RequestMethod.POST, RequestMethod.PUT})
    @Operation(summary = "Update device status",
                  description = "Update the status of given device registration based on the associated activation ID. " +
                          "This can help assure that registration is in non-active state and cannot receive personal messages.")
    public Response updateDeviceStatus(@RequestBody UpdateDeviceStatusRequest request) throws PushServerException {
        try {
            if (request == null) {
                throw new PushServerException("Request object must not be empty");
            }
            logger.info("Received updateDeviceStatus request, activation ID: {}", request.getActivationId());
            String errorMessage = UpdateDeviceStatusRequestValidator.validate(request);
            if (errorMessage != null) {
                throw new PushServerException(errorMessage);
            }
            String activationId = request.getActivationId();
            ActivationStatus activationStatus = request.getActivationStatus();
            List<PushDeviceRegistrationEntity> device = pushDeviceRepository.findByActivationId(activationId);
            if (device != null)  {
                if (activationStatus == null) {
                    // Activation status was not received via callback data, retrieve it from PowerAuth server
                    activationStatus = client.getActivationStatus(activationId).getActivationStatus();
                }
                for (PushDeviceRegistrationEntity registration: device) {
                    registration.setActive(activationStatus.equals(ActivationStatus.ACTIVE));
                    pushDeviceRepository.save(registration);
                }
            }
            logger.info("The updateDeviceStatus request succeeded, activation ID: {}", request.getActivationId());
            return new Response();
        } catch (PowerAuthClientException ex) {
            logger.warn(ex.getMessage(), ex);
            throw new PushServerException("Update device status failed because activation status is unknown");
        }
    }

    /**
     * Remove device registration with given push token.
     * @param request Remove registration request.
     * @return Removal status response.
     * @throws PushServerException In case request object is invalid.
     */
    @RequestMapping(value = "delete", method = {RequestMethod.POST, RequestMethod.DELETE})
    @Operation(summary = "Delete a device",
                  description = "Remove device identified by application ID and device token. " +
                          "If device identifiers don't match, nothing happens")
    public Response deleteDevice(@RequestBody ObjectRequest<DeleteDeviceRequest> request) throws PushServerException {
        DeleteDeviceRequest requestObject = request.getRequestObject();
        if (requestObject == null) {
            throw new PushServerException("Request object must not be empty");
        }
        logger.info("Received deleteDevice request, app ID: {}, token: {}", requestObject.getAppId(), maskPushToken(requestObject.getToken()));
        String errorMessage = DeleteDeviceRequestValidator.validate(requestObject);
        if (errorMessage != null) {
            throw new PushServerException(errorMessage);
        }
        Long appId = requestObject.getAppId();
        String pushToken = requestObject.getToken();
        List<PushDeviceRegistrationEntity> devices = pushDeviceRepository.findByAppIdAndPushToken(appId, pushToken);
        devices.forEach(pushDeviceRepository::delete);
        logger.info("The deleteDevice request succeeded, app ID: {}", requestObject.getAppId());
        return new Response();
    }

    /**
     * Mask push token String to avoid revealing the full token in logs.
     * @param token Push token.
     * @return Masked push token.
     */
    private String maskPushToken(String token) {
        if (token == null || token.length() < 6) {
            return token;
        }
        return token.substring(0, 6) + "...";
    }
}