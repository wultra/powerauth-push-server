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
 * Interface that prescribes methods for userDevice storage
 *
 * @author Petr Dvorak, petr@wultra.com
 *
 * @param <T> Type of the stored record.
 */
public interface ItemStorageSet<T> {

    /**
     * Check if item exists in the set.
     * @param item Item to be checked.
     * @return True if set contains given item, false otherwise.
     */
    boolean exists(T item);

    /**
     * Put item in the set.
     * @param item Item.
     */
    void put(T item);

}
