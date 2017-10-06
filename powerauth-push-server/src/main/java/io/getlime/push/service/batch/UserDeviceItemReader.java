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

import io.getlime.push.configuration.PushServiceConfiguration;
import io.getlime.push.repository.model.aggregate.UserDevice;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * Item reader that reads users' devices from the database.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Component
@StepScope
public class UserDeviceItemReader extends JpaPagingItemReader<UserDevice> {

    @Autowired
    public UserDeviceItemReader(EntityManagerFactory entityManagerFactory, PushServiceConfiguration configuration) {
        // Configure queries and reader
        this.setEntityManagerFactory(entityManagerFactory);
        this.setQueryString("select " +
                " new io.getlime.push.repository.model.aggregate.UserDevice(c.userId, d.id, d.activationId, c.campaignId, c.appId, d.platform, d.pushToken) " +
                " from PushCampaignUserEntity c, PushDeviceRegistrationEntity d " +
                " where c.appId = d.appId and c.userId = d.userId and c.campaignId = :campaignId");
        // Map parameters to query
        this.setPageSize(configuration.getCampaignBatchSize());
    }

    @Value("#{jobParameters['campaignId']}")
    public void setCampaignId(Long campaignId) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("campaignId", campaignId);
        this.setParameterValues(parameters);
    }
}

