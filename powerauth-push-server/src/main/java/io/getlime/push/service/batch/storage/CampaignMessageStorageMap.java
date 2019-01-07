/*
 * Copyright 2016 Lime - HighTech Solutions s.r.o.
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

import io.getlime.push.model.entity.PushMessageBody;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * Simple to use class for storing campaigns. Simple in-memory storage cache for app credentials. Uses {@link ConcurrentHashMap}
 * as an underlying storage.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class CampaignMessageStorageMap implements ItemStorageMap<Long, PushMessageBody> {

    private ConcurrentMap<Long, PushMessageBody> mapStorage = new ConcurrentHashMap<>();

    @Override
    public PushMessageBody get(Long key) {
        return mapStorage.get(key);
    }

    @Override
    public void put(Long key, PushMessageBody value) {
        mapStorage.put(key, value);
    }

    @Override
    public boolean contains(Long key) {
        return mapStorage.containsKey(key);
    }

    @Override
    public void cleanAll() {
        mapStorage.clear();
    }

    @Override
    public void cleanByKey(Long key) {
        mapStorage.remove(key);
    }
}
