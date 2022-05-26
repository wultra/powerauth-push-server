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

import com.google.common.io.BaseEncoding;
import com.wultra.security.powerauth.client.PowerAuthClient;
import com.wultra.security.powerauth.client.model.error.PowerAuthClientException;
import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.core.rest.model.base.response.Response;
import io.getlime.push.errorhandling.exceptions.PushServerException;
import io.getlime.push.model.entity.PushServerApplication;
import io.getlime.push.model.request.*;
import io.getlime.push.model.response.CreateApplicationResponse;
import io.getlime.push.model.response.GetApplicationDetailResponse;
import io.getlime.push.model.response.GetApplicationListResponse;
import io.getlime.push.model.validator.*;
import io.getlime.push.repository.AppCredentialsRepository;
import io.getlime.push.repository.model.AppCredentialsEntity;
import io.getlime.push.service.batch.storage.AppCredentialStorageMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping(value = "admin/app")
public class AdministrationController {

    private static final Logger logger = LoggerFactory.getLogger(AdministrationController.class);

    private final PowerAuthClient powerAuthClient;
    private final AppCredentialsRepository appCredentialsRepository;
    private final AppCredentialStorageMap appCredentialStorageMap;

    /**
     * Constructor with injected fields.
     * @param powerAuthClient PowerAuth service client.
     * @param appCredentialsRepository Application credentials repository.
     * @param appCredentialStorageMap Application credentials storage map.
     */
    @Autowired
    public AdministrationController(PowerAuthClient powerAuthClient, AppCredentialsRepository appCredentialsRepository, AppCredentialStorageMap appCredentialStorageMap) {
        this.powerAuthClient = powerAuthClient;
        this.appCredentialsRepository = appCredentialsRepository;
        this.appCredentialStorageMap = appCredentialStorageMap;
    }

