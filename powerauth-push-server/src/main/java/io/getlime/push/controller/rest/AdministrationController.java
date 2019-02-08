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
import io.getlime.security.powerauth.soap.spring.client.PowerAuthServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Controller
@RequestMapping(value = "admin/app")
public class AdministrationController {

    private final PowerAuthServiceClient powerAuthClient;
    private final AppCredentialsRepository appCredentialsRepository;
    private final AppCredentialStorageMap appCredentialStorageMap;

    /**
     * Constructor with injected fields.
     * @param powerAuthClient PowerAuth service client.
     * @param appCredentialsRepository Application credentials repository.
     * @param appCredentialStorageMap Application credentials storage map.
     */
    @Autowired
    public AdministrationController(PowerAuthServiceClient powerAuthClient, AppCredentialsRepository appCredentialsRepository, AppCredentialStorageMap appCredentialStorageMap) {
        this.powerAuthClient = powerAuthClient;
        this.appCredentialsRepository = appCredentialsRepository;
        this.appCredentialStorageMap = appCredentialStorageMap;
    }

    /**
     * List applications configured in Push Server.
     * @return Application list response.
     */
    @RequestMapping(value = "list", method = RequestMethod.GET)
    public @ResponseBody ObjectResponse<GetApplicationListResponse> listApplications() {
        final GetApplicationListResponse response = new GetApplicationListResponse();
        final Iterable<AppCredentialsEntity> appCredentials = appCredentialsRepository.findAll();
        final List<PushServerApplication> appList = new ArrayList<>();
        for (AppCredentialsEntity appCredentialsEntity : appCredentials) {
            PushServerApplication app = new PushServerApplication();
            app.setId(appCredentialsEntity.getId());
            app.setAppId(appCredentialsEntity.getAppId());
            app.setAppName(powerAuthClient.getApplicationDetail(appCredentialsEntity.getAppId()).getApplicationName());
            app.setIos(appCredentialsEntity.getIosPrivateKey() != null);
            app.setAndroid(appCredentialsEntity.getAndroidPrivateKey() != null);
            appList.add(app);
        }
        response.setApplicationList(appList);
        return new ObjectResponse<>(response);
    }

    /**
     * List applications which are not yet configured in Push Server.
     * @return Application list response.
     */
    @RequestMapping(value = "unconfigured/list", method = RequestMethod.GET)
    public @ResponseBody ObjectResponse<GetApplicationListResponse> listUnconfiguredApplications() {
        GetApplicationListResponse response = new GetApplicationListResponse();
        // Get all applications in PA Server
        final List<io.getlime.powerauth.soap.v3.GetApplicationListResponse.Applications> applicationList = powerAuthClient.getApplicationList();

        // Get all applications that are already set up
        final Iterable<AppCredentialsEntity> appCredentials = appCredentialsRepository.findAll();

        // Compute intersection by app ID
        Set<Long> identifiers = new HashSet<>();
        for (AppCredentialsEntity appCred: appCredentials) {
            identifiers.add(appCred.getAppId());
        }
        for (io.getlime.powerauth.soap.v3.GetApplicationListResponse.Applications app : applicationList) {
            if (!identifiers.contains(app.getId())) {
                PushServerApplication applicationToAdd = new PushServerApplication();
                applicationToAdd.setId(app.getId());
                applicationToAdd.setAppName(app.getApplicationName());
                // add apps in intersection
                response.getApplicationList().add(applicationToAdd);
            }
        }
        return new ObjectResponse<>(response);
    }

