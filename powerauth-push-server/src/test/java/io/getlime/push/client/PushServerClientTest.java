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

package io.getlime.push.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.push.model.base.PagedResponse;
import io.getlime.push.model.entity.*;
import io.getlime.push.model.request.CreateCampaignRequest;
import io.getlime.push.model.request.SendPushMessageBatchRequest;
import io.getlime.push.model.request.SendPushMessageRequest;
import io.getlime.push.model.response.*;
import io.getlime.push.repository.AppCredentialsRepository;
import org.hamcrest.CoreMatchers;
import org.hamcrest.beans.HasPropertyWithValue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Class used for testing client-server methods
 * All tests cover each method from {@link PushServerClient}.
 * Methods are named with suffix "Test" and are just compared with expected server responses.
 * Using in memory H2 create/drop database.
 *
 * @author Martin Tupy, martin.tupy.work@gmail.com
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
public class PushServerClientTest {

    private static final String MOCK_ACTIVATION_ID = "11111111-1111-1111-1111-111111111111";
    private static final Long MOCK_APPLICATION_ID = 1L;
    private static final String MOCK_PUSH_TOKEN = "1234567890987654321234567890";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private PushServerClient pushServerClient;

    @Autowired
    private AppCredentialsRepository appCredentialsRepository;

    @LocalServerPort
    private int port;

