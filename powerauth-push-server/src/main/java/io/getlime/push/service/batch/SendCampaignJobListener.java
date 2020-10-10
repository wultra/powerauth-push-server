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

import io.getlime.push.repository.PushCampaignRepository;
import io.getlime.push.repository.model.PushCampaignEntity;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

@Component
@JobScope
public class SendCampaignJobListener implements JobExecutionListener {

    private final PushCampaignRepository pushCampaignRepository;

    @Value("#{jobParameters['campaignId']}")
    private Long campaignId;

    @Autowired
    public SendCampaignJobListener(PushCampaignRepository pushCampaignRepository) {
        this.pushCampaignRepository = pushCampaignRepository;
    }

    @Override
    public void beforeJob(@NonNull JobExecution jobExecution) {
        PushCampaignEntity campaign = findPushCampaignById(campaignId);
        campaign.setTimestampSent(new Date());
        pushCampaignRepository.save(campaign);
    }

    @Override
    public void afterJob(@NonNull JobExecution jobExecution) {
        PushCampaignEntity campaign = findPushCampaignById(campaignId);
        campaign.setTimestampCompleted(new Date());
        campaign.setSent(true);
        pushCampaignRepository.save(campaign);
    }

    /**
     * Find push campaign entity by ID.
     * @param campaignId Campaign ID.
     * @return Push campaign entity.
     */
    private PushCampaignEntity findPushCampaignById(Long campaignId) {
        final Optional<PushCampaignEntity> pushCampaignEntityOptional = pushCampaignRepository.findById(campaignId);
        if (!pushCampaignEntityOptional.isPresent()) {
            throw new IllegalArgumentException("Campaign with entered ID does not exist");
        }
        return pushCampaignEntityOptional.get();
    }
}
