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

import java.util.HashMap;
import java.util.Map;

public class CampaignStorageMap<K, V> implements ItemStorageMap<K, V> {
    private Map<K, V> mapStorage = new HashMap<>();

    @Override
    public V get(K key) {
        return mapStorage.get(key);
    }

    @Override
    public void put(K key, V value) {
        mapStorage.put(key, value);
    }

    @Override
    public boolean contains(K key) {
        return mapStorage.containsKey(key);
    }
}
