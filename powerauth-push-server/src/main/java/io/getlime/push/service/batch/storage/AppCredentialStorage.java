/*
 * Copyright 2016 Wultra s.r.o.
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
package io.getlime.push.service.batch.storage;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.getlime.push.service.AppRelatedPushClient;

import java.time.Duration;

/**
 * Simple in-memory storage cache for app credentials and push service clients.
 * Uses {@link Cache} as an underlying storage.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class AppCredentialStorage implements ItemStorage<String, AppRelatedPushClient> {

    private final Cache<String, AppRelatedPushClient> cache;

    /**
     * All-arg constructor.
     *
     * @param expireAfterWrite The length of time after an entry is created that it should be automatically removed.
     */
    public AppCredentialStorage(final Duration expireAfterWrite) {
         cache = Caffeine.newBuilder()
                .expireAfterWrite(expireAfterWrite)
                .build();
    }

    @Override
    public AppRelatedPushClient get(String key) {
        return cache.getIfPresent(key);
    }

    @Override
    public void put(String key, AppRelatedPushClient value) {
        cache.put(key, value);
    }

    @Override
    public void cleanByKey(final String key) {
        cache.invalidate(key);
    }
}
