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
import io.getlime.push.model.entity.ListOfUsers;
import io.getlime.push.model.entity.PushMessageBody;
import io.getlime.push.model.request.CreateCampaignRequest;
import io.getlime.push.model.request.CreateDeviceRequest;
import io.getlime.push.model.response.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Class used for testing client-server methods
 *
 * @author Martin Tupy, martin.tupy.work@gmail.com
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
public class PushServerClientTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Test
    public void getServiceStatusTest() throws Exception {
        PushServerClient pushServerClient = new PushServerClient("http://localhost:" + port);
        ObjectResponse<ServiceStatusResponse> actual = pushServerClient.getServiceStatus();
        String body = restTemplate.getForEntity("http://localhost:" + port + "/push/service/status", String.class).getBody();
        ObjectResponse<ServiceStatusResponse> expected = new ObjectMapper().readValue(body, new TypeReference<ObjectResponse<ServiceStatusResponse>>() {
        });
        assertThat(actual.getStatus()).isEqualTo(expected.getStatus());
        assertThat(actual.getResponseObject().getApplicationDisplayName()).isEqualTo(expected.getResponseObject().getApplicationDisplayName());
        assertThat(actual.getResponseObject().getApplicationEnvironment()).isEqualTo(expected.getResponseObject().getApplicationEnvironment());
        assertThat(actual.getResponseObject().getApplicationName()).isEqualTo(expected.getResponseObject().getApplicationName());
        // Time on every call is different can't be compared
        // Assertions.assertThat(actual.getResponseObject().getTimestamp()).isEqualTo(parsedExpected.getResponseObject().getTimestamp());
    }

    @Test
    public void createDeviceTest() throws Exception {
        PushServerClient pushServerClient = new PushServerClient("http://localhost:" + port);
        CreateDeviceRequest createDeviceRequest = new CreateDeviceRequest();
        createDeviceRequest.setAppId(10L);
        createDeviceRequest.setToken("1234567890987654321234567890");
        createDeviceRequest.setPlatform(MobilePlatform.iOS.value());
//        createDeviceRequest.setActivationId("49414e31-f3df-4cea-87e6-f214ca3b8412");
        boolean actual1 = pushServerClient.createDevice(createDeviceRequest.getAppId(), createDeviceRequest.getToken(), MobilePlatform.iOS);
//        boolean actual2 = pushServerClient.createDevice(createDeviceRequest.getAppId(), createDeviceRequest.getToken(), MobilePlatform.iOS, createDeviceRequest.getActivationId());
//        String body = restTemplate.postForEntity("http://localhost:" + port + "/push/device/create", new ObjectRequest<>(createDeviceRequest), String.class).getBody();
//        ObjectResponse expected = new ObjectMapper().readValue(body, ObjectResponse.class);
        assertThat(actual1).isTrue();
//        assertThat(actual2).isEqualTo(expected.getStatus().equals(ObjectResponse.Status.OK));
    }

    @Test
    public void deleteDeviceTest() throws Exception {
        PushServerClient pushServerClient = new PushServerClient("http://localhost:" + port);
//        DeleteDeviceRequest deleteDeviceRequest = new DeleteDeviceRequest();
//        deleteDeviceRequest.setAppId(10L);
//        deleteDeviceRequest.setToken("12456789098321234567890");
        boolean actual = pushServerClient.deleteDevice(10L, "12456789098321234567890");
//        String body = restTemplate.postForEntity("http://localhost:" + port + "/push/device/delete", new ObjectRequest<>(deleteDeviceRequest), String.class).getBody();
//        ObjectResponse expected = new ObjectMapper().readValue(body, ObjectResponse.class);
        assertThat(actual).isTrue();
    }



