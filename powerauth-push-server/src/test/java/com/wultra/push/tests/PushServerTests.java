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

package com.wultra.push.tests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wultra.core.rest.model.base.response.ObjectResponse;
import com.wultra.push.api.PowerAuthTestClient;
import com.wultra.push.client.PushServerClient;
import com.wultra.push.client.PushServerClientException;
import com.wultra.push.client.PushServerTestClientFactory;
import com.wultra.push.configuration.PushServerAppCredentialConfiguration;
import com.wultra.push.model.base.PagedResponse;
import com.wultra.push.model.entity.*;
import com.wultra.push.model.enumeration.ApnsEnvironment;
import com.wultra.push.model.enumeration.MobilePlatform;
import com.wultra.push.model.enumeration.Mode;
import com.wultra.push.model.request.CreateDeviceForActivationsRequest;
import com.wultra.push.model.request.CreateDeviceRequest;
import com.wultra.push.model.response.*;
import com.wultra.push.repository.PushDeviceRepository;
import com.wultra.push.repository.model.PushDeviceRegistrationEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Class used for testing client-server methods
 * All tests cover each method from {@link PushServerClient}.
 * Methods are named with suffix "Test" and are just compared with expected server responses.
 * Using in memory H2 create/drop database.
 *
 * @author Martin Tupy, martin.tupy.work@gmail.com
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
@Sql(scripts = "classpath:/sql/batch-init-h2.sql")
class PushServerTests {

    private static final String MOCK_PUSH_TOKEN = "1234567890987654321234567890";
    private static final String MOCK_PUSH_TOKEN_2 = "9876543212345678901234567890";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private PushDeviceRepository pushDeviceRepository;

    @Autowired
    private PushServerTestClientFactory testClientFactory;

    @Autowired
    private PushServerAppCredentialConfiguration appCredentialConfig;

    private PushServerClient pushServerClient;

    private PowerAuthTestClient powerAuthTestClient;

    @LocalServerPort
    private int port;

    @Value("${powerauth.push.service.fcm.sendMessageUrl}")
    private String fcmUrlForTests;

    @BeforeEach
    void setUp() throws Exception {
        pushServerClient = testClientFactory.createPushServerClient("http://localhost:" + port);
        powerAuthTestClient = testClientFactory.createPowerAuthTestClient();
        appCredentialConfig.configure(powerAuthTestClient.getApplicationId());
    }

    @Test
    void getServiceStatusTest() throws Exception {
        ObjectResponse<ServiceStatusResponse> actual = pushServerClient.getServiceStatus();
        String body = restTemplate.getForEntity("http://localhost:" + port + "/push/service/status", String.class).getBody();
        assertNotNull(body);
        final ObjectResponse<ServiceStatusResponse> expected = mapper.readValue(body, new TypeReference<>() {});
        assertEquals(expected.getStatus(), actual.getStatus());
        assertEquals(expected.getResponseObject().getApplicationDisplayName(), actual.getResponseObject().getApplicationDisplayName());
        assertEquals(expected.getResponseObject().getApplicationEnvironment(), actual.getResponseObject().getApplicationEnvironment());
        assertEquals(expected.getResponseObject().getApplicationName(), actual.getResponseObject().getApplicationName());
    }

    @Test
    void createDeviceWithoutActivationIDTest() {
        CreateDeviceRequest request = CreateDeviceRequest.builder()
                .appId(powerAuthTestClient.getActivationId())
                .token(MOCK_PUSH_TOKEN)
                .platform(MobilePlatform.APNS)
                .environment(ApnsEnvironment.DEVELOPMENT)
                .build();
        assertThrows(PushServerClientException.class, () ->
            pushServerClient.createDevice(request));
    }

