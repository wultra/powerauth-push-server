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

import com.eatthepath.pushy.apns.ApnsClient;
import com.github.benmanes.caffeine.cache.CacheLoader;
import io.getlime.push.errorhandling.exceptions.PushServerException;
import io.getlime.push.repository.AppCredentialsRepository;
import io.getlime.push.repository.model.AppCredentialsEntity;
import io.getlime.push.service.fcm.FcmClient;
import io.getlime.push.service.hms.HmsClient;
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

    @Override
    public AppRelatedPushClient reload(final String key, final AppRelatedPushClient oldValue) throws Exception {
        final LocalDateTime lastUpdated = appCredentialsRepository.findFirstByAppId(key).map(AppCredentialsEntity::getTimestampLastUpdated).orElse(null);
        if (Objects.equals(oldValue.getAppCredentials().getTimestampLastUpdated(), lastUpdated)) {
            logger.debug("LastUpdated is same for app: {}", key);
            return oldValue;
        }
        logger.debug("LastUpdated differs for app: {}", key);
        return load(key);
    }

    @Override
    public AppRelatedPushClient load(final String key) throws Exception {
        final AppCredentialsEntity credentials = appCredentialsRepository.findFirstByAppId(key).orElse(null);
        if (credentials == null) {
            logger.debug("AppCredentials does not exist for app: {}", key);
            return null;
        }

        logger.info("Creating APNS, FCM, and HMS clients for app: {}", key);
        return createPushClient(credentials);
    }

    private AppRelatedPushClient createPushClient(final AppCredentialsEntity credentials) throws PushServerException {
        final AppRelatedPushClient pushClient = new AppRelatedPushClient();
        if (credentials.getIosPrivateKey() != null) {
            final ApnsClient apnsClient = pushSendingWorker.prepareApnsClient(credentials);
            pushClient.setApnsClient(apnsClient);
        }
        if (credentials.getAndroidPrivateKey() != null) {
            final FcmClient fcmClient = pushSendingWorker.prepareFcmClient(credentials.getAndroidProjectId(), credentials.getAndroidPrivateKey());
            pushClient.setFcmClient(fcmClient);
        }
        if (credentials.getHmsClientId() != null) {
            final HmsClient hmsClient = pushSendingWorker.prepareHmsClient(credentials);
            pushClient.setHmsClient(hmsClient);
        }
        pushClient.setAppCredentials(credentials);
        return pushClient;
    }
}
