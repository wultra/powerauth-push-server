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
package com.wultra.push.controller.rest;

import com.wultra.security.powerauth.client.model.error.PowerAuthClientException;
import com.wultra.core.rest.model.base.request.ObjectRequest;
import com.wultra.core.rest.model.base.response.ObjectResponse;
import com.wultra.core.rest.model.base.response.Response;
import com.wultra.push.errorhandling.exceptions.PushServerException;
import com.wultra.push.model.entity.PushServerApplication;
import com.wultra.push.model.enumeration.ApnsEnvironment;
import com.wultra.push.model.request.*;
import com.wultra.push.model.response.CreateApplicationResponse;
import com.wultra.push.model.response.GetApplicationDetailResponse;
import com.wultra.push.model.response.GetApplicationListResponse;
import com.wultra.push.model.validator.*;
import com.wultra.push.repository.model.AppCredentialsEntity;
import com.wultra.push.service.AdministrationService;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "List all applications", description = "List all application registered in Push Server")
    public ObjectResponse<GetApplicationListResponse> listApplications() {
        logger.debug("action: listApplications, state: initiated");
        final List<PushServerApplication> applications = administrationService.findAllApplications();
        final GetApplicationListResponse response = new GetApplicationListResponse();
        response.setApplicationList(applications);
        logger.debug("action: listApplications, state: succeeded");
        return new ObjectResponse<>(response);
    }

    /**
     * List applications which are not yet configured in Push Server.
     * @return Application list response.
     * @throws PushServerException Throw in case communication with PowerAuth server fails.
     */
    @GetMapping(value = "unconfigured/list")
    @Operation(summary = "List unconfigured applications", description = "List unconfigured application in Push Server")
    public ObjectResponse<GetApplicationListResponse> listUnconfiguredApplications() throws PushServerException {
        try {
            logger.debug("action: listUnconfiguredApplications, state: initiated");
            final List<PushServerApplication> applications = administrationService.findUnconfiguredApplications();
            final GetApplicationListResponse response = new GetApplicationListResponse();
            response.setApplicationList(applications);
            logger.debug("action: listUnconfiguredApplications, state: succeeded");
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
    @Operation(summary = "Get application detail", description = "Obtain registered application detail")
    public ObjectResponse<GetApplicationDetailResponse> getApplicationDetail(@RequestBody ObjectRequest<GetApplicationDetailRequest> request) throws PushServerException {
        logger.debug("action: getApplicationDetail, state: initiated");
        final GetApplicationDetailRequest requestObject = request.getRequestObject();
        final String errorMessage = GetApplicationDetailRequestValidator.validate(requestObject);
        if (errorMessage != null) {
            throw new PushServerException(errorMessage);
        }
        final GetApplicationDetailResponse response = new GetApplicationDetailResponse();
        final AppCredentialsEntity appCredentialsEntity = administrationService.findAppCredentials(requestObject.getAppId());
        final PushServerApplication app = new PushServerApplication();
        app.setAppId(appCredentialsEntity.getAppId());
        app.setApns(appCredentialsEntity.getApnsPrivateKey() != null);
        app.setFcm(appCredentialsEntity.getFcmPrivateKey() != null);
        app.setHms(isHuawei(appCredentialsEntity));
        response.setApplication(app);
        if (requestObject.isIncludeApns()) {
            response.setApnsBundle(appCredentialsEntity.getApnsBundle());
            response.setApnsKeyId(appCredentialsEntity.getApnsKeyId());
            response.setApnsTeamId(appCredentialsEntity.getApnsTeamId());
            response.setApnsEnvironment(ApnsEnvironment.fromString(appCredentialsEntity.getApnsEnvironment()));
        }
        if (requestObject.isIncludeFcm()) {
            response.setFcmProjectId(appCredentialsEntity.getFcmProjectId());
        }
        if (requestObject.isIncludeHms()) {
            response.setHmsProjectId(appCredentialsEntity.getHmsProjectId());
        }
        logger.debug("action: getApplicationDetail, state: succeeded");
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
    @Operation(summary = "Create an application", description = "Register an application in Push server")
    public ObjectResponse<CreateApplicationResponse> createApplication(@RequestBody ObjectRequest<CreateApplicationRequest> request) throws PushServerException {
        final CreateApplicationRequest requestObject = request.getRequestObject();
        if (requestObject == null) {
            throw new PushServerException("Request object must not be empty");
        }
        logger.info("action: createApplication, state: initiated, applicationId: {}", requestObject.getAppId());
        final AppCredentialsEntity appCredentials = administrationService.createAppCredentials(requestObject);
        final CreateApplicationResponse response = new CreateApplicationResponse(appCredentials.getAppId());
        logger.info("action: createApplication, state: succeeded, applicationId: {}", requestObject.getAppId());
        return new ObjectResponse<>(response);
    }

    /**
     * Update iOS configuration.
     * @param request Update iOS configuration request.
     * @return Response.
     * @throws PushServerException Thrown when application credentials entity could not be found or request validation fails.
     */
    @RequestMapping(value = "ios/update", method = { RequestMethod.POST, RequestMethod.PUT })
    @Operation(summary = "Update iOS configuration (deprecated)", description = "Update iOS configuration endpoint (deprecated), use the POST /admin/app/apns endpoint")
    public Response updateIos(@RequestBody ObjectRequest<UpdateIosRequest> request) throws PushServerException {
        final UpdateIosRequest requestObject = request.getRequestObject();
        if (requestObject == null) {
            throw new PushServerException("Request object must not be empty");
        }
        logger.info("action: updateIos, state: initiated, applicationId: {}", requestObject.getAppId());
        final String errorMessage = UpdateIosRequestValidator.validate(requestObject);
        if (errorMessage != null) {
            throw new PushServerException(errorMessage);
        }
        administrationService.updateIosAppCredentials(requestObject);
        logger.info("action: updateIos, state: succeeded, applicationId: {}", requestObject.getAppId());
        return new Response();
    }

    /**
     * Update APNs configuration.
     * @param request Update APNs configuration request.
     * @return Response.
     * @throws PushServerException Thrown when application credentials entity could not be found or request validation fails.
     */
    @RequestMapping(value = "apns", method = { RequestMethod.POST, RequestMethod.PUT })
    @Operation(summary = "Update APNs configuration", description = "Update APNs configuration in Push server")
    public Response updateApns(@Valid @RequestBody ObjectRequest<UpdateApnsRequest> request) throws PushServerException {
        final UpdateApnsRequest requestObject = request.getRequestObject();
        if (requestObject == null) {
            throw new PushServerException("Request object must not be empty");
        }
        logger.info("action: updateApns, state: initiated, applicationId: {}", requestObject.getAppId());
        administrationService.updateApnsAppCredentials(requestObject);
        logger.info("action: updateApns, state: succeeded, applicationId: {}", requestObject.getAppId());
        return new Response();
    }

    /**
     * Remove iOS configuration.
     * @param request Remove iOS configuration request.
     * @return Response.
     * @throws PushServerException Thrown when application credentials entity could not be found or request validation fails.
     */
    @RequestMapping(value = "ios/remove", method = { RequestMethod.POST, RequestMethod.DELETE })
    @Operation(summary = "Remove iOS configuration (deprecated)", description = "Remove iOS configuration endpoint (deprecated), use the DELETE /admin/app/apns endpoint")
    public Response removeIos(@RequestBody ObjectRequest<RemoveIosRequest> request) throws PushServerException {
        final RemoveIosRequest requestObject = request.getRequestObject();
        if (requestObject == null) {
            throw new PushServerException("Request object must not be empty");
        }
        logger.info("action: removeIos, state: initiated, applicationId: {}", requestObject.getAppId());
        String errorMessage = RemoveIosRequestValidator.validate(requestObject);
        if (errorMessage != null) {
            throw new PushServerException(errorMessage);
        }
        administrationService.removeIosAppCredentials(requestObject.getAppId());
        logger.info("action: removeIos, state: succeeded, applicationId: {}", requestObject.getAppId());
        return new Response();
    }

    /**
     * Remove APNs configuration.
     * @param appId Application identifier.
     * @return Response.
     * @throws PushServerException Thrown when application credentials entity could not be found or request validation fails.
     */
    @RequestMapping(value = "apns", method = RequestMethod.DELETE)
    @Operation(summary = "Remove APNs configuration", description = "Remove APNs configuration from Push server")
    public Response removeApns(@RequestParam("appId") String appId) throws PushServerException {
        if (appId == null) {
            throw new PushServerException("Parameter appId must not be empty");
        }
        logger.info("action: removeApns, state: initiated, applicationId: {}", appId);
        administrationService.removeApnsAppCredentials(appId);
        logger.info("action: removeApns, state: succeeded, applicationId: {}", appId);
        return new Response();
    }

    /**
     * Update Android configuration.
     * @param request Update Android configuration request.
     * @return Response.
     * @throws PushServerException Thrown when application credentials entity could not be found or request validation fails.
     */
    @RequestMapping(value = "android/update", method = { RequestMethod.POST, RequestMethod.PUT })
    @Operation(summary = "Update Android configuration (deprecated)", description = "Update Android configuration endpoint (deprecated), use the POST /admin/app/fcm endpoint")
    public Response updateAndroid(@RequestBody ObjectRequest<UpdateAndroidRequest> request) throws PushServerException {
        final UpdateAndroidRequest requestObject = request.getRequestObject();
        if (requestObject == null) {
            throw new PushServerException("Request object must not be empty");
        }
        logger.info("action: updateAndroid, state: initiated, applicationId: {}", requestObject.getAppId());
        String errorMessage = UpdateAndroidRequestValidator.validate(requestObject);
        if (errorMessage != null) {
            throw new PushServerException(errorMessage);
        }
        administrationService.updateAndroidAppCredentials(requestObject);
        logger.info("action: updateAndroid, state: succeeded, applicationId: {}", requestObject.getAppId());
        return new Response();
    }

    /**
     * Update FCM configuration.
     * @param request Update FCM configuration request.
     * @return Response.
     * @throws PushServerException Thrown when application credentials entity could not be found or request validation fails.
     */
    @RequestMapping(value = "fcm", method = { RequestMethod.POST, RequestMethod.PUT })
    @Operation(summary = "Update FCM configuration", description = "Update FCM configuration in Push server")
    public Response updateFcm(@Valid @RequestBody ObjectRequest<UpdateFcmRequest> request) throws PushServerException {
        final UpdateFcmRequest requestObject = request.getRequestObject();
        if (requestObject == null) {
            throw new PushServerException("Request object must not be empty");
        }
        logger.info("action: updateFcm, state: initiated, applicationId: {}", requestObject.getAppId());
        administrationService.updateFcmAppCredentials(requestObject);
        logger.info("action: updateFcm, state: succeeded, applicationId: {}", requestObject.getAppId());
        return new Response();
    }

    /**
     * Remove Android configuration.
     * @param request Remove Android configuration request.
     * @return Response.
     * @throws PushServerException Thrown when application credentials entity could not be found or request validation fails.
     */
    @RequestMapping(value = "android/remove", method = { RequestMethod.POST, RequestMethod.DELETE })
    @Operation(summary = "Remove Android configuration (deprecated)", description = "Remove Android configuration endpoint (deprecated), use the DELETE /admin/app/fcm endpoint")
    public Response removeAndroid(@RequestBody ObjectRequest<RemoveAndroidRequest> request) throws PushServerException {
        final RemoveAndroidRequest requestObject = request.getRequestObject();
        if (requestObject == null) {
            throw new PushServerException("Request object must not be empty");
        }
        logger.info("action: removeAndroid, state: initiated, applicationId: {}", requestObject.getAppId());
        String errorMessage = RemoveAndroidRequestValidator.validate(requestObject);
        if (errorMessage != null) {
            throw new PushServerException(errorMessage);
        }
        administrationService.removeAndroidAppCredentials(requestObject.getAppId());
        logger.info("action: removeAndroid, state: succeeded, applicationId: {}", requestObject.getAppId());
        return new Response();
    }

    /**
     * Remove FCM configuration.
     * @param appId Application identifier.
     * @return Response.
     * @throws PushServerException Thrown when application credentials entity could not be found or request validation fails.
     */
    @RequestMapping(value = "fcm", method = RequestMethod.DELETE)
    @Operation(summary = "Remove FCM configuration", description = "Remove FCM configuration from Push server")
    public Response removeFcm(@RequestParam("appId") String appId) throws PushServerException {
        if (appId == null) {
            throw new PushServerException("Parameter appId must not be empty");
        }
        logger.info("action: removeFcm, state: initiated, applicationId: {}", appId);
        administrationService.removeFcmAppCredentials(appId);
        logger.info("action: removeFcm, state: succeeded, applicationId: {}", appId);
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
    @Operation(summary = "Update Huawei configuration (deprecated)", description = "Update Huawei configuration endpoint (deprecated), use the POST /admin/app/hms endpoint")
    public Response updateHuawei(@Valid @RequestBody ObjectRequest<UpdateHuaweiRequest> request) throws PushServerException {
        final UpdateHuaweiRequest requestObject = request.getRequestObject();
        logger.info("action: updateHuawei, state: initiated, applicationId: {}", requestObject.getAppId());
        administrationService.updateHuaweiAppCredentials(requestObject);
        logger.info("action: updateHuawei, state: succeeded, applicationId: {}", requestObject.getAppId());
        return new Response();
    }

    /**
     * Update HMS configuration.
     *
     * @param request Update HMS configuration request.
     * @return Response.
     * @throws PushServerException Thrown when application credentials entity could not be found or request validation fails.
     */
    @RequestMapping(value = "hms", method = { RequestMethod.POST, RequestMethod.PUT })
    @Operation(summary = "Update HMS configuration", description = "Update HMS configuration in Push server")
    public Response updateHms(@Valid @RequestBody ObjectRequest<UpdateHmsRequest> request) throws PushServerException {
        final UpdateHmsRequest requestObject = request.getRequestObject();
        logger.info("action: updateHms, state: initiated, applicationId: {}", requestObject.getAppId());
        administrationService.updateHmsAppCredentials(requestObject);
        logger.info("action: updateHms, state: succeeded, applicationId: {}", requestObject.getAppId());
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
    @Operation(summary = "Remove Huawei configuration (deprecated)", description = "Remove Huawei configuration endpoint (deprecated), use the DELETE /admin/app/hms endpoint")
    public Response removeHuawei(@Valid @RequestBody ObjectRequest<RemoveHuaweiRequest> request) throws PushServerException {
        final RemoveHuaweiRequest requestObject = request.getRequestObject();
        logger.info("action: removeHuawei, state: initiated, applicationId: {}", requestObject.getAppId());
        administrationService.removeHuaweiAppCredentials(requestObject.getAppId());
        logger.info("action: removeHuawei, state: succeeded, applicationId: {}", requestObject.getAppId());
        return new Response();
    }

    /**
     * Remove HMS configuration.
     *
     * @param appId Application identifier.
     * @return Response.
     * @throws PushServerException Thrown when application credentials entity could not be found or request validation fails.
     */
    @RequestMapping(value = "hms", method = RequestMethod.DELETE)
    @Operation(summary = "Remove HMS configuration", description = "Remove HMS configuration from Push server")
    public Response removeHms(@RequestParam String appId) throws PushServerException {
        if (appId == null) {
            throw new PushServerException("Parameter appId must not be empty");
        }
        logger.info("action: removeHms, state: initiated, applicationId: {}", appId);
        administrationService.removeHmsAppCredentials(appId);
        logger.info("action: removeHms, state: succeeded, applicationId: {}", appId);
        return new Response();
    }

}
