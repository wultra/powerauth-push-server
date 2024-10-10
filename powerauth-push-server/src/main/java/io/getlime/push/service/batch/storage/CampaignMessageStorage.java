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

import io.getlime.push.repository.model.PushCampaignEntity;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * Simple to use class for storing campaigns. Simple in-memory storage cache for app credentials. Uses {@link ConcurrentHashMap}
 * as an underlying storage.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class CampaignMessageStorage implements ItemStorage<Long, PushCampaignEntity> {

    private final ConcurrentMap<Long, PushCampaignEntity> mapStorage = new ConcurrentHashMap<>();

    @Override
    public PushCampaignEntity get(Long key) {
        return mapStorage.get(key);
    }

    @Override
    public void put(Long key, PushCampaignEntity value) {
        mapStorage.put(key, value);
    }

    @Override
    public void cleanByKey(Long key) {
        mapStorage.remove(key);
    }
}