    /**
     * List applications configured in Push Server.
     * @return Application list response.
     */
    @GetMapping(value = "list")
    public ObjectResponse<GetApplicationListResponse> listApplications() {
        logger.debug("Received listApplications request");
        final GetApplicationListResponse response = new GetApplicationListResponse();
        final Iterable<AppCredentialsEntity> appCredentials = appCredentialsRepository.findAll();
        final List<PushServerApplication> appList = new ArrayList<>();
        for (AppCredentialsEntity appCredentialsEntity : appCredentials) {
            final PushServerApplication app = new PushServerApplication();
            app.setAppId(appCredentialsEntity.getAppId());
            app.setIos(appCredentialsEntity.getIosPrivateKey() != null);
            app.setAndroid(appCredentialsEntity.getAndroidPrivateKey() != null);
            appList.add(app);
        }
        response.setApplicationList(appList);
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
            final GetApplicationListResponse response = new GetApplicationListResponse();

            // Get all applications in PA Server
            final List<com.wultra.security.powerauth.client.v3.GetApplicationListResponse.Applications> applicationList = powerAuthClient.getApplicationList();

            // Get all applications that are already set up
            final Iterable<AppCredentialsEntity> appCredentials = appCredentialsRepository.findAll();

            // Compute intersection by app ID
            final Set<String> identifiers = new HashSet<>();
            for (AppCredentialsEntity appCred: appCredentials) {
                identifiers.add(appCred.getAppId());
            }
            for (com.wultra.security.powerauth.client.v3.GetApplicationListResponse.Applications app : applicationList) {
                if (!identifiers.contains(app.getApplicationId())) {
                    final PushServerApplication applicationToAdd = new PushServerApplication();
                    applicationToAdd.setAppId(app.getApplicationId());
                    // add apps in intersection
                    response.getApplicationList().add(applicationToAdd);
                }
            }
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
        final AppCredentialsEntity appCredentialsEntity = findAppCredentialsEntityById(requestObject.getAppId());
        final PushServerApplication app = new PushServerApplication();
        app.setAppId(appCredentialsEntity.getAppId());
        app.setIos(appCredentialsEntity.getIosPrivateKey() != null);
        app.setAndroid(appCredentialsEntity.getAndroidPrivateKey() != null);
        response.setApplication(app);
        if (requestObject.getIncludeIos()) {
            response.setIosBundle(appCredentialsEntity.getIosBundle());
            response.setIosKeyId(appCredentialsEntity.getIosKeyId());
            response.setIosTeamId(appCredentialsEntity.getIosTeamId());
            response.setIosEnvironment(appCredentialsEntity.getIosEnvironment());
        }
        if (requestObject.getIncludeAndroid()) {
            response.setAndroidProjectId(appCredentialsEntity.getAndroidProjectId());
        }
        logger.debug("The getApplicationDetail request succeeded");
        return new ObjectResponse<>(response);
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
        String errorMessage = CreateApplicationRequestValidator.validate(requestObject);
        final Optional<AppCredentialsEntity> existingAppCredentialsEntity = appCredentialsRepository.findFirstByAppId(requestObject.getAppId());
        if (existingAppCredentialsEntity.isPresent()) {
            errorMessage = "Application already exists";
        }
        if (errorMessage != null) {
            throw new PushServerException(errorMessage);
        }
        final AppCredentialsEntity appCredentialsEntity = new AppCredentialsEntity();
        appCredentialsEntity.setAppId(requestObject.getAppId());
        final AppCredentialsEntity newAppCredentialsEntity = appCredentialsRepository.save(appCredentialsEntity);
        final CreateApplicationResponse response = new CreateApplicationResponse(newAppCredentialsEntity.getAppId());
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
        logger.info("Received updateIos request, application credentials entity ID: {}", requestObject.getAppId());
        final String errorMessage = UpdateIosRequestValidator.validate(requestObject);
        if (errorMessage != null) {
            throw new PushServerException(errorMessage);
        }
        final AppCredentialsEntity appCredentialsEntity = findAppCredentialsEntityById(requestObject.getAppId());
        final byte[] privateKeyBytes = BaseEncoding.base64().decode(requestObject.getPrivateKeyBase64());
        appCredentialsEntity.setIosPrivateKey(privateKeyBytes);
        appCredentialsEntity.setIosTeamId(requestObject.getTeamId());
        appCredentialsEntity.setIosKeyId(requestObject.getKeyId());
        appCredentialsEntity.setIosBundle(requestObject.getBundle());
        appCredentialsEntity.setIosEnvironment(requestObject.getEnvironment());
        appCredentialsRepository.save(appCredentialsEntity);
        appCredentialStorageMap.cleanByKey(appCredentialsEntity.getAppId());
        logger.info("The updateIos request succeeded, application credentials entity ID: {}", requestObject.getAppId());
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
        logger.info("Received removeIos request, application credentials entity ID: {}", requestObject.getAppId());
        String errorMessage = RemoveIosRequestValidator.validate(requestObject);
        if (errorMessage != null) {
            throw new PushServerException(errorMessage);
        }
        final AppCredentialsEntity appCredentialsEntity = findAppCredentialsEntityById(requestObject.getAppId());
        appCredentialsEntity.setIosPrivateKey(null);
        appCredentialsEntity.setIosTeamId(null);
        appCredentialsEntity.setIosKeyId(null);
        appCredentialsEntity.setIosBundle(null);
        appCredentialsEntity.setIosEnvironment(null);
        appCredentialsRepository.save(appCredentialsEntity);
        appCredentialStorageMap.cleanByKey(appCredentialsEntity.getAppId());
        logger.info("The removeIos request succeeded, application credentials entity ID: {}", requestObject.getAppId());
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
        logger.info("Received updateAndroid request, application credentials entity ID: {}", requestObject.getAppId());
        String errorMessage = UpdateAndroidRequestValidator.validate(requestObject);
        if (errorMessage != null) {
            throw new PushServerException(errorMessage);
        }
        final AppCredentialsEntity appCredentialsEntity = findAppCredentialsEntityById(requestObject.getAppId());
        byte[] privateKeyBytes = BaseEncoding.base64().decode(requestObject.getPrivateKeyBase64());
        appCredentialsEntity.setAndroidPrivateKey(privateKeyBytes);
        appCredentialsEntity.setAndroidProjectId(requestObject.getProjectId());
        appCredentialsRepository.save(appCredentialsEntity);
        appCredentialStorageMap.cleanByKey(appCredentialsEntity.getAppId());
        logger.info("The updateAndroid request succeeded, application credentials entity ID: {}", requestObject.getAppId());
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
        logger.info("Received removeAndroid request, application credentials entity ID: {}", requestObject.getAppId());
        String errorMessage = RemoveAndroidRequestValidator.validate(requestObject);
        if (errorMessage != null) {
            throw new PushServerException(errorMessage);
        }
        final AppCredentialsEntity appCredentialsEntity = findAppCredentialsEntityById(requestObject.getAppId());
        appCredentialsEntity.setAndroidPrivateKey(null);
        appCredentialsEntity.setAndroidProjectId(null);
        appCredentialsRepository.save(appCredentialsEntity);
        appCredentialStorageMap.cleanByKey(appCredentialsEntity.getAppId());
        logger.info("The removeAndroid request succeeded, application credentials entity ID: {}", requestObject.getAppId());
        return new Response();
    }

    /**
     * Find app credentials entity by ID.
     * @param powerAuthAppId App credentials ID.
     * @return App credentials entity.
     * @throws PushServerException Thrown when application credentials entity does not exists.
     */
    private AppCredentialsEntity findAppCredentialsEntityById(String powerAuthAppId) throws PushServerException {
        final Optional<AppCredentialsEntity> appCredentialsEntityOptional = appCredentialsRepository.findFirstByAppId(powerAuthAppId);
        if (!appCredentialsEntityOptional.isPresent()) {
            throw new PushServerException("Application credentials with entered ID does not exist");
        }
        return appCredentialsEntityOptional.get();
    }
}