    @Test
    void createDeviceWithActivationIDTest() throws Exception {
        CreateDeviceRequest request = CreateDeviceRequest.builder()
                .appId(powerAuthTestClient.getApplicationId())
                .token(MOCK_PUSH_TOKEN)
                .platform(MobilePlatform.APNS)
                .environment(ApnsEnvironment.DEVELOPMENT)
                .activationId(powerAuthTestClient.getActivationId())
                .build();
        boolean result = pushServerClient.createDevice(request);
        assertTrue(result);
        List<PushDeviceRegistrationEntity> devices = pushDeviceRepository.findByAppCredentialsAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        assertEquals(1, devices.size());
        pushDeviceRepository.deleteAll(devices);
    }

    @Test
    void deleteDeviceTest() throws Exception {
        CreateDeviceRequest request = CreateDeviceRequest.builder()
                .appId(powerAuthTestClient.getApplicationId())
                .token(MOCK_PUSH_TOKEN)
                .platform(MobilePlatform.APNS)
                .environment(ApnsEnvironment.DEVELOPMENT)
                .activationId(powerAuthTestClient.getActivationId())
                .build();
        boolean result = pushServerClient.createDevice(request);
        assertTrue(result);
        List<PushDeviceRegistrationEntity> devices = pushDeviceRepository.findByAppCredentialsAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        assertEquals(1, devices.size());
        boolean result2 = pushServerClient.deleteDevice(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        assertTrue(result2);
        List<PushDeviceRegistrationEntity> devices2 = pushDeviceRepository.findByAppCredentialsAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        assertEquals(0, devices2.size());
    }

    @Test
    void testFcmUrlConfiguredForTests() {
        assertEquals("http://localhost:" + port + "/mockfcm/message:send", fcmUrlForTests);
    }

    @Test
    void updateDeviceStatusTest() throws Exception {
        CreateDeviceRequest request = CreateDeviceRequest.builder()
                .appId(powerAuthTestClient.getApplicationId())
                .token(MOCK_PUSH_TOKEN)
                .platform(MobilePlatform.APNS)
                .environment(ApnsEnvironment.DEVELOPMENT)
                .activationId(powerAuthTestClient.getActivationId())
                .build();
        boolean result = pushServerClient.createDevice(request);
        assertTrue(result);
        List<PushDeviceRegistrationEntity> devices = pushDeviceRepository.findByAppCredentialsAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        assertEquals(1, devices.size());
        powerAuthTestClient.blockActivation(powerAuthTestClient.getActivationId());
        boolean result2 = pushServerClient.updateDeviceStatus(powerAuthTestClient.getActivationId());
        assertTrue(result2);
        List<PushDeviceRegistrationEntity> devices2 = pushDeviceRepository.findByAppCredentialsAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        assertEquals(1, devices2.size());
        assertFalse(devices2.get(0).getActive());
        powerAuthTestClient.unblockActivation(powerAuthTestClient.getActivationId());
        boolean result3 = pushServerClient.updateDeviceStatus(powerAuthTestClient.getActivationId());
        assertTrue(result3);
        List<PushDeviceRegistrationEntity> devices3 = pushDeviceRepository.findByAppCredentialsAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        assertEquals(1, devices3.size());
        assertTrue(devices3.get(0).getActive());
        boolean result4 = pushServerClient.deleteDevice(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        assertTrue(result4);
    }

    @Test
    void sendPushMessageTest() throws Exception {
        CreateDeviceRequest request = CreateDeviceRequest.builder()
                .appId(powerAuthTestClient.getApplicationId())
                .token(MOCK_PUSH_TOKEN)
                .platform(MobilePlatform.FCM)
                .activationId(powerAuthTestClient.getActivationId())
                .build();
        final boolean result = pushServerClient.createDevice(request);
        assertTrue(result);

        final PushMessageAttributes attributes = new PushMessageAttributes();
        attributes.setSilent(false);
        attributes.setPersonal(true);

        final PushMessageBody pushMessageBody = new PushMessageBody();
        pushMessageBody.setTitle("Balance update");
        pushMessageBody.setBody("Your balance is now $745.00");
        pushMessageBody.setBadge(3);
        pushMessageBody.setSound("riff.wav");
        pushMessageBody.setCategory("balance-update");
        pushMessageBody.setCollapseKey("balance-update");
        pushMessageBody.setValidUntil(Instant.now());
        pushMessageBody.setExtras(Map.of("_comment", "Any custom data."));

        final PushMessage pushMessage = new PushMessage();
        pushMessage.setUserId(PushServerTestClientFactory.TEST_USER_ID);
        pushMessage.setActivationId(powerAuthTestClient.getActivationId());
        pushMessage.setAttributes(attributes);
        pushMessage.setBody(pushMessageBody);

        final ObjectResponse<PushMessageSendResult> actual = pushServerClient.sendPushMessage(powerAuthTestClient.getApplicationId(), Mode.SYNCHRONOUS, pushMessage);
        assertEquals("OK", actual.getStatus());

        final List<PushDeviceRegistrationEntity> devices = pushDeviceRepository.findByAppCredentialsAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        pushDeviceRepository.deleteAll(devices);
    }


    @Test
    void sendPushMessageBatchTest() throws Exception {
        CreateDeviceRequest request = CreateDeviceRequest.builder()
                .appId(powerAuthTestClient.getApplicationId())
                .token(MOCK_PUSH_TOKEN)
                .platform(MobilePlatform.FCM)
                .activationId(powerAuthTestClient.getActivationId())
                .build();
        boolean result = pushServerClient.createDevice(request);
        assertTrue(result);

        final PushMessageBody pushMessageBody = new PushMessageBody();
        pushMessageBody.setTitle("Balance update");
        pushMessageBody.setBody("Your balance is now $745.00");
        pushMessageBody.setBadge(3);
        pushMessageBody.setSound("riff.wav");
        pushMessageBody.setCategory("balance-update");
        pushMessageBody.setCollapseKey("balance-update");
        pushMessageBody.setValidUntil(Instant.now());
        pushMessageBody.setExtras(Map.of("_comment", "Any custom data."));

        final PushMessageAttributes attributes = new PushMessageAttributes();
        attributes.setSilent(false);
        attributes.setPersonal(true);

        final PushMessage pushMessage = new PushMessage();
        pushMessage.setUserId(PushServerTestClientFactory.TEST_USER_ID);
        pushMessage.setActivationId(powerAuthTestClient.getActivationId());
        pushMessage.setAttributes(attributes);
        pushMessage.setBody(pushMessageBody);

        final List<PushMessage> batch = List.of(pushMessage);

        final ObjectResponse<PushMessageSendResult> actual = pushServerClient.sendPushMessageBatch(powerAuthTestClient.getApplicationId(), Mode.SYNCHRONOUS, batch);
        assertEquals("OK", actual.getStatus());

        final List<PushDeviceRegistrationEntity> devices = pushDeviceRepository.findByAppCredentialsAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        pushDeviceRepository.deleteAll(devices);
    }

    @Test
    void createCampaignTest() throws Exception {
        CreateDeviceRequest request = CreateDeviceRequest.builder()
                .appId(powerAuthTestClient.getApplicationId())
                .token(MOCK_PUSH_TOKEN)
                .platform(MobilePlatform.FCM)
                .activationId(powerAuthTestClient.getActivationId())
                .build();
        boolean result = pushServerClient.createDevice(request);
        assertTrue(result);
        final ObjectResponse<CreateCampaignResponse> actual = createCampaign();
        assertEquals("OK", actual.getStatus());
        List<PushDeviceRegistrationEntity> devices = pushDeviceRepository.findByAppCredentialsAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        pushDeviceRepository.deleteAll(devices);
    }

    @Test
    void deleteCampaignTest() throws Exception {
        boolean actual = pushServerClient.deleteCampaign(2L);
        assertTrue(actual);
    }

    @Test
    void getListOfCampaignsTest() throws Exception{
        ObjectResponse<ListOfCampaignsResponse> actual = pushServerClient.getListOfCampaigns(true);
        String body = restTemplate.getForEntity("http://localhost:" + port + "/push/campaign/list?all=true", String.class).getBody();
        assertNotNull(body);
        final ObjectResponse<ListOfCampaignsResponse> expected = mapper.readValue(body, new TypeReference<>() {});
        assertEquals(expected.getStatus(), actual.getStatus());
        assertTrue(actual.getResponseObject().containsAll(expected.getResponseObject()));
    }

    @Test
    void getCampaignTest() throws Exception{
        ObjectResponse<CampaignResponse> actual = pushServerClient.getCampaign(1L);
        String body = restTemplate.getForEntity("http://localhost:" + port + "/push/campaign/1/detail", String.class).getBody();
        assertNotNull(body);
        final ObjectResponse<CampaignResponse> expected = mapper.readValue(body, new TypeReference<>() {});
        assertEquals(expected.getStatus(), actual.getStatus());
        assertEquals(expected.getResponseObject(), actual.getResponseObject());
    }

    @Test
    void addUsersToCampaignTest() throws Exception {
        ListOfUsers listOfUsers = new ListOfUsers();
        listOfUsers.addAll(Arrays.asList("1234567890", "1234567891", "1234567893"));
        boolean actual = pushServerClient.addUsersToCampaign(1L, listOfUsers);
        assertTrue(actual);
    }

    @Test
    void getListOfUsersFromCampaignTest() throws Exception {
        PagedResponse<ListOfUsersFromCampaignResponse> actual = pushServerClient.getListOfUsersFromCampaign(10L, 0, 3);
        String body = restTemplate.getForEntity("http://localhost:" + port + "/push/campaign/10/user/list?page=0&size=3", String.class).getBody();
        assertNotNull(body);
        final PagedResponse<ListOfUsersFromCampaignResponse> expected = mapper.readValue(body, new TypeReference<>() {});
        assertEquals(expected.getResponseObject(), actual.getResponseObject());
        assertEquals(expected.getStatus(), actual.getStatus());
        assertEquals(expected.getPage(), actual.getPage());
    }

    @Test
    void deleteUsersFromCampaignTest() throws Exception {
        ListOfUsers listOfUsers = new ListOfUsers();
        listOfUsers.addAll(Arrays.asList("1234567890", "1234567891", "1234567893"));
        boolean actual = pushServerClient.deleteUsersFromCampaign(3L, listOfUsers);
        assertTrue(actual);
    }

    @Test
    void sendTestingCampaignTest() throws Exception {
        CreateDeviceRequest request = CreateDeviceRequest.builder()
                .appId(powerAuthTestClient.getApplicationId())
                .token(MOCK_PUSH_TOKEN)
                .platform(MobilePlatform.FCM)
                .activationId(powerAuthTestClient.getActivationId())
                .build();
        boolean result = pushServerClient.createDevice(request);
        assertTrue(result);

        final Long campaignId = createCampaign().getResponseObject().getId();
        boolean actual = pushServerClient.sendTestCampaign(campaignId, PushServerTestClientFactory.TEST_USER_ID);
        assertTrue(actual);
    }

    @Test
    void sendCampaignTest() throws Exception {
        final Long campaignId = createCampaign().getResponseObject().getId();
        boolean result = pushServerClient.sendCampaign(campaignId);
        assertTrue(result);
    }

    @Test
    void createDeviceWithMultipleActivationsTest() {
        assertThrows(PushServerClientException.class, () -> {
            List<String> activationIds = new ArrayList<>();
            activationIds.add(powerAuthTestClient.getActivationId());
            activationIds.add(powerAuthTestClient.getActivationId2());
            CreateDeviceForActivationsRequest request = CreateDeviceForActivationsRequest.builder()
                    .appId(powerAuthTestClient.getApplicationId())
                    .token(MOCK_PUSH_TOKEN)
                    .platform(MobilePlatform.APNS)
                    .environment(ApnsEnvironment.DEVELOPMENT)
                    .build();
            request.getActivationIds().addAll(activationIds);
            pushServerClient.createDeviceForActivations(request);
        });
    }

    @Test
    void createDeviceSameActivationSamePushTokenUpdatesTest() throws PushServerClientException {
        // This test tests refresh of a device registration
        CreateDeviceRequest request = CreateDeviceRequest.builder()
                .appId(powerAuthTestClient.getApplicationId())
                .token(MOCK_PUSH_TOKEN)
                .platform(MobilePlatform.FCM)
                .activationId(powerAuthTestClient.getActivationId())
                .build();
        boolean actual = pushServerClient.createDevice(request);
        assertTrue(actual);
        List<PushDeviceRegistrationEntity> devices = pushDeviceRepository.findByAppCredentialsAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        assertEquals(1, devices.size());
        Long rowId = devices.get(0).getId();
        boolean actual2 = pushServerClient.createDevice(request);
        assertTrue(actual2);
        List<PushDeviceRegistrationEntity> devices2 = pushDeviceRepository.findByAppCredentialsAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        assertEquals(1, devices2.size());
        assertEquals(rowId, devices2.get(0).getId());
        pushDeviceRepository.deleteAll(devices2);
    }

    @Test
    void createDeviceSameActivationDifferentPushTokenTest() throws PushServerClientException {
        // This test tests change of Push Token - new token has been issued by Google or Apple and the device registers for same activation
        CreateDeviceRequest request = CreateDeviceRequest.builder()
                .appId(powerAuthTestClient.getApplicationId())
                .token(MOCK_PUSH_TOKEN)
                .platform(MobilePlatform.FCM)
                .activationId(powerAuthTestClient.getActivationId())
                .build();
        boolean actual = pushServerClient.createDevice(request);
        assertTrue(actual);
        List<PushDeviceRegistrationEntity> devices = pushDeviceRepository.findByAppCredentialsAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        Long rowId = devices.get(0).getId();
        request.setToken(MOCK_PUSH_TOKEN_2);
        boolean actual2 = pushServerClient.createDevice(request);
        assertTrue(actual2);
        // The push token must change, however row ID stays the same
        List<PushDeviceRegistrationEntity> devices1 = pushDeviceRepository.findByAppCredentialsAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        List<PushDeviceRegistrationEntity> devices2 = pushDeviceRepository.findByAppCredentialsAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN_2);
        assertEquals(0, devices1.size());
        assertEquals(1, devices2.size());
        assertEquals(rowId, devices2.get(0).getId());
        pushDeviceRepository.deleteAll(devices2);
    }

    @Test
    void createDeviceDifferentActivationSamePushTokenTest() throws PushServerClientException {
        // This test tests change of activation - user deleted the activation and created a new one, the push token is the same
        CreateDeviceRequest request = CreateDeviceRequest.builder()
                .appId(powerAuthTestClient.getApplicationId())
                .token(MOCK_PUSH_TOKEN)
                .platform(MobilePlatform.FCM)
                .activationId(powerAuthTestClient.getActivationId())
                .build();
        boolean actual = pushServerClient.createDevice(request);
        assertTrue(actual);
        List<PushDeviceRegistrationEntity> devices = pushDeviceRepository.findByAppCredentialsAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        Long rowId = devices.get(0).getId();
        request.setActivationId(powerAuthTestClient.getActivationId2());
        boolean actual2 = pushServerClient.createDevice(request);
        assertTrue(actual2);
        List<PushDeviceRegistrationEntity> devices2 = pushDeviceRepository.findByAppCredentialsAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        assertEquals(1, devices2.size());
        assertEquals(rowId, devices2.get(0).getId());
        pushDeviceRepository.deleteAll(devices2);
    }

    private ObjectResponse<CreateCampaignResponse> createCampaign() throws Exception {
        final Map<String, Object> extras = new HashMap<>();
        extras.put("_comment", "Any custom data.");

        final PushMessageBody pushMessageBody = new PushMessageBody();
        pushMessageBody.setTitle("Balance update");
        pushMessageBody.setBody("Your balance is now $745.00");
        pushMessageBody.setBadge(3);
        pushMessageBody.setSound("riff.wav");
        pushMessageBody.setCategory("balance-update");
        pushMessageBody.setCollapseKey("balance-update");
        pushMessageBody.setValidUntil(Instant.now());
        pushMessageBody.setExtras(extras);
        return pushServerClient.createCampaign(powerAuthTestClient.getApplicationId(), pushMessageBody);
    }

}
