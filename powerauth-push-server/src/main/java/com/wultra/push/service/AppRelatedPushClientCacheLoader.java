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

import com.eatthepath.pushy.apns.ApnsClient;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.wultra.push.configuration.PushServiceConfiguration;
import com.wultra.push.errorhandling.exceptions.PushServerException;
import com.wultra.push.model.enumeration.ApnsEnvironment;
import com.wultra.push.repository.AppCredentialsRepository;
import com.wultra.push.repository.model.AppCredentialsEntity;
import com.wultra.push.service.fcm.FcmClient;
import com.wultra.push.service.hms.HmsClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Specialization of {@link CacheLoader} for {@link AppRelatedPushClient}.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
@AllArgsConstructor
@Slf4j
@Component
public class AppRelatedPushClientCacheLoader implements CacheLoader<String, AppRelatedPushClient> {

    private final AppCredentialsRepository appCredentialsRepository;

    private final PushSendingWorker pushSendingWorker;

    private final PushServiceConfiguration configuration;

    /**
     * Smartly reload {@link AppRelatedPushClient}.
     * Fetch {@link AppCredentialsEntity#getTimestampLastUpdated()} and reload only if the value differs from the value store in the cache.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public AppRelatedPushClient reload(final String appId, final AppRelatedPushClient oldAppRelatedPushClient) throws Exception {
        final AppCredentialsEntity credentials = appCredentialsRepository.findFirstByAppId(appId).orElse(null);
        if (credentials == null) {
            logger.warn("AppCredentials does not exist anymore for app: {}", appId);
            return null;
        }

        final LocalDateTime lastUpdatedInDb = credentials.getTimestampLastUpdated();
        final LocalDateTime lastUpdatedInCache = oldAppRelatedPushClient.getAppCredentials().getTimestampLastUpdated();
        if (Objects.equals(lastUpdatedInCache, lastUpdatedInDb)) {
            logger.debug("LastUpdated is same for app: {}", appId);
            return oldAppRelatedPushClient;
        }

        logger.debug("LastUpdated differs for app: {}", appId);
        return createPushClient(credentials);
    }

    @Override
    public AppRelatedPushClient load(final String appId) throws Exception {
        final AppCredentialsEntity credentials = appCredentialsRepository.findFirstByAppId(appId).orElse(null);
        if (credentials == null) {
            logger.warn("AppCredentials does not exist for app: {}", appId);
            return null;
        }

        return createPushClient(credentials);
    }

    private AppRelatedPushClient createPushClient(final AppCredentialsEntity credentials) throws PushServerException {
        logger.info("Creating APNS, FCM, and HMS clients for app: {}", credentials.getAppId());

        final AppRelatedPushClient pushClient = new AppRelatedPushClient();
        pushClient.setAppCredentials(credentials);

        if (credentials.getApnsPrivateKey() != null) {
            final ApnsClient apnsClientProduction = pushSendingWorker.prepareApnsClient(credentials, ApnsEnvironment.PRODUCTION);
            pushClient.setApnsClientProduction(apnsClientProduction);
            final String environmentAppConfig = credentials.getApnsEnvironment();
            if ((ApnsEnvironment.DEVELOPMENT.getKey().equals(environmentAppConfig)) || configuration.isApnsUseDevelopment()) {
                final ApnsClient apnsClientDevelopment = pushSendingWorker.prepareApnsClient(credentials, ApnsEnvironment.DEVELOPMENT);
                pushClient.setApnsClientDevelopment(apnsClientDevelopment);
            }
        }

        if (credentials.getFcmPrivateKey() != null) {
            final FcmClient fcmClient = pushSendingWorker.prepareFcmClient(credentials.getFcmProjectId(), credentials.getFcmPrivateKey());
            pushClient.setFcmClient(fcmClient);
        }

        if (credentials.getHmsClientId() != null) {
            final HmsClient hmsClient = pushSendingWorker.prepareHmsClient(credentials);
            pushClient.setHmsClient(hmsClient);
        }

        return pushClient;
    }
}
