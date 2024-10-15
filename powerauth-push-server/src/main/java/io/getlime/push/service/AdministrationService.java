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

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.wultra.security.powerauth.client.PowerAuthClient;
import com.wultra.security.powerauth.client.model.entity.Application;
import com.wultra.security.powerauth.client.model.error.PowerAuthClientException;
import io.getlime.push.errorhandling.exceptions.PushServerException;
import io.getlime.push.model.entity.PushServerApplication;
import io.getlime.push.model.enumeration.ApnsEnvironment;
import io.getlime.push.model.request.CreateApplicationRequest;
import io.getlime.push.model.request.UpdateAndroidRequest;
import io.getlime.push.model.request.UpdateHuaweiRequest;
import io.getlime.push.model.request.UpdateIosRequest;
import io.getlime.push.model.validator.CreateApplicationRequestValidator;
import io.getlime.push.repository.AppCredentialsRepository;
import io.getlime.push.repository.model.AppCredentialsEntity;
import io.getlime.push.service.http.HttpCustomizationService;
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

    @Transactional(readOnly = true)
    public List<PushServerApplication> findAllApplications() {
        return StreamSupport.stream(appCredentialsRepository.findAll().spliterator(),false)
                .map(appCredentialsEntity -> {
                    final PushServerApplication app = new PushServerApplication();
                    app.setAppId(appCredentialsEntity.getAppId());
                    app.setIos(appCredentialsEntity.getIosPrivateKey() != null);
                    app.setAndroid(appCredentialsEntity.getAndroidPrivateKey() != null);
                    app.setHuawei(isHuawei(appCredentialsEntity));
                    return app;
                })
                .toList();
    }

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

    @Transactional(readOnly = true)
    public AppCredentialsEntity findAppCredentials(final String appId) throws PushServerException {
        return findAppCredentialsByAppId(appId);
    }

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

    public void updateIosAppCredentials(final UpdateIosRequest request) throws PushServerException {
        final AppCredentialsEntity appCredentialsEntity = findAppCredentialsByAppId(request.getAppId());
        final byte[] privateKeyBytes = Base64.getDecoder().decode(request.getPrivateKeyBase64());
        appCredentialsEntity.setIosPrivateKey(privateKeyBytes);
        appCredentialsEntity.setIosTeamId(request.getTeamId());
        appCredentialsEntity.setIosKeyId(request.getKeyId());
        appCredentialsEntity.setIosBundle(request.getBundle());
        appCredentialsEntity.setIosEnvironment(convert(request.getEnvironment()));
        appCredentialsEntity.setTimestampLastUpdated(LocalDateTime.now());
        appCredentialsRepository.save(appCredentialsEntity);
        refreshCacheAfterCommit(appCredentialsEntity.getAppId());
        logger.info("The updateIos request succeeded, application credentials entity ID: {}", appCredentialsEntity.getId());
    }

    public void removeIosAppCredentials(final String appId) throws PushServerException {
        final AppCredentialsEntity appCredentialsEntity = findAppCredentialsByAppId(appId);
        appCredentialsEntity.setIosPrivateKey(null);
        appCredentialsEntity.setIosTeamId(null);
        appCredentialsEntity.setIosKeyId(null);
        appCredentialsEntity.setIosBundle(null);
        appCredentialsEntity.setIosEnvironment(null);
        appCredentialsEntity.setTimestampLastUpdated(LocalDateTime.now());
        appCredentialsRepository.save(appCredentialsEntity);
        refreshCacheAfterCommit(appCredentialsEntity.getAppId());
        logger.info("The removeIos request succeeded, application credentials entity ID: {}", appCredentialsEntity.getId());
    }

    public void updateAndroidAppCredentials(final UpdateAndroidRequest request) throws PushServerException {
        final AppCredentialsEntity appCredentialsEntity = findAppCredentialsByAppId(request.getAppId());
        final byte[] privateKeyBytes = Base64.getDecoder().decode(request.getPrivateKeyBase64());
        appCredentialsEntity.setAndroidPrivateKey(privateKeyBytes);
        appCredentialsEntity.setAndroidProjectId(request.getProjectId());
        appCredentialsEntity.setTimestampLastUpdated(LocalDateTime.now());
        appCredentialsRepository.save(appCredentialsEntity);
        refreshCacheAfterCommit(appCredentialsEntity.getAppId());
        logger.info("The updateAndroid request succeeded, application credentials entity ID: {}", appCredentialsEntity.getId());
    }

    public void removeAndroidAppCredentials(final String appId) throws PushServerException {
        final AppCredentialsEntity appCredentialsEntity = findAppCredentialsByAppId(appId);
        appCredentialsEntity.setAndroidPrivateKey(null);
        appCredentialsEntity.setAndroidProjectId(null);
        appCredentialsEntity.setTimestampLastUpdated(LocalDateTime.now());
        appCredentialsRepository.save(appCredentialsEntity);
        refreshCacheAfterCommit(appCredentialsEntity.getAppId());
        logger.info("The removeAndroid request succeeded, application credentials entity ID: {}", appCredentialsEntity.getId());
    }

    public void updateHuaweiAppCredentials(final UpdateHuaweiRequest request) throws PushServerException {
        final AppCredentialsEntity appCredentialsEntity = findAppCredentialsByAppId(request.getAppId());
        appCredentialsEntity.setHmsProjectId(request.getProjectId());
        appCredentialsEntity.setHmsClientId(request.getClientId());
        appCredentialsEntity.setHmsClientSecret(request.getClientSecret());
        appCredentialsEntity.setTimestampLastUpdated(LocalDateTime.now());
        appCredentialsRepository.save(appCredentialsEntity);
        refreshCacheAfterCommit(appCredentialsEntity.getAppId());
        logger.info("The update Huawei request succeeded, application credentials entity ID: {}", appCredentialsEntity.getId());
    }

    public void removeHuaweiAppCredentials(final String appId) throws PushServerException {
        final AppCredentialsEntity appCredentialsEntity = findAppCredentialsByAppId(appId);
        appCredentialsEntity.setHmsProjectId(null);
        appCredentialsEntity.setHmsClientSecret(null);
        appCredentialsEntity.setHmsClientId(null);
        appCredentialsEntity.setTimestampLastUpdated(LocalDateTime.now());
        appCredentialsRepository.save(appCredentialsEntity);
        refreshCacheAfterCommit(appId);
        logger.info("The remove Huawei request succeeded, application credentials entity ID: {}", appCredentialsEntity.getId());
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
     * @throws PushServerException Thrown when application credentials entity does not exists.
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

    private static boolean isHuawei(final AppCredentialsEntity appCredentialsEntity) {
        return appCredentialsEntity.getHmsClientSecret() != null && appCredentialsEntity.getHmsClientId() != null;
    }
}