//    @Test
//    public void updateDeviceStatus() throws Exception {
//        PushServerClient pushServerClient = new PushServerClient("http://localhost:" + port);
//        UpdateDeviceStatusRequest updateDeviceStatusRequest = new UpdateDeviceStatusRequest();
//        updateDeviceStatusRequest.setActivationId("49414e31-f3df-4cea-87e6-f214ca3b8412");
//        boolean actual = pushServerClient.updateDeviceStatus(updateDeviceStatusRequest.getActivationId());
//        String body = restTemplate.postForEntity("http://localhost:" + port + "/push/device/update", new ObjectRequest<>(updateDeviceStatusRequest), String.class).getBody();
//        ObjectResponse expected = new ObjectMapper().readValue(body, new TypeReference<ObjectResponse<UpdateDeviceStatusRequest>>() {
//        });
//        assertThat(actual).isEqualTo(expected.getStatus().equals(Response.Status.OK));
//    }

    @Test
    @SuppressWarnings("unchecked") //known parameters of HashMap
    public void createCampaignTest() throws Exception {
        PushServerClient pushServerClient = new PushServerClient("http://localhost:" + port);
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
        ObjectResponse<CreateCampaignResponse> expected = new ObjectMapper().readValue(body, new TypeReference<ObjectResponse<CreateCampaignResponse>>(){
        });
        assertThat(actual.getStatus()).isEqualTo(expected.getStatus());
        assertThat(actual.getResponseObject().getId() + 1).isEqualTo(expected.getResponseObject().getId());
    }

    @Test
    public void deleteCampaignTest() throws Exception {
        PushServerClient pushServerClient = new PushServerClient("http://localhost:" + port);
        boolean actual = pushServerClient.deleteCampaign(2L);
//        String body = restTemplate.postForEntity("http://localhost:" + port + "/push/campaign/2/delete", null, String.class).getBody();
//        ObjectResponse expected = new ObjectMapper().readValue(body, new TypeReference<ObjectResponse<DeleteCampaignResponse>>() {
//        });
        assertThat(actual).isTrue();
    }

    @Test
    public void getListOfCampaignsTest() throws Exception{
        PushServerClient pushServerClient = new PushServerClient("http://localhost:" + port);
        ObjectResponse<ListOfCampaignsResponse> actual = pushServerClient.getListOfCampaigns(true);
        String body = restTemplate.getForEntity("http://localhost:" + port + "/push/campaign/list?all=true", String.class).getBody();
        ObjectResponse<ListOfCampaignsResponse> expected = new ObjectMapper().readValue(body, new TypeReference<ObjectResponse<ListOfCampaignsResponse>>() {
        });
        assertThat(actual.getStatus()).isEqualTo(expected.getStatus());
        assertThat(actual.getResponseObject().containsAll(expected.getResponseObject())).isTrue();
    }

    //TODO: throws unhandled exception (PushServerException)
    @Test
    public void getCampaignTest() throws Exception{
        PushServerClient pushServerClient = new PushServerClient("http://localhost:" + port);
        ObjectResponse<CampaignResponse> actual = pushServerClient.getCampaign(1L);
        String body = restTemplate.getForEntity("http://localhost:" + port + "/push/campaign/1/detail", String.class).getBody();
        ObjectResponse<CampaignResponse> expected = new ObjectMapper().readValue(body, new TypeReference<ObjectResponse<CampaignResponse>>() {
        });
        assertThat(actual.getStatus()).isEqualTo(expected.getStatus());
        assertThat(actual.getResponseObject()).isEqualTo(expected.getResponseObject());
    }

    //TODO: throws unhandled exception (PushServerException)
    @Test
    public void addUsersToCampaignTest() throws Exception {
        PushServerClient pushServerClient = new PushServerClient("http://localhost:" + port);
        ListOfUsers listOfUsers = new ListOfUsers();
        listOfUsers.addAll(Arrays.asList("1234567890", "1234567891", "1234567893"));
        boolean actual = pushServerClient.addUsersToCampaign(1L, listOfUsers);
        assertThat(actual).isTrue();

    }

    @Test
    public void getListOfUsersFromCampaignTest() throws Exception {
        PushServerClient pushServerClient = new PushServerClient("http://localhost:" + port);
        ListOfUsersPagedResponse<ListOfUsersFromCampaignResponse> actual = pushServerClient.getListOfUsersFromCampaign(10L, 0, 3);
        String body = restTemplate.getForEntity("http://localhost:" + port + "/push/campaign/10/user/list?page=0&size=3", String.class).getBody();
        ListOfUsersPagedResponse<ListOfUsersFromCampaignResponse> expected = new ObjectMapper().readValue(body, new TypeReference<ListOfUsersPagedResponse>() {
        });
        assertThat(actual.getResponseObject()).isEqualTo(expected.getResponseObject());
        assertThat(actual.getStatus()).isEqualTo(expected.getStatus());
        assertThat(actual.getPage()).isEqualTo(expected.getPage());
        assertThat(actual.getPage()).isEqualTo(expected.getPage());
    }

    @Test
    public void deleteUsersFromCampaignTest() throws Exception {
        PushServerClient pushServerClient = new PushServerClient("http://localhost:" + port);
        ListOfUsers listOfUsers = new ListOfUsers();
        listOfUsers.addAll(Arrays.asList("1234567890", "1234567891", "1234567893"));
        boolean actual = pushServerClient.deleteUsersFromCampaign(3L, listOfUsers);
        assertThat(actual).isTrue();
    }

}
