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

package io.getlime.push.service.batch;

import io.getlime.push.errorhandling.exceptions.PushServerException;
import io.getlime.push.model.entity.PushMessageBody;
import io.getlime.push.model.enumeration.ApnsEnvironment;
import io.getlime.push.repository.PushCampaignRepository;
import io.getlime.push.repository.model.Platform;
import io.getlime.push.repository.model.PushCampaignEntity;
import io.getlime.push.repository.model.aggregate.UserDevice;
import io.getlime.push.repository.serialization.JsonSerialization;
import io.getlime.push.service.PushMessageSenderService;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Item writer that send notification to directed device and save message to database.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Component
@StepScope
public class UserDeviceItemWriter implements ItemWriter<UserDevice> {

    private final PushMessageSenderService pushMessageSenderService;
    private final PushCampaignRepository pushCampaignRepository;
    private final JsonSerialization jsonSerialization;

    // Non-autowired fields
    private final ConcurrentMap<Long, PushCampaignEntity> campaignMessageStorage = new ConcurrentHashMap<>();

    /**
     * Constructor with autowired dependencies.
     * @param pushMessageSenderService Push message sender service.
     * @param pushCampaignRepository Push campaign repository.
     * @param jsonSerialization Helper class for JSON serialization.
     */
    @Autowired
    public UserDeviceItemWriter(PushMessageSenderService pushMessageSenderService,
                                PushCampaignRepository pushCampaignRepository, JsonSerialization jsonSerialization) {
        this.pushMessageSenderService = pushMessageSenderService;
        this.pushCampaignRepository = pushCampaignRepository;
        this.jsonSerialization = jsonSerialization;
    }

    /**
     * Write list of user devices into the campaign sender.
     * @param list List of devices on which to send the campaign.
     * @throws Exception In case of business logic error.
     */
    @Override
    public void write(Chunk<? extends UserDevice> list) throws Exception {
        for (UserDevice device: list) {
            final Platform platform = device.getPlatform();
            final String environment = device.getEnvironment();
            final String token = device.getToken();
            final String userId = device.getUserId();
            final Long campaignId = device.getCampaignId();
            final Long deviceId = device.getDeviceId();
            final String activationId = device.getActivationId();

            final PushCampaignEntity campaign = campaignMessageStorage.computeIfAbsent(campaignId, k ->
                    pushCampaignRepository.findById(k).orElse(null));
            if (campaign == null) {
                throw new PushServerException("Campaign with entered ID: %s does not exist".formatted(campaign));
            }

            final PushMessageBody messageBody = jsonSerialization.deserializePushMessageBody(campaign.getMessage());

            // Send the push message using push sender service
            pushMessageSenderService.sendCampaignMessage(campaign.getAppCredentials().getAppId(), platform, environment, token, messageBody, userId, deviceId, activationId);
        }
    }
}