    /**
     * Get application credentials entity details.
     * @param request Application detail request.
     * @return Application detail response.
     * @throws PushServerException Thrown when application credentials entity could not be found or request validation fails.
     */
    @RequestMapping(value = "detail", method = RequestMethod.POST)
    public @ResponseBody ObjectResponse<GetApplicationDetailResponse> getApplicationDetail(@RequestBody ObjectRequest<GetApplicationDetailRequest> request) throws PushServerException {
        final GetApplicationDetailRequest requestObject = request.getRequestObject();
        String errorMessage = GetApplicationDetailRequestValidator.validate(requestObject);
        if (errorMessage != null) {
            throw new PushServerException(errorMessage);
        }
        final GetApplicationDetailResponse response = new GetApplicationDetailResponse();
        final AppCredentialsEntity appCredentialsEntity = findAppCredentialsEntityById(requestObject.getId());
        final PushServerApplication app = new PushServerApplication();
        app.setId(appCredentialsEntity.getId());
        app.setAppId(appCredentialsEntity.getAppId());
        app.setIos(appCredentialsEntity.getIosPrivateKey() != null);
        app.setAndroid(appCredentialsEntity.getAndroidPrivateKey() != null);
        app.setAppName(powerAuthClient.getApplicationDetail(appCredentialsEntity.getAppId()).getApplicationName());
        response.setApplication(app);
        if (requestObject.getIncludeIos()) {
            response.setIosBundle(appCredentialsEntity.getIosBundle());
            response.setIosKeyId(appCredentialsEntity.getIosKeyId());
            response.setIosTeamId(appCredentialsEntity.getIosTeamId());
        }
        if (requestObject.getIncludeAndroid()) {
            response.setAndroidProjectId(appCredentialsEntity.getAndroidProjectId());
        }
        return new ObjectResponse<>(response);
    }

    /**
     * Create application.
     * @param request Create application request.
     * @return Create application response.
     * @throws PushServerException Thrown when request validation fails.
     */
    @RequestMapping(value = "create", method = RequestMethod.POST)
    public @ResponseBody ObjectResponse<CreateApplicationResponse> createApplication(@RequestBody ObjectRequest<CreateApplicationRequest> request) throws PushServerException {
        final CreateApplicationRequest requestObject = request.getRequestObject();
        String errorMessage = CreateApplicationRequestValidator.validate(requestObject);
        if (errorMessage != null) {
            throw new PushServerException(errorMessage);
        }
        final AppCredentialsEntity appCredentialsEntity = new AppCredentialsEntity();
        appCredentialsEntity.setAppId(requestObject.getAppId());
        AppCredentialsEntity newAppCredentialsEntity = appCredentialsRepository.save(appCredentialsEntity);
        final CreateApplicationResponse response = new CreateApplicationResponse(newAppCredentialsEntity.getId());
        return new ObjectResponse<>(response);
    }

    /**
     * Update iOS configuration.
     * @param request Update iOS configuration request.
     * @return Response.
     * @throws PushServerException Thrown when application credentials entity could not be found or request validation fails.
     */
    @RequestMapping(value = "ios/update", method = RequestMethod.PUT)
    public @ResponseBody Response updateIos(@RequestBody ObjectRequest<UpdateIosRequest> request) throws PushServerException {
        final UpdateIosRequest requestObject = request.getRequestObject();
        String errorMessage = UpdateIosRequestValidator.validate(requestObject);
        if (errorMessage != null) {
            throw new PushServerException(errorMessage);
        }
        final AppCredentialsEntity appCredentialsEntity = findAppCredentialsEntityById(requestObject.getId());
        byte[] privateKeyBytes = BaseEncoding.base64().decode(requestObject.getPrivateKeyBase64());
        appCredentialsEntity.setIosPrivateKey(privateKeyBytes);
        appCredentialsEntity.setIosTeamId(requestObject.getTeamId());
        appCredentialsEntity.setIosKeyId(requestObject.getKeyId());
        appCredentialsEntity.setIosBundle(requestObject.getBundle());
        appCredentialsRepository.save(appCredentialsEntity);
        appCredentialStorageMap.cleanByKey(appCredentialsEntity.getAppId());
        return new Response();
    }