    @Before
    public void setPushServerClientsUrl() {
        pushServerClient = new PushServerClient("http://localhost:" + port);
        mapper = new ObjectMapper();
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void getServiceStatusTest() throws Exception {
        ObjectResponse<ServiceStatusResponse> actual = pushServerClient.getServiceStatus();
        String body = restTemplate.getForEntity("http://localhost:" + port + "/push/service/status", String.class).getBody();
        ObjectResponse<ServiceStatusResponse> expected = mapper.readValue(body, new TypeReference<ObjectResponse<ServiceStatusResponse>>() {
        });
        assertThat(actual.getStatus()).isEqualTo(expected.getStatus());
        assertThat(actual.getResponseObject().getApplicationDisplayName()).isEqualTo(expected.getResponseObject().getApplicationDisplayName());
        assertThat(actual.getResponseObject().getApplicationEnvironment()).isEqualTo(expected.getResponseObject().getApplicationEnvironment());
        assertThat(actual.getResponseObject().getApplicationName()).isEqualTo(expected.getResponseObject().getApplicationName());
    }

    @Test
    public void createDeviceTest() throws Exception {
        boolean actual = pushServerClient.createDevice(MOCK_APPLICATION_ID, MOCK_PUSH_TOKEN, MobilePlatform.iOS);
        assertThat(actual).isTrue();
    }

    @Test
    public void createDeviceWithActivationIDTest() throws Exception {
        boolean actual = pushServerClient.createDevice(MOCK_APPLICATION_ID, MOCK_PUSH_TOKEN, MobilePlatform.iOS, MOCK_ACTIVATION_ID);
        assertThat(actual).isTrue();
    }

    @Test
    public void deleteDeviceTest() throws Exception {
        boolean actual = pushServerClient.deleteDevice(MOCK_APPLICATION_ID, MOCK_PUSH_TOKEN);
        assertThat(actual).isTrue();
    }


    @Test
    public void updateDeviceStatusTest() throws Exception {
        boolean actual = pushServerClient.updateDeviceStatus(MOCK_ACTIVATION_ID);
        assertThat(actual).isTrue();
    }


    @Test
    @SuppressWarnings("unchecked") //known parameters of HashMap
    public void sendPushMessageTest() throws Exception {
        if (appCredentialsRepository.count() < 1) {
            exception.expect(PushServerClientException.class);
            exception.expect(HasPropertyWithValue.hasProperty("error", HasPropertyWithValue.hasProperty("message", CoreMatchers.is("Application not found"))));
        }
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
        attributes.setSilent(true);
        attributes.setPersonal(true);
        attributes.setEncrypted(false);
        pushMessage.setUserId("123");
        pushMessage.setActivationId("49414e31-f3df-4cea-87e6-f214ca3b8412");
        pushMessage.setAttributes(attributes);
        pushMessage.setBody(pushMessageBody);
        pushMessage.setAttributes(attributes);
        request.setAppId(2L);
        request.setMessage(pushMessage);
        ObjectResponse<PushMessageSendResult> actual = pushServerClient.sendPushMessage(2L, pushMessage);
        String body = restTemplate.postForEntity("http://localhost:" + port + "/push/message/send", new ObjectRequest<>(request), String.class).getBody();
        ObjectResponse<PushMessageSendResult> expected = mapper.readValue(body, new TypeReference<ObjectResponse<PushMessageSendResult>>() {
        });
        assertThat(actual.getStatus()).isEqualTo(expected.getStatus());
        assertThat(actual.getResponseObject()).isEqualTo(expected.getResponseObject());
    }


    @Test
    @SuppressWarnings("unchecked") //known parameters of HashMap
    public void sendPushMessageBatchTest() throws Exception {
        if (appCredentialsRepository.count() < 1) {
            exception.expect(PushServerClientException.class);
            exception.expect(HasPropertyWithValue.hasProperty("error", HasPropertyWithValue.hasProperty("message", CoreMatchers.is("Application not found"))));
        }
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
        attributes.setSilent(true);
        attributes.setPersonal(true);
        attributes.setEncrypted(false);
        pushMessage.setUserId("123");
        pushMessage.setActivationId("49414e31-f3df-4cea-87e6-f214ca3b8412");
        pushMessage.setAttributes(attributes);
        pushMessage.setBody(pushMessageBody);
        pushMessage.setAttributes(attributes);
        batch.add(pushMessage);
        request.setAppId(2L);
        request.setBatch(batch);
        ObjectResponse<PushMessageSendResult> actual = pushServerClient.sendPushMessage(2L, pushMessage);
        String body = restTemplate.postForEntity("http://localhost:" + port + "/push/message/send", new ObjectRequest<>(request), String.class).getBody();
        ObjectResponse<PushMessageSendResult> expected = mapper.readValue(body, new TypeReference<ObjectResponse<PushMessageSendResult>>() {
        });
        assertThat(actual.getStatus()).isEqualTo(expected.getStatus());
        assertThat(actual.getResponseObject()).isEqualTo(expected.getResponseObject());
    }

    @Test
    @SuppressWarnings("unchecked") //known parameters of HashMap
    public void createCampaignTest() throws Exception {
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
        campaignRequest.setAppId(1L);
        campaignRequest.setMessage(pushMessageBody);
        ObjectResponse<CreateCampaignResponse> actual = pushServerClient.createCampaign(1L, pushMessageBody);
        String body = restTemplate.postForEntity("http://localhost:" + port + "/push/campaign/create", new ObjectRequest<>(campaignRequest), String.class).getBody();
        ObjectResponse<CreateCampaignResponse> expected = mapper.readValue(body, new TypeReference<ObjectResponse<CreateCampaignResponse>>(){
        });
        assertThat(actual.getStatus()).isEqualTo(expected.getStatus());
        assertThat(actual.getResponseObject().getId() + 1).isEqualTo(expected.getResponseObject().getId());
    }

    @Test
    public void deleteCampaignTest() throws Exception {
        boolean actual = pushServerClient.deleteCampaign(2L);
        assertThat(actual).isTrue();
    }

    @Test
    public void getListOfCampaignsTest() throws Exception{
        ObjectResponse<ListOfCampaignsResponse> actual = pushServerClient.getListOfCampaigns(true);
        String body = restTemplate.getForEntity("http://localhost:" + port + "/push/campaign/list?all=true", String.class).getBody();
        ObjectResponse<ListOfCampaignsResponse> expected = mapper.readValue(body, new TypeReference<ObjectResponse<ListOfCampaignsResponse>>() {
        });
        assertThat(actual.getStatus()).isEqualTo(expected.getStatus());
        assertThat(actual.getResponseObject().containsAll(expected.getResponseObject())).isTrue();
    }

    @Test
    public void getCampaignTest() throws Exception{
        ObjectResponse<CampaignResponse> actual = pushServerClient.getCampaign(1L);
        String body = restTemplate.getForEntity("http://localhost:" + port + "/push/campaign/1/detail", String.class).getBody();
        ObjectResponse<CampaignResponse> expected = mapper.readValue(body, new TypeReference<ObjectResponse<CampaignResponse>>() {
        });
        assertThat(actual.getStatus()).isEqualTo(expected.getStatus());
        assertThat(actual.getResponseObject()).isEqualTo(expected.getResponseObject());
    }

    @Test
    public void addUsersToCampaignTest() throws Exception {
        ListOfUsers listOfUsers = new ListOfUsers();
        listOfUsers.addAll(Arrays.asList("1234567890", "1234567891", "1234567893"));
        boolean actual = pushServerClient.addUsersToCampaign(1L, listOfUsers);
        assertThat(actual).isTrue();

    }

    @Test
    public void getListOfUsersFromCampaignTest() throws Exception {
        PagedResponse<ListOfUsersFromCampaignResponse> actual = pushServerClient.getListOfUsersFromCampaign(10L, 0, 3);
        String body = restTemplate.getForEntity("http://localhost:" + port + "/push/campaign/10/user/list?page=0&size=3", String.class).getBody();
        PagedResponse<ListOfUsersFromCampaignResponse> expected = mapper.readValue(body, new TypeReference<PagedResponse<ListOfUsersFromCampaignResponse>>() {
        });
        assertThat(actual.getResponseObject()).isEqualTo(expected.getResponseObject());
        assertThat(actual.getStatus()).isEqualTo(expected.getStatus());
        assertThat(actual.getPage()).isEqualTo(expected.getPage());
        assertThat(actual.getPage()).isEqualTo(expected.getPage());
    }

    @Test
    public void deleteUsersFromCampaignTest() throws Exception {
        ListOfUsers listOfUsers = new ListOfUsers();
        listOfUsers.addAll(Arrays.asList("1234567890", "1234567891", "1234567893"));
        boolean actual = pushServerClient.deleteUsersFromCampaign(3L, listOfUsers);
        assertThat(actual).isTrue();
    }

    @Test
    public void sendTestingCampaignTest() throws Exception {
        if (appCredentialsRepository.count() < 1) {
            exception.expect(PushServerClientException.class);
            exception.expect(HasPropertyWithValue.hasProperty("error", HasPropertyWithValue.hasProperty("message", CoreMatchers.is("Application not found"))));
        }
        boolean actual = pushServerClient.sendTestCampaign(1L, "1234567890");
        assertThat(actual).isTrue();
    }

    @Test
    public void sendCampaignTest() throws Exception {
        boolean actual = pushServerClient.sendCampaign(1L);
        assertThat(actual).isTrue();
    }
}
