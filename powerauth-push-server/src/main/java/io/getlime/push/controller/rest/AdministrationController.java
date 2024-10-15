/*
 * Copyright 2018 Wultra s.r.o.
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

import com.wultra.security.powerauth.client.model.error.PowerAuthClientException;
import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.core.rest.model.base.response.Response;
import io.getlime.push.errorhandling.exceptions.PushServerException;
import io.getlime.push.model.entity.PushServerApplication;
import io.getlime.push.model.enumeration.ApnsEnvironment;
import io.getlime.push.model.request.*;
import io.getlime.push.model.response.CreateApplicationResponse;
import io.getlime.push.model.response.GetApplicationDetailResponse;
import io.getlime.push.model.response.GetApplicationListResponse;
import io.getlime.push.model.validator.*;
import io.getlime.push.repository.model.AppCredentialsEntity;
import io.getlime.push.service.AdministrationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for administering the push server.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping(value = "admin/app")
public class AdministrationController {

    private final AdministrationService administrationService;

    /**
     * List applications configured in Push Server.
     * @return Application list response.
     */
    @GetMapping(value = "list")
    public ObjectResponse<GetApplicationListResponse> listApplications() {
        logger.debug("Received listApplications request");
        final List<PushServerApplication> applications = administrationService.findAllApplications();
        final GetApplicationListResponse response = new GetApplicationListResponse();
        response.setApplicationList(applications);
        logger.debug("The listApplications request succeeded");
        return new ObjectResponse<>(response);
    }

    /**
     * List applications which are not yet configured in Push Server.
     * @return Application list response.
     * @throws PushServerException Throw in case communication with PowerAuth server fails.
     */
    @GetMapping(value = "unconfigured/list")
    public ObjectResponse<GetApplicationListResponse> listUnconfiguredApplications() throws PushServerException {
        try {
            logger.debug("Received listUnconfiguredApplications request");
            final List<PushServerApplication> applications = administrationService.findUnconfiguredApplications();
            final GetApplicationListResponse response = new GetApplicationListResponse();
            response.setApplicationList(applications);
            logger.debug("The listUnconfiguredApplications request succeeded");
            return new ObjectResponse<>(response);
        } catch (PowerAuthClientException ex) {
            logger.warn(ex.getMessage(), ex);
            throw new PushServerException("Unconfigured application list failed because application name could not be retrieved");
        }
    }

    /**
     * Get application credentials entity details.
     * @param request Application detail request.
     * @return Application detail response.
     * @throws PushServerException Thrown when application credentials entity could not be found or request validation fails.
     */
    @PostMapping(value = "detail")
    public ObjectResponse<GetApplicationDetailResponse> getApplicationDetail(@RequestBody ObjectRequest<GetApplicationDetailRequest> request) throws PushServerException {
        logger.debug("Received getApplicationDetail request");
        final GetApplicationDetailRequest requestObject = request.getRequestObject();
        final String errorMessage = GetApplicationDetailRequestValidator.validate(requestObject);
        if (errorMessage != null) {
            throw new PushServerException(errorMessage);
        }
        final GetApplicationDetailResponse response = new GetApplicationDetailResponse();
        final AppCredentialsEntity appCredentialsEntity = administrationService.findAppCredentials(requestObject.getAppId());
        final PushServerApplication app = new PushServerApplication();
        app.setAppId(appCredentialsEntity.getAppId());
        app.setIos(appCredentialsEntity.getIosPrivateKey() != null);
        app.setAndroid(appCredentialsEntity.getAndroidPrivateKey() != null);
        app.setHuawei(isHuawei(appCredentialsEntity));
        response.setApplication(app);
        if (requestObject.isIncludeIos()) {
            response.setIosBundle(appCredentialsEntity.getIosBundle());
            response.setIosKeyId(appCredentialsEntity.getIosKeyId());
            response.setIosTeamId(appCredentialsEntity.getIosTeamId());
            response.setIosEnvironment(ApnsEnvironment.fromString(appCredentialsEntity.getIosEnvironment()));
        }
        if (requestObject.isIncludeAndroid()) {
            response.setAndroidProjectId(appCredentialsEntity.getAndroidProjectId());
        }
        if (requestObject.isIncludeHuawei()) {
            response.setHuaweiProjectId(appCredentialsEntity.getHmsProjectId());
        }
        logger.debug("The getApplicationDetail request succeeded");
        return new ObjectResponse<>(response);
    }

    private static boolean isHuawei(final AppCredentialsEntity appCredentialsEntity) {
        return appCredentialsEntity.getHmsClientSecret() != null && appCredentialsEntity.getHmsClientId() != null;
    }

    /**
     * Create application.
     * @param request Create application request.
     * @return Create application response.
     * @throws PushServerException Thrown when request validation fails.
     */
    @PostMapping(value = "create")
    public ObjectResponse<CreateApplicationResponse> createApplication(@RequestBody ObjectRequest<CreateApplicationRequest> request) throws PushServerException {
        final CreateApplicationRequest requestObject = request.getRequestObject();
        if (requestObject == null) {
            throw new PushServerException("Request object must not be empty");
        }
        logger.info("Received createApplication request, application ID: {}", requestObject.getAppId());
        final AppCredentialsEntity appCredentials = administrationService.createAppCredentials(requestObject);
        final CreateApplicationResponse response = new CreateApplicationResponse(appCredentials.getAppId());
        logger.info("The createApplication request succeeded, application ID: {}", requestObject.getAppId());
        return new ObjectResponse<>(response);
    }

    /**
     * Update iOS configuration.
     * @param request Update iOS configuration request.
     * @return Response.
     * @throws PushServerException Thrown when application credentials entity could not be found or request validation fails.
     */
    @RequestMapping(value = "ios/update", method = { RequestMethod.POST, RequestMethod.PUT })
    public Response updateIos(@RequestBody ObjectRequest<UpdateIosRequest> request) throws PushServerException {
        final UpdateIosRequest requestObject = request.getRequestObject();
        if (requestObject == null) {
            throw new PushServerException("Request object must not be empty");
        }
        logger.info("Received updateIos request, application ID: {}", requestObject.getAppId());
        final String errorMessage = UpdateIosRequestValidator.validate(requestObject);
        if (errorMessage != null) {
            throw new PushServerException(errorMessage);
        }
        administrationService.updateIosAppCredentials(requestObject);
        return new Response();
    }

    /**
     * Remove iOS configuration.
     * @param request Remove iOS configuration request.
     * @return Response.
     * @throws PushServerException Thrown when application credentials entity could not be found or request validation fails.
     */
    @RequestMapping(value = "ios/remove", method = { RequestMethod.POST, RequestMethod.DELETE })
    public Response removeIos(@RequestBody ObjectRequest<RemoveIosRequest> request) throws PushServerException {
        final RemoveIosRequest requestObject = request.getRequestObject();
        if (requestObject == null) {
            throw new PushServerException("Request object must not be empty");
        }
        logger.info("Received removeIos request, application ID: {}", requestObject.getAppId());
        String errorMessage = RemoveIosRequestValidator.validate(requestObject);
        if (errorMessage != null) {
            throw new PushServerException(errorMessage);
        }
        administrationService.removeIosAppCredentials(requestObject.getAppId());
        return new Response();
    }

    /**
     * Update Android configuration.
     * @param request Update Android configuration request.
     * @return Response.
     * @throws PushServerException Thrown when application credentials entity could not be found or request validation fails.
     */
    @RequestMapping(value = "android/update", method = { RequestMethod.POST, RequestMethod.PUT })
    public Response updateAndroid(@RequestBody ObjectRequest<UpdateAndroidRequest> request) throws PushServerException {
        final UpdateAndroidRequest requestObject = request.getRequestObject();
        if (requestObject == null) {
            throw new PushServerException("Request object must not be empty");
        }
        logger.info("Received updateAndroid request, application ID: {}", requestObject.getAppId());
        String errorMessage = UpdateAndroidRequestValidator.validate(requestObject);
        if (errorMessage != null) {
            throw new PushServerException(errorMessage);
        }
        administrationService.updateAndroidAppCredentials(requestObject);
        return new Response();
    }

    /**
     * Remove Android configuration.
     * @param request Remove Android configuration request.
     * @return Response.
     * @throws PushServerException Thrown when application credentials entity could not be found or request validation fails.
     */
    @RequestMapping(value = "android/remove", method = { RequestMethod.POST, RequestMethod.DELETE })
    public Response removeAndroid(@RequestBody ObjectRequest<RemoveAndroidRequest> request) throws PushServerException {
        final RemoveAndroidRequest requestObject = request.getRequestObject();
        if (requestObject == null) {
            throw new PushServerException("Request object must not be empty");
        }
        logger.info("Received removeAndroid request, application ID: {}", requestObject.getAppId());
        String errorMessage = RemoveAndroidRequestValidator.validate(requestObject);
        if (errorMessage != null) {
            throw new PushServerException(errorMessage);
        }
        administrationService.removeAndroidAppCredentials(requestObject.getAppId());
        return new Response();
    }

    /**
     * Update Huawei configuration.
     *
     * @param request Update Huawei configuration request.
     * @return Response.
     * @throws PushServerException Thrown when application credentials entity could not be found or request validation fails.
     */
    @RequestMapping(value = "huawei/update", method = { RequestMethod.POST, RequestMethod.PUT })
    public Response updateHuawei(@Valid @RequestBody ObjectRequest<UpdateHuaweiRequest> request) throws PushServerException {
        final UpdateHuaweiRequest requestObject = request.getRequestObject();
        logger.info("Received update Huawei request, application ID: {}", requestObject.getAppId());
        administrationService.updateHuaweiAppCredentials(requestObject);
        return new Response();
    }

    /**
     * Remove Huawei configuration.
     *
     * @param request Remove Huawei configuration request.
     * @return Response.
     * @throws PushServerException Thrown when application credentials entity could not be found or request validation fails.
     */
    @RequestMapping(value = "huawei/remove", method = { RequestMethod.POST, RequestMethod.DELETE })
    public Response removeHuawei(@Valid @RequestBody ObjectRequest<RemoveHuaweiRequest> request) throws PushServerException {
        final RemoveHuaweiRequest requestObject = request.getRequestObject();
        logger.info("Received remove Huawei request, application ID: {}", requestObject.getAppId());
        administrationService.removeHuaweiAppCredentials(requestObject.getAppId());
        return new Response();
    }
}
