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

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.wultra.security.powerauth.client.PowerAuthClient;
import com.wultra.security.powerauth.client.model.entity.Application;
import com.wultra.security.powerauth.client.model.error.PowerAuthClientException;
import com.wultra.push.errorhandling.exceptions.PushServerException;
import com.wultra.push.model.entity.PushServerApplication;
import com.wultra.push.model.enumeration.ApnsEnvironment;
import com.wultra.push.model.request.*;
import com.wultra.push.model.validator.CreateApplicationRequestValidator;
import com.wultra.push.repository.AppCredentialsRepository;
import com.wultra.push.repository.model.AppCredentialsEntity;
import com.wultra.push.service.http.HttpCustomizationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.StreamSupport;

/**
 * Administration service.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class AdministrationService {

    private final PowerAuthClient powerAuthClient;
    private final AppCredentialsRepository appCredentialsRepository;
    private final LoadingCache<String, AppRelatedPushClient> appRelatedPushClientCache;
    private final HttpCustomizationService httpCustomizationService;

    /**
     * Find all applications.
     *
     * @return List of Push Server application.
     */
    @Transactional(readOnly = true)
    public List<PushServerApplication> findAllApplications() {
        return StreamSupport.stream(appCredentialsRepository.findAll().spliterator(),false)
                .map(appCredentialsEntity -> {
                    final PushServerApplication app = new PushServerApplication();
                    app.setAppId(appCredentialsEntity.getAppId());
                    app.setApns(appCredentialsEntity.getApnsPrivateKey() != null);
                    app.setFcm(appCredentialsEntity.getFcmPrivateKey() != null);
                    app.setHms(isHms(appCredentialsEntity));
                    return app;
                })
                .toList();
    }

    /**
     * Find all unconfigured applications.
     * @return List of unconfigured Push Server applications.
     * @throws PowerAuthClientException Thrown in case request fails.
     */
    @Transactional(readOnly = true)
    public List<PushServerApplication> findUnconfiguredApplications() throws PowerAuthClientException {
        // Get all applications in PA Server
        final List<Application> applicationList = getApplicationList().getApplications();

        // Get all applications that are already set up
        final Iterable<AppCredentialsEntity> appCredentials = appCredentialsRepository.findAll();

        // Compute intersection by app ID
        final Set<String> identifiers = new HashSet<>();
        for (AppCredentialsEntity appCred: appCredentials) {
            identifiers.add(appCred.getAppId());
        }

        final List<PushServerApplication> result = new ArrayList<>();
        for (Application app : applicationList) {
            if (!identifiers.contains(app.getApplicationId())) {
                final PushServerApplication applicationToAdd = new PushServerApplication();
                applicationToAdd.setAppId(app.getApplicationId());
                result.add(applicationToAdd);
            }
        }

        return result;
    }

    /**
     * Find application credentials entity.
     * @param appId Application identifier.
     * @return Application credentials entity.
     * @throws PushServerException Thrown when application credentials entity does not exist.
     */
    @Transactional(readOnly = true)
    public AppCredentialsEntity findAppCredentials(final String appId) throws PushServerException {
        return findAppCredentialsByAppId(appId);
    }

    /**
     * Create application credentials.
     * @param request Create application request.
     * @return Application credentials entity.
     * @throws PushServerException Thrown in case application already exists.
     */
    public AppCredentialsEntity createAppCredentials(final CreateApplicationRequest request) throws PushServerException {
        String errorMessage = CreateApplicationRequestValidator.validate(request);
        final Optional<AppCredentialsEntity> appCredentialsEntityOptional = appCredentialsRepository.findFirstByAppId(request.getAppId());
        if (appCredentialsEntityOptional.isPresent()) {
            errorMessage = "Application already exists";
        }
        if (errorMessage != null) {
            throw new PushServerException(errorMessage);
        }
        final AppCredentialsEntity appCredentialsEntity = new AppCredentialsEntity();
        appCredentialsEntity.setAppId(request.getAppId());
        return appCredentialsRepository.save(appCredentialsEntity);
    }

    /**
     * Update iOS application credentials.
     *
     * @deprecated use {@link #updateApnsAppCredentials(UpdateApnsRequest)}
     *
     * @param request Update iOS application credentials request.
     * @throws PushServerException Thrown in case application does not exist.
     */
    @Deprecated
    public void updateIosAppCredentials(final UpdateIosRequest request) throws PushServerException {
        final UpdateApnsRequest apnsRequest = new UpdateApnsRequest();
        apnsRequest.setAppId(request.getAppId());
        apnsRequest.setPrivateKeyBase64(request.getPrivateKeyBase64());
        apnsRequest.setTeamId(request.getTeamId());
        apnsRequest.setKeyId(request.getKeyId());
        apnsRequest.setBundle(request.getBundle());
        apnsRequest.setEnvironment(request.getEnvironment());
        updateApnsAppCredentials(apnsRequest);
    }

    /**
     * Update APNs application credentials.
     *
     * @param request Update APNs application credentials request.
     * @throws PushServerException Thrown in case application does not exist.
     */
    public void updateApnsAppCredentials(final UpdateApnsRequest request) throws PushServerException {
        final AppCredentialsEntity appCredentialsEntity = findAppCredentialsByAppId(request.getAppId());
        final byte[] privateKeyBytes = Base64.getDecoder().decode(request.getPrivateKeyBase64());
        appCredentialsEntity.setApnsPrivateKey(privateKeyBytes);
        appCredentialsEntity.setApnsTeamId(request.getTeamId());
        appCredentialsEntity.setApnsKeyId(request.getKeyId());
        appCredentialsEntity.setApnsBundle(request.getBundle());
        appCredentialsEntity.setApnsEnvironment(convert(request.getEnvironment()));
        appCredentialsEntity.setTimestampLastUpdated(LocalDateTime.now());
        appCredentialsRepository.save(appCredentialsEntity);
        refreshCacheAfterCommit(appCredentialsEntity.getAppId());
        logger.info("The updateApns request succeeded, application credentials entity ID: {}", appCredentialsEntity.getId());
    }

    /**
     * Remove iOS application credentials.
     *
     * @deprecated use {@link #removeApnsAppCredentials(String)}
     *
     * @param appId Application identifier.
     * @throws PushServerException Thrown in case application does not exist.
     */
    @Deprecated
    public void removeIosAppCredentials(final String appId) throws PushServerException {
        removeApnsAppCredentials(appId);
    }

    /**
     * Remove APNs application credentials.
     *
     * @param appId Application identifier.
     *
     * @throws PushServerException Thrown in case application does not exist.
     */
    public void removeApnsAppCredentials(final String appId) throws PushServerException {
        final AppCredentialsEntity appCredentialsEntity = findAppCredentialsByAppId(appId);
        appCredentialsEntity.setApnsPrivateKey(null);
        appCredentialsEntity.setApnsTeamId(null);
        appCredentialsEntity.setApnsKeyId(null);
        appCredentialsEntity.setApnsBundle(null);
        appCredentialsEntity.setApnsEnvironment(null);
        appCredentialsEntity.setTimestampLastUpdated(LocalDateTime.now());
        appCredentialsRepository.save(appCredentialsEntity);
        refreshCacheAfterCommit(appCredentialsEntity.getAppId());
        logger.info("The removeApns request succeeded, application credentials entity ID: {}", appCredentialsEntity.getId());
    }

    /**
     * Update Android application credentials.
     *
     * @deprecated use {@link #updateFcmAppCredentials(UpdateFcmRequest)}
     *
     * @param request Update Android application credentials request.
     * @throws PushServerException  Thrown in case application does not exist.
     */
    @Deprecated
    public void updateAndroidAppCredentials(final UpdateAndroidRequest request) throws PushServerException {
        final UpdateFcmRequest fcmRequest = new UpdateFcmRequest();
        fcmRequest.setAppId(request.getAppId());
        fcmRequest.setPrivateKeyBase64(request.getPrivateKeyBase64());
        fcmRequest.setProjectId(request.getProjectId());
        updateFcmAppCredentials(fcmRequest);
    }

    /**
     * Update FCM application credentials.
     *
     * @param request Update FCM request.
     * @throws PushServerException Thrown when application credentials entity does not exist.
     */
    public void updateFcmAppCredentials(final UpdateFcmRequest request) throws PushServerException {
        final AppCredentialsEntity appCredentialsEntity = findAppCredentialsByAppId(request.getAppId());
        final byte[] privateKeyBytes = Base64.getDecoder().decode(request.getPrivateKeyBase64());
        appCredentialsEntity.setFcmPrivateKey(privateKeyBytes);
        appCredentialsEntity.setFcmProjectId(request.getProjectId());
        appCredentialsEntity.setTimestampLastUpdated(LocalDateTime.now());
        appCredentialsRepository.save(appCredentialsEntity);
        refreshCacheAfterCommit(appCredentialsEntity.getAppId());
        logger.info("The updateFcm request succeeded, application credentials entity ID: {}", appCredentialsEntity.getId());
    }

    /**
     * Remove Android application credentials.
     *
     * @deprecated use {@link #removeFcmAppCredentials(String)}
     *
     * @param appId Application identifier.
     * @throws PushServerException Thrown when application credentials entity does not exist.
     */
    public void removeAndroidAppCredentials(final String appId) throws PushServerException {
        removeFcmAppCredentials(appId);
    }

    /**
     * Remove FCM application credentials.
     *
     * @param appId Application identifier.
     * @throws PushServerException Thrown when application credentials entity does not exist.
     */
    public void removeFcmAppCredentials(final String appId) throws PushServerException {
        final AppCredentialsEntity appCredentialsEntity = findAppCredentialsByAppId(appId);
        appCredentialsEntity.setFcmPrivateKey(null);
        appCredentialsEntity.setFcmProjectId(null);
        appCredentialsEntity.setTimestampLastUpdated(LocalDateTime.now());
        appCredentialsRepository.save(appCredentialsEntity);
        refreshCacheAfterCommit(appCredentialsEntity.getAppId());
        logger.info("The removeFcm request succeeded, application credentials entity ID: {}", appCredentialsEntity.getId());
    }

    /**
     * Update Huawei application credentials.
     *
     * @deprecated use {@link #updateHmsAppCredentials(UpdateHmsRequest)}
     *
     * @param request Update Huawei application credentials request.
     * @throws PushServerException Thrown when application credentials entity does not exist.
     */
    @Deprecated
    public void updateHuaweiAppCredentials(final UpdateHuaweiRequest request) throws PushServerException {
        final UpdateHmsRequest hmsRequest = new UpdateHmsRequest();
        hmsRequest.setAppId(request.getAppId());
        hmsRequest.setProjectId(request.getProjectId());
        hmsRequest.setClientId(request.getClientId());
        hmsRequest.setClientSecret(request.getClientSecret());
        updateHmsAppCredentials(hmsRequest);
    }

    /**
     * Update HMS application credentials.
     *
     * @param request Update HMS application credentials request.
     * @throws PushServerException Thrown when application credentials entity does not exist.
     */
    public void updateHmsAppCredentials(final UpdateHmsRequest request) throws PushServerException {
        final AppCredentialsEntity appCredentialsEntity = findAppCredentialsByAppId(request.getAppId());
        appCredentialsEntity.setHmsProjectId(request.getProjectId());
        appCredentialsEntity.setHmsClientId(request.getClientId());
        appCredentialsEntity.setHmsClientSecret(request.getClientSecret());
        appCredentialsEntity.setTimestampLastUpdated(LocalDateTime.now());
        appCredentialsRepository.save(appCredentialsEntity);
        refreshCacheAfterCommit(appCredentialsEntity.getAppId());
        logger.info("The updateHms request succeeded, application credentials entity ID: {}", appCredentialsEntity.getId());
    }

    /**
     * Remove Huawei application credentials.
     *
     * @deprecated use {@link #removeHmsAppCredentials(String)}
     *
     * @param appId Application identifier.
     * @throws PushServerException Thrown when application credentials entity does not exist.
     */
    @Deprecated
    public void removeHuaweiAppCredentials(final String appId) throws PushServerException {
        removeHmsAppCredentials(appId);
    }

    /**
     * Remove HMS application credentials.
     *
     * @param appId Application identifier.
     * @throws PushServerException Thrown when application credentials entity does not exist.
     */
    public void removeHmsAppCredentials(final String appId) throws PushServerException {
        final AppCredentialsEntity appCredentialsEntity = findAppCredentialsByAppId(appId);
        appCredentialsEntity.setHmsProjectId(null);
        appCredentialsEntity.setHmsClientSecret(null);
        appCredentialsEntity.setHmsClientId(null);
        appCredentialsEntity.setTimestampLastUpdated(LocalDateTime.now());
        appCredentialsRepository.save(appCredentialsEntity);
        refreshCacheAfterCommit(appId);
        logger.info("The removeHms request succeeded, application credentials entity ID: {}", appCredentialsEntity.getId());
    }

    private void refreshCacheAfterCommit(final String appId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                appRelatedPushClientCache.refresh(appId);
            }
        });
    }

    /**
     * Find app credentials entity by ID.
     * @param powerAuthAppId App credentials ID.
     * @return App credentials entity.
     * @throws PushServerException Thrown when application credentials entity does not exist.
     */
    private AppCredentialsEntity findAppCredentialsByAppId(String powerAuthAppId) throws PushServerException {
        return appCredentialsRepository.findFirstByAppId(powerAuthAppId).orElseThrow(() ->
                new PushServerException("Application credentials with entered ID: %s does not exist".formatted(powerAuthAppId)));
    }

    private com.wultra.security.powerauth.client.model.response.GetApplicationListResponse getApplicationList() throws PowerAuthClientException {
        return powerAuthClient.getApplicationList(
                httpCustomizationService.getQueryParams(),
                httpCustomizationService.getHttpHeaders()
        );
    }

    private static String convert(final ApnsEnvironment environment) {
        return Optional.ofNullable(environment)
                .map(ApnsEnvironment::getKey)
                .orElse(null);
    }

    private static boolean isHms(final AppCredentialsEntity appCredentialsEntity) {
        return appCredentialsEntity.getHmsClientSecret() != null && appCredentialsEntity.getHmsClientId() != null;
    }
}