    /**
     * Remove iOS configuration.
     * @param request Remove iOS configuration request.
     * @return Response.
     * @throws PushServerException Thrown when application credentials entity could not be found or request validation fails.
     */
    @RequestMapping(value = "ios/remove", method = RequestMethod.POST)
    public @ResponseBody Response removeIos(@RequestBody ObjectRequest<RemoveIosRequest> request) throws PushServerException {
        final RemoveIosRequest requestObject = request.getRequestObject();
        String errorMessage = RemoveIosRequestValidator.validate(requestObject);
        if (errorMessage != null) {
            throw new PushServerException(errorMessage);
        }
        final AppCredentialsEntity appCredentialsEntity = findAppCredentialsEntityById(requestObject.getId());
        appCredentialsEntity.setIosPrivateKey(null);
        appCredentialsEntity.setIosTeamId(null);
        appCredentialsEntity.setIosKeyId(null);
        appCredentialsEntity.setIosBundle(null);
        appCredentialsRepository.save(appCredentialsEntity);
        appCredentialStorageMap.cleanByKey(requestObject.getId());
        return new Response();
    }

    /**
     * Update Android configuration.
     * @param request Update Android configuration request.
     * @return Response.
     * @throws PushServerException Thrown when application credentials entity could not be found or request validation fails.
     */
    @RequestMapping(value = "android/update", method = RequestMethod.PUT)
    public @ResponseBody Response updateAndroid(@RequestBody ObjectRequest<UpdateAndroidRequest> request) throws PushServerException {
        final UpdateAndroidRequest requestObject = request.getRequestObject();
        String errorMessage = UpdateAndroidRequestValidator.validate(requestObject);
        if (errorMessage != null) {
            throw new PushServerException(errorMessage);
        }
        final AppCredentialsEntity appCredentialsEntity = findAppCredentialsEntityById(requestObject.getId());
        byte[] privateKeyBytes = BaseEncoding.base64().decode(requestObject.getPrivateKeyBase64());
        appCredentialsEntity.setAndroidPrivateKey(privateKeyBytes);
        appCredentialsEntity.setAndroidProjectId(requestObject.getProjectId());
        appCredentialsRepository.save(appCredentialsEntity);
        appCredentialStorageMap.cleanByKey(appCredentialsEntity.getAppId());
        return new Response();
    }

    /**
     * Remove Android configuration.
     * @param request Remove Android configuration request.
     * @return Response.
     * @throws PushServerException Thrown when application credentials entity could not be found or request validation fails.
     */
    @RequestMapping(value = "android/remove", method = RequestMethod.POST)
    public @ResponseBody Response removeAndroid(@RequestBody ObjectRequest<RemoveAndroidRequest> request) throws PushServerException {
        final RemoveAndroidRequest requestObject = request.getRequestObject();
        String errorMessage = RemoveAndroidRequestValidator.validate(requestObject);
        if (errorMessage != null) {
            throw new PushServerException(errorMessage);
        }
        final AppCredentialsEntity appCredentialsEntity = findAppCredentialsEntityById(requestObject.getId());
        appCredentialsEntity.setAndroidPrivateKey(null);
        appCredentialsEntity.setAndroidProjectId(null);
        appCredentialsRepository.save(appCredentialsEntity);
        appCredentialStorageMap.cleanByKey(requestObject.getId());
        return new Response();
    }

    /**
     * Find app credentials entity by ID.
     * @param id App credentials ID.
     * @return App credentials entity.
     * @throws PushServerException Thrown when application credentials entity does not exists.
     */
    private AppCredentialsEntity findAppCredentialsEntityById(Long id) throws PushServerException {
        final Optional<AppCredentialsEntity> appCredentialsEntityOptional = appCredentialsRepository.findById(id);
        if (!appCredentialsEntityOptional.isPresent()) {
            throw new PushServerException("Application credentials with entered ID does not exist");
        }
        return appCredentialsEntityOptional.get();
    }
}
