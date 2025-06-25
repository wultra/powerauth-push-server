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
import io.getlime.push.repository.AppCredentialsRepository;
import io.getlime.push.repository.model.AppCredentialsEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for managing push devices.
 *
 * @author Jan Pesek, jan.pesek@wultra.com
 */
@Service
@AllArgsConstructor
@Slf4j
public class PushDeviceService {

    private final DeviceRegistrationService deviceRegistrationService;
    private final AppCredentialsRepository appCredentialsRepository;
    private final PushServiceConfiguration config;

    public Response createDevice(final CreateDeviceRequest request) throws PushServerException {
        if (request == null) {
            throw new PushServerException("Request object must not be empty");
        }
        logger.info("action: createDevice, state: initiated, appId: {}, activationId: {}, token: {}, platform: {}, environment: {}, activationStatus: {}, userId: {}", request.getAppId(),
                request.getActivationId(), maskPushToken(request.getToken()), request.getPlatform(), request.getEnvironment(), request.getActivationStatus(), request.getUserId());

        final String errorMessage = CreateDeviceRequestValidator.validate(request);
        if (errorMessage != null) {
            throw new PushServerException(errorMessage);
        }

        final AppCredentialsEntity appCredentials = findAppCredentials(request.getAppId());

        deviceRegistrationService.createOrUpdateDevice(request, appCredentials);
        logger.info("action: createDevice, state: succeeded, appId: {}, activationId: {}, platform: {}, environment: {}", request.getAppId(), request.getActivationId(), request.getPlatform(), request.getEnvironment());
        return new Response();
    }

    public Response createDeviceMultipleActivations(final CreateDeviceForActivationsRequest request) throws PushServerException {
        if (!config.isRegistrationOfMultipleActivationsEnabled()) {
            throw new PushServerException("Registration of multiple associated activations per device is not enabled.");
        }

        if (request == null) {
            throw new PushServerException("Request object must not be empty");
        }
        logger.info("action: createDeviceMultipleActivations, state: initiated, appId: {}, activationIds: {}, token: {}, platform: {}, environment: {}",
                request.getAppId(), request.getActivationIds(), maskPushToken(request.getToken()), request.getPlatform(), request.getEnvironment());

        final String errorMessage = CreateDeviceRequestValidator.validate(request);
        if (errorMessage != null) {
            throw new PushServerException(errorMessage);
        }

        final AppCredentialsEntity appCredentials = findAppCredentials(request.getAppId());

        deviceRegistrationService.createOrUpdateDevices(request, appCredentials);
        logger.info("action: createDeviceMultipleActivations, state: succeeded, appId: {}, activationIds: {}, platform: {}, environment: {}", request.getAppId(), request.getActivationIds(), request.getPlatform(), request.getEnvironment());
        return new Response();
    }

    public Response updateDeviceStatus(final UpdateDeviceStatusRequest request) throws PushServerException {
        if (request == null) {
            throw new PushServerException("Request object must not be empty");
        }
        logger.info("action: updateDeviceStatus, state: initiated, activationId: {}", request.getActivationId());

        final String errorMessage = UpdateDeviceStatusRequestValidator.validate(request);
        if (errorMessage != null) {
            throw new PushServerException(errorMessage);
        }

        deviceRegistrationService.updateStatus(request);
        logger.info("action: updateDeviceStatus, state: succeeded, activationId: {}", request.getActivationId());
        return new Response();
    }

    public Response deleteDevice(final DeleteDeviceRequest request) throws PushServerException {
        if (request == null) {
            throw new PushServerException("Request object must not be empty");
        }
        logger.info("action: deleteDevice, state: initiated, appId: {}, token: {}", request.getAppId(), maskPushToken(request.getToken()));

        final String errorMessage = DeleteDeviceRequestValidator.validate(request);
        if (errorMessage != null) {
            throw new PushServerException(errorMessage);
        }

        deviceRegistrationService.delete(request.getAppId(), request.getToken());
        logger.info("action: deleteDevice, state: succeeded, appId: {}", request.getAppId());
        return new Response();
    }

    private AppCredentialsEntity findAppCredentials(String powerAuthAppId) throws PushServerException {
        return appCredentialsRepository.findFirstByAppId(powerAuthAppId).orElseThrow(() ->
                new PushServerException("Application with given ID does not exist"));
    }

    /**
     * Mask push token String to avoid revealing the full token in logs.
     * @param token Push token.
     * @return Masked push token.
     */
    private static String maskPushToken(final String token) {
        if (token == null || token.length() < 6) {
            return token;
        }
        return token.substring(0, 6) + "...";
    }

}
