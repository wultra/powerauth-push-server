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
package com.wultra.push.service.batch.storage;

import java.util.HashSet;
import java.util.Set;

/**
 * In memory storage (uses {@link HashSet} internally) for unique
 * {@link com.wultra.push.repository.model.aggregate.UserDevice} objects.
 *
 * @author Petr Dvorak, petr@wultra.com
 *
 * @param <T> Type of the stored record.
 */
public class UserDeviceStorageSet<T> implements ItemStorageSet<T> {

    private final Set<T> items = new HashSet<>();

    @Override
    public boolean exists(T item) {
        return items.contains(item);
    }

    @Override
    public void put(T item) {
        items.add(item);
    }
}