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

/**
 * Interface for generic item storage
 * @param <K> Key class for item storage.
 * @param <V> Value class for item storage.
 */
public interface ItemStorage<K,V> {

    /**
     * Get the value from the map.
     * @param key Key.
     * @return Value from the map for corresponding key.
     */
    V get(K key);

    /**
     * Put a value for provided key.
     * @param key Key.
     * @param value Value.
     */
    void put(K key, V value);

    /**
     * Clean value for provided key.
     * @param key Key.
     */
    void cleanByKey(K key);

}
