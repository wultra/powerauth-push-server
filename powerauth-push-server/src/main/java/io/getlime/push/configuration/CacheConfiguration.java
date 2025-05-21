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

package io.getlime.push.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.getlime.push.service.AppRelatedPushClient;
import io.getlime.push.service.AppRelatedPushClientCacheLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;


/**
 * Cache configuration.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
@Configuration
@Slf4j
public class CacheConfiguration {

    /**
     * Configure cache for {@link AppRelatedPushClient}.
     *
     * @return cache for AppRelatedPushClient
     */
    @Bean
    public LoadingCache<String, AppRelatedPushClient> appRelatedPushClientCache(
            @Value("${powerauth.push.service.clients.cache.refreshAfterWrite}") final Duration refreshAfterWrite,
            final AppRelatedPushClientCacheLoader cacheLoader) {

        logger.info("Initializing AppRelatedPushClient cache with refreshAfterWrite={}", refreshAfterWrite);
        return Caffeine.newBuilder()
                .refreshAfterWrite(refreshAfterWrite)
                .build(cacheLoader);
    }

}
