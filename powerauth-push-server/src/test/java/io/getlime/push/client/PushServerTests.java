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

package io.getlime.push.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.push.model.base.PagedResponse;
import io.getlime.push.model.entity.*;
import io.getlime.push.model.request.CreateCampaignRequest;
import io.getlime.push.model.request.SendPushMessageBatchRequest;
import io.getlime.push.model.request.SendPushMessageRequest;
import io.getlime.push.model.response.*;
import io.getlime.push.repository.AppCredentialsRepository;
import io.getlime.push.repository.PushDeviceRepository;
import io.getlime.push.repository.model.AppCredentialsEntity;
import io.getlime.push.repository.model.PushDeviceRegistrationEntity;
import io.getlime.push.shared.PowerAuthTestClient;
import io.getlime.push.shared.PushServerTestClientFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
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
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PushServerTests {

    private static final String MOCK_PUSH_TOKEN = "1234567890987654321234567890";
    private static final String MOCK_PUSH_TOKEN_2 = "9876543212345678901234567890";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private AppCredentialsRepository appCredentialsRepository;

    @Autowired
    private PushDeviceRepository pushDeviceRepository;

    @Autowired
    private PushServerTestClientFactory testClientFactory;

    @MockBean
    private PushServerClient pushServerClient;

    @MockBean
    private PowerAuthTestClient powerAuthTestClient;

    @LocalServerPort
    private int port;

    @Value("${powerauth.service.url}")
    private String powerAuthRestUrl;

    @Value("${powerauth.push.service.fcm.sendMessageUrl}")
    private String fcmUrlForTests;

    @Before
    public void setUp() throws Exception {
        pushServerClient = testClientFactory.createPushServerClient("http://localhost:" + port);
        powerAuthTestClient = testClientFactory.createPowerAuthTestClient();
        AppCredentialsEntity testCredentials = new AppCredentialsEntity();
        testCredentials.setAppId(powerAuthTestClient.getApplicationId());
        testCredentials.setAndroidProjectId("test-project");
        testCredentials.setAndroidPrivateKey(new byte[128]);
        appCredentialsRepository.save(testCredentials);
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void getServiceStatusTest() throws Exception {
        ObjectResponse<ServiceStatusResponse> actual = pushServerClient.getServiceStatus();
        String body = restTemplate.getForEntity("http://localhost:" + port + "/push/service/status", String.class).getBody();
        assertNotNull(body);
        ObjectResponse<ServiceStatusResponse> expected = mapper.readValue(body, new TypeReference<ObjectResponse<ServiceStatusResponse>>() {});
        assertThat(actual.getStatus()).isEqualTo(expected.getStatus());
        assertThat(actual.getResponseObject().getApplicationDisplayName()).isEqualTo(expected.getResponseObject().getApplicationDisplayName());
        assertThat(actual.getResponseObject().getApplicationEnvironment()).isEqualTo(expected.getResponseObject().getApplicationEnvironment());
        assertThat(actual.getResponseObject().getApplicationName()).isEqualTo(expected.getResponseObject().getApplicationName());
    }

    @Test(expected = PushServerClientException.class)
    public void createDeviceWithoutActivationIDTest() throws Exception {
        pushServerClient.createDevice(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN, MobilePlatform.iOS);
    }

    @Test
    public void createDeviceWithActivationIDTest() throws Exception {
        boolean result = pushServerClient.createDevice(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN, MobilePlatform.iOS, powerAuthTestClient.getActivationId());
        assertTrue(result);
        List<PushDeviceRegistrationEntity> devices = pushDeviceRepository.findByAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        assertEquals(1, devices.size());
        devices.forEach(pushDeviceRepository::delete);
    }

    @Test
    public void deleteDeviceTest() throws Exception {
        boolean result = pushServerClient.createDevice(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN, MobilePlatform.iOS, powerAuthTestClient.getActivationId());
        assertTrue(result);
        List<PushDeviceRegistrationEntity> devices = pushDeviceRepository.findByAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        assertEquals(1, devices.size());
        boolean result2 = pushServerClient.deleteDevice(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        assertTrue(result2);
        List<PushDeviceRegistrationEntity> devices2 = pushDeviceRepository.findByAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        assertEquals(0, devices2.size());
    }

    @Test
    public void testFcmUrlConfiguredForTests() {
        assertEquals("http://localhost:" + port + "/mockfcm/message:send", fcmUrlForTests);
    }


    @Test
    public void updateDeviceStatusTest() throws Exception {
        boolean result = pushServerClient.createDevice(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN, MobilePlatform.iOS, powerAuthTestClient.getActivationId());
        assertTrue(result);
        List<PushDeviceRegistrationEntity> devices = pushDeviceRepository.findByAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        assertEquals(1, devices.size());
        powerAuthTestClient.blockActivation(powerAuthTestClient.getActivationId());
        boolean result2 = pushServerClient.updateDeviceStatus(powerAuthTestClient.getActivationId());
        assertTrue(result2);
        List<PushDeviceRegistrationEntity> devices2 = pushDeviceRepository.findByAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        assertEquals(1, devices2.size());
        assertFalse(devices2.get(0).getActive());
        powerAuthTestClient.unblockActivation(powerAuthTestClient.getActivationId());
        boolean result3 = pushServerClient.updateDeviceStatus(powerAuthTestClient.getActivationId());
        assertTrue(result3);
        List<PushDeviceRegistrationEntity> devices3 = pushDeviceRepository.findByAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        assertEquals(1, devices3.size());
        assertTrue(devices3.get(0).getActive());
        boolean result4 = pushServerClient.deleteDevice(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        assertTrue(result4);
    }

    @Test
    @SuppressWarnings("unchecked") //known parameters of HashMap
    public void sendPushMessageTest() throws Exception {
        boolean result = pushServerClient.createDevice(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN, MobilePlatform.Android, powerAuthTestClient.getActivationId());
        assertTrue(result);
        SendPushMessageRequest request = new SendPushMessageRequest();
        PushMessage pushMessage = new PushMessage();
        PushMessageAttributes attributes = new PushMessageAttributes();
        PushMessageBody pushMessageBody = new PushMessageBody();
        pushMessageBody.setTitle("Balance update");
        pushMessageBody.setBody("Your balance is now $745.00");
        pushMessageBody.setBadge(3);
        pushMessageBody.setSound("riff.wav");
        pushMessageBody.setCategory("balance-update");
        pushMessageBody.setCollapseKey("balance-update");
        pushMessageBody.setValidUntil(new Date());
        pushMessageBody.setExtras((Map<String, Object>) new HashMap<String, Object>().put("_comment", "Any custom data."));
        attributes.setSilent(false);
        attributes.setPersonal(true);
        pushMessage.setUserId("Test_User");
        pushMessage.setActivationId(powerAuthTestClient.getActivationId());
        pushMessage.setAttributes(attributes);
        pushMessage.setBody(pushMessageBody);
        pushMessage.setAttributes(attributes);
        request.setAppId(powerAuthTestClient.getApplicationId());
        request.setMessage(pushMessage);
        ObjectResponse<PushMessageSendResult> actual = pushServerClient.sendPushMessage(powerAuthTestClient.getApplicationId(), pushMessage);
        assertThat(actual.getStatus()).isEqualTo("OK");
        List<PushDeviceRegistrationEntity> devices = pushDeviceRepository.findByAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        devices.forEach(pushDeviceRepository::delete);
    }


    @Test
    @SuppressWarnings("unchecked") //known parameters of HashMap
    public void sendPushMessageBatchTest() throws Exception {
        boolean result = pushServerClient.createDevice(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN, MobilePlatform.Android, powerAuthTestClient.getActivationId());
        assertTrue(result);
        SendPushMessageBatchRequest request = new SendPushMessageBatchRequest();
        List<PushMessage> batch = new ArrayList<>();
        PushMessage pushMessage = new PushMessage();
        PushMessageAttributes attributes = new PushMessageAttributes();
        PushMessageBody pushMessageBody = new PushMessageBody();
        pushMessageBody.setTitle("Balance update");
        pushMessageBody.setBody("Your balance is now $745.00");
        pushMessageBody.setBadge(3);
        pushMessageBody.setSound("riff.wav");
        pushMessageBody.setCategory("balance-update");
        pushMessageBody.setCollapseKey("balance-update");
        pushMessageBody.setValidUntil(new Date());
        pushMessageBody.setExtras((Map<String, Object>) new HashMap<String, Object>().put("_comment", "Any custom data."));
        attributes.setSilent(false);
        attributes.setPersonal(true);
        pushMessage.setUserId("Test_User");
        pushMessage.setActivationId(powerAuthTestClient.getActivationId());
        pushMessage.setAttributes(attributes);
        pushMessage.setBody(pushMessageBody);
        pushMessage.setAttributes(attributes);
        batch.add(pushMessage);
        request.setAppId(powerAuthTestClient.getApplicationId());
        request.setBatch(batch);
        ObjectResponse<PushMessageSendResult> actual = pushServerClient.sendPushMessageBatch(powerAuthTestClient.getApplicationId(), batch);
        assertThat(actual.getStatus()).isEqualTo("OK");
        List<PushDeviceRegistrationEntity> devices = pushDeviceRepository.findByAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        devices.forEach(pushDeviceRepository::delete);
    }

    @Test
    @SuppressWarnings("unchecked") //known parameters of HashMap
    public void createCampaignTest() throws Exception {
        boolean result = pushServerClient.createDevice(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN, MobilePlatform.Android, powerAuthTestClient.getActivationId());
        assertTrue(result);
        CreateCampaignRequest campaignRequest = new CreateCampaignRequest();
        PushMessageBody pushMessageBody = new PushMessageBody();
        pushMessageBody.setTitle("Balance update");
        pushMessageBody.setBody("Your balance is now $745.00");
        pushMessageBody.setBadge(3);
        pushMessageBody.setSound("riff.wav");
        pushMessageBody.setCategory("balance-update");
        pushMessageBody.setCollapseKey("balance-update");
        pushMessageBody.setValidUntil(new Date());
        pushMessageBody.setExtras((Map<String, Object>) new HashMap<String, Object>().put("_comment", "Any custom data."));
        campaignRequest.setAppId(powerAuthTestClient.getApplicationId());
        campaignRequest.setMessage(pushMessageBody);
        ObjectResponse<CreateCampaignResponse> actual = pushServerClient.createCampaign(powerAuthTestClient.getApplicationId(), pushMessageBody);
        assertThat(actual.getStatus()).isEqualTo("OK");
        List<PushDeviceRegistrationEntity> devices = pushDeviceRepository.findByAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        devices.forEach(pushDeviceRepository::delete);
    }

    @Test
    public void deleteCampaignTest() throws Exception {
        boolean actual = pushServerClient.deleteCampaign(2L);
        assertTrue(actual);
    }

    @Test
    public void getListOfCampaignsTest() throws Exception{
        ObjectResponse<ListOfCampaignsResponse> actual = pushServerClient.getListOfCampaigns(true);
        String body = restTemplate.getForEntity("http://localhost:" + port + "/push/campaign/list?all=true", String.class).getBody();
        assertNotNull(body);
        ObjectResponse<ListOfCampaignsResponse> expected = mapper.readValue(body, new TypeReference<ObjectResponse<ListOfCampaignsResponse>>() {});
        assertThat(actual.getStatus()).isEqualTo(expected.getStatus());
        assertThat(actual.getResponseObject().containsAll(expected.getResponseObject())).isTrue();
    }

    @Test
    public void getCampaignTest() throws Exception{
        ObjectResponse<CampaignResponse> actual = pushServerClient.getCampaign(1L);
        String body = restTemplate.getForEntity("http://localhost:" + port + "/push/campaign/1/detail", String.class).getBody();
        assertNotNull(body);
        ObjectResponse<CampaignResponse> expected = mapper.readValue(body, new TypeReference<ObjectResponse<CampaignResponse>>() {});
        assertThat(actual.getStatus()).isEqualTo(expected.getStatus());
        assertThat(actual.getResponseObject()).isEqualTo(expected.getResponseObject());
    }

    @Test
    public void addUsersToCampaignTest() throws Exception {
        ListOfUsers listOfUsers = new ListOfUsers();
        listOfUsers.addAll(Arrays.asList("1234567890", "1234567891", "1234567893"));
        boolean actual = pushServerClient.addUsersToCampaign(1L, listOfUsers);
        assertTrue(actual);
    }

    @Test
    public void getListOfUsersFromCampaignTest() throws Exception {
        PagedResponse<ListOfUsersFromCampaignResponse> actual = pushServerClient.getListOfUsersFromCampaign(10L, 0, 3);
        String body = restTemplate.getForEntity("http://localhost:" + port + "/push/campaign/10/user/list?page=0&size=3", String.class).getBody();
        assertNotNull(body);
        PagedResponse<ListOfUsersFromCampaignResponse> expected = mapper.readValue(body, new TypeReference<PagedResponse<ListOfUsersFromCampaignResponse>>() {});
        assertThat(actual.getResponseObject()).isEqualTo(expected.getResponseObject());
        assertThat(actual.getStatus()).isEqualTo(expected.getStatus());
        assertThat(actual.getPage()).isEqualTo(expected.getPage());
    }

    @Test
    public void deleteUsersFromCampaignTest() throws Exception {
        ListOfUsers listOfUsers = new ListOfUsers();
        listOfUsers.addAll(Arrays.asList("1234567890", "1234567891", "1234567893"));
        boolean actual = pushServerClient.deleteUsersFromCampaign(3L, listOfUsers);
        assertTrue(actual);
    }

    @Test
    public void sendTestingCampaignTest() throws Exception {
        boolean actual = pushServerClient.sendTestCampaign(1L, "Test_User");
        assertTrue(actual);
    }

    @Test
    public void sendCampaignTest() throws Exception {
        boolean actual = pushServerClient.sendCampaign(1L);
        assertTrue(actual);
    }

    @Test(expected = PushServerClientException.class)
    public void createDeviceWithMultipleActivationsTest() throws PushServerClientException {
        List<String> activationIds = new ArrayList<>();
        activationIds.add(powerAuthTestClient.getActivationId());
        activationIds.add(powerAuthTestClient.getActivationId2());
        pushServerClient.createDeviceForActivations(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN, MobilePlatform.iOS, activationIds);
    }

    @Test
    public void createDeviceSameActivationSamePushTokenUpdatesTest() throws PushServerClientException {
        // This test tests refresh of a device registration
        boolean actual = pushServerClient.createDevice(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN, MobilePlatform.iOS, powerAuthTestClient.getActivationId());
        assertTrue(actual);
        List<PushDeviceRegistrationEntity> devices = pushDeviceRepository.findByAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        assertEquals(1, devices.size());
        Long rowId = devices.get(0).getId();
        boolean actual2 = pushServerClient.createDevice(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN, MobilePlatform.iOS, powerAuthTestClient.getActivationId());
        assertTrue(actual2);
        List<PushDeviceRegistrationEntity> devices2 = pushDeviceRepository.findByAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        assertEquals(1, devices2.size());
        assertEquals(rowId, devices2.get(0).getId());
        devices2.forEach(pushDeviceRepository::delete);
    }

    @Test
    public void createDeviceSameActivationDifferentPushTokenTest() throws PushServerClientException {
        // This test tests change of Push Token - new token has been issued by Google or Apple and the device registers for same activation
        boolean actual = pushServerClient.createDevice(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN, MobilePlatform.iOS, powerAuthTestClient.getActivationId());
        assertTrue(actual);
        List<PushDeviceRegistrationEntity> devices = pushDeviceRepository.findByAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        Long rowId = devices.get(0).getId();
        boolean actual2 = pushServerClient.createDevice(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN_2, MobilePlatform.iOS, powerAuthTestClient.getActivationId());
        assertTrue(actual2);
        // The push token must change, however row ID stays the same
        List<PushDeviceRegistrationEntity> devices1 = pushDeviceRepository.findByAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        List<PushDeviceRegistrationEntity> devices2 = pushDeviceRepository.findByAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN_2);
        assertEquals(0, devices1.size());
        assertEquals(1, devices2.size());
        assertEquals(rowId, devices2.get(0).getId());
        devices2.forEach(pushDeviceRepository::delete);
    }

    @Test
    public void createDeviceDifferentActivationSamePushTokenTest() throws PushServerClientException {
        // This test tests change of activation - user deleted the activation and created a new one, the push token is the same
        boolean actual = pushServerClient.createDevice(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN, MobilePlatform.iOS, powerAuthTestClient.getActivationId());
        assertTrue(actual);
        List<PushDeviceRegistrationEntity> devices = pushDeviceRepository.findByAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        Long rowId = devices.get(0).getId();
        boolean actual2 = pushServerClient.createDevice(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN, MobilePlatform.iOS, powerAuthTestClient.getActivationId());
        assertTrue(actual2);
        List<PushDeviceRegistrationEntity> devices2 = pushDeviceRepository.findByAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        assertEquals(1, devices2.size());
        assertEquals(rowId, devices2.get(0).getId());
        devices2.forEach(pushDeviceRepository::delete);
    }

}
