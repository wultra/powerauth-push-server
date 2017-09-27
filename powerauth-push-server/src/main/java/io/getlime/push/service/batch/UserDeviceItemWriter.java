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

import io.getlime.push.repository.PushCampaignRepository;
import io.getlime.push.repository.model.PushCampaignEntity;
import io.getlime.push.repository.model.aggregate.UserDevice;
import io.getlime.push.service.PushMessageSenderService;
import io.getlime.push.service.batch.storage.CampaignStorageMap;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
@StepScope
public class UserDeviceItemWriter implements ItemWriter<UserDevice>, InitializingBean {

    private PushMessageSenderService pushMessageSenderService;
    private CampaignStorageMap<Long, PushCampaignEntity> storage;
    private PushCampaignRepository pushCampaignRepository;
    Map<Integer, Integer> map = new HashMap<>();

    @Autowired
    public UserDeviceItemWriter(PushMessageSenderService pushMessageSenderService,
                                PushCampaignRepository pushCampaignRepository) {
        this.pushMessageSenderService = pushMessageSenderService;
        this.pushCampaignRepository = pushCampaignRepository;
    }

    @Override
    public void write(List<? extends UserDevice> list) throws Exception {
        for (UserDevice device: list) {
            String platform = device.getPlatform();
            String token = device.getToken();
            Long appId = device.getAppId();
            Long campaignId = device.getCampaignId();
            if (!storage.contains(campaignId)){
                storage.put(campaignId, pushCampaignRepository.findOne(campaignId));
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }

}
