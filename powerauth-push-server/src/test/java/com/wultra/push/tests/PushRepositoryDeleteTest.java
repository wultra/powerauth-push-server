/*
 * Copyright 2026 Wultra s.r.o.
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
package com.wultra.push.tests;

import com.wultra.push.repository.AppCredentialsRepository;
import com.wultra.push.repository.PushCampaignRepository;
import com.wultra.push.repository.PushCampaignUserRepository;
import com.wultra.push.repository.PushDeviceRepository;
import com.wultra.push.repository.model.AppCredentialsEntity;
import com.wultra.push.repository.model.Platform;
import com.wultra.push.repository.model.PushCampaignEntity;
import com.wultra.push.repository.model.PushCampaignUserEntity;
import com.wultra.push.repository.model.PushDeviceRegistrationEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PushRepositoryDeleteTest {

    @Autowired
    private PushCampaignRepository pushCampaignRepository;

    @Autowired
    private PushCampaignUserRepository pushCampaignUserRepository;

    @Autowired
    private PushDeviceRepository pushDeviceRepository;

    @Autowired
    private AppCredentialsRepository appCredentialsRepository;

    @Test
    void testDeleteByCampaignId() {
        // Prepare data
        AppCredentialsEntity app = createAppCredentials("test_app_1");
        PushCampaignEntity campaign = createCampaign(app);
        
        createCampaignUser(campaign.getId(), "user1");
        createCampaignUser(campaign.getId(), "user2");
        createCampaignUser(campaign.getId(), "user3");
        
        // Verify data exists
        assertEquals(3, pushCampaignUserRepository.findAllByCampaignId(campaign.getId(), Pageable.unpaged()).size());
        
        // Execute delete
        pushCampaignUserRepository.deleteByCampaignId(campaign.getId());
        
        // Verify deletion
        assertEquals(0, pushCampaignUserRepository.findAllByCampaignId(campaign.getId(), Pageable.unpaged()).size());
    }

    @Test
    void testDeleteByCampaignIdAndUserId() {
        // Prepare data
        AppCredentialsEntity app = createAppCredentials("test_app_2");
        PushCampaignEntity campaign = createCampaign(app);
        
        createCampaignUser(campaign.getId(), "user1");
        createCampaignUser(campaign.getId(), "user2");
        
        // Verify data exists
        assertEquals(2, pushCampaignUserRepository.findAllByCampaignId(campaign.getId(), Pageable.unpaged()).size());
        
        // Execute delete for specific user
        pushCampaignUserRepository.deleteByCampaignIdAndUserId(campaign.getId(), "user1");
        
        // Verify deletion
        assertEquals(1, pushCampaignUserRepository.findAllByCampaignId(campaign.getId(), Pageable.unpaged()).size());
        assertNotNull(pushCampaignUserRepository.findFirstByUserIdAndCampaignId("user2", campaign.getId()));
        assertNull(pushCampaignUserRepository.findFirstByUserIdAndCampaignId("user1", campaign.getId()));
    }

    @Test
    void testDeleteAllByAppCredentialsIdAndPushToken() {
        // Prepare data
        AppCredentialsEntity app = createAppCredentials("test_app_3");
        String pushToken = "test_token_123";
        
        createDevice(app, pushToken, "user1");
        createDevice(app, pushToken, "user2"); // Same token, different user (edge case)
        
        // Verify data exists
        List<PushDeviceRegistrationEntity> devices = pushDeviceRepository.findByAppCredentialsAppIdAndPushToken(app.getAppId(), pushToken);
        assertEquals(2, devices.size());
        
        // Execute delete
        pushDeviceRepository.deleteAllByAppCredentialsIdAndPushToken(app.getId(), pushToken);
        
        // Verify deletion
        List<PushDeviceRegistrationEntity> remainingDevices = pushDeviceRepository.findByAppCredentialsAppIdAndPushToken(app.getAppId(), pushToken);
        assertEquals(0, remainingDevices.size());
    }

    // Helper methods
    
    private AppCredentialsEntity createAppCredentials(String appId) {
        AppCredentialsEntity app = new AppCredentialsEntity();
        app.setAppId(appId);
        return appCredentialsRepository.save(app);
    }

    private PushCampaignEntity createCampaign(AppCredentialsEntity app) {
        PushCampaignEntity campaign = new PushCampaignEntity();
        campaign.setAppCredentials(app);
        campaign.setMessage("Test message");
        campaign.setSent(false);
        campaign.setTimestampCreated(new Date());
        return pushCampaignRepository.save(campaign);
    }

    private void createCampaignUser(Long campaignId, String userId) {
        PushCampaignUserEntity user = new PushCampaignUserEntity();
        user.setCampaignId(campaignId);
        user.setUserId(userId);
        user.setTimestampCreated(new Date());
        pushCampaignUserRepository.save(user);
    }

    private void createDevice(AppCredentialsEntity app, String token, String userId) {
        PushDeviceRegistrationEntity device = new PushDeviceRegistrationEntity();
        device.setAppCredentials(app);
        device.setPushToken(token);
        device.setUserId(userId);
        device.setActivationId("act_" + userId);
        device.setTimestampLastRegistered(new Date());
        device.setPlatform(Platform.APNS);
        device.setActive(true);
        pushDeviceRepository.save(device);
    }
}
