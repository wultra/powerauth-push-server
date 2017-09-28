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
package io.getlime.push.service.batch;

import io.getlime.push.repository.model.aggregate.UserDevice;
import io.getlime.push.service.batch.storage.ItemStorageSet;
import io.getlime.push.service.batch.storage.UserDeviceStorageSet;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

/**
 * Processor used in batch sending campaign
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Component
@StepScope
public class UserDeviceItemProcessor implements ItemProcessor<UserDevice, UserDevice> {

    private ItemStorageSet<UserDevice> itemStore = new UserDeviceStorageSet<>();

    /**
     * Decides if current userDevice is going to be processed to sending
     *
     * @param userDevice object used in sending campaign
     * @return userDevice
     */
    @Override
    public UserDevice process(UserDevice userDevice) {
        if (itemStore.exists(userDevice)) {
            return null;
        }
        itemStore.put(userDevice);
        return userDevice;
    }

}
