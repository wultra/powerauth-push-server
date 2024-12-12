/*
 * Copyright 2019 Wultra s.r.o.
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

package io.getlime.push.tests;

import io.getlime.push.api.PowerAuthTestClient;
import io.getlime.push.client.PushServerClient;
import io.getlime.push.client.PushServerClientException;
import io.getlime.push.client.PushServerTestClientFactory;
import io.getlime.push.configuration.PushServerAppCredentialConfiguration;
import io.getlime.push.model.enumeration.ApnsEnvironment;
import io.getlime.push.model.enumeration.MobilePlatform;
import io.getlime.push.model.request.CreateDeviceForActivationsRequest;
import io.getlime.push.model.request.CreateDeviceRequest;
import io.getlime.push.repository.PushDeviceRepository;
import io.getlime.push.repository.model.PushDeviceRegistrationEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Class used for testing multi-activation registrations.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(locations = "classpath:application-test-multiple-activations.properties")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class PushServerMultipleActivationsTests {

    private static final String MOCK_PUSH_TOKEN = "1234567890987654321234567890";
    private static final String MOCK_PUSH_TOKEN_2 = "9876543212345678901234567890";

    @Autowired
    private PushDeviceRepository pushDeviceRepository;

    @LocalServerPort
    private int port;

    @Autowired
    private PushServerTestClientFactory testClientFactory;

    @Autowired
    private PushServerAppCredentialConfiguration appCredentialConfig;

    private PushServerClient pushServerClient;

    private PowerAuthTestClient powerAuthTestClient;

    @BeforeEach
    public void setUp() throws Exception {
        pushServerClient = testClientFactory.createPushServerClient("http://localhost:" + port);
        powerAuthTestClient = testClientFactory.createPowerAuthTestClient();
        appCredentialConfig.configure(powerAuthTestClient.getApplicationId());
    }

    @Test
    public void createDeviceWithMultipleActivationsTest() throws Exception {
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
        boolean result = pushServerClient.createDeviceForActivations(request);
        assertTrue(result);
        pushDeviceRepository.deleteAll(pushDeviceRepository.findByAppCredentialsAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN));
    }

    @Test
    public void createDeviceWithMultipleActivationsInvalidTest() {
        CreateDeviceForActivationsRequest request = CreateDeviceForActivationsRequest.builder()
                .appId(powerAuthTestClient.getApplicationId())
                .token(MOCK_PUSH_TOKEN)
                .platform(MobilePlatform.APNS)
                .environment(ApnsEnvironment.DEVELOPMENT)
                .build();
        assertThrows(PushServerClientException.class, () ->
                pushServerClient.createDeviceForActivations(request));
    }

    @Test
    public void createDeviceSameActivationsSamePushTokenUpdatesTest() throws PushServerClientException {
        List<String> activationIds = new ArrayList<>();
        activationIds.add(powerAuthTestClient.getActivationId());
        activationIds.add(powerAuthTestClient.getActivationId2());
        // This test tests refresh of a device registration
        CreateDeviceForActivationsRequest request = CreateDeviceForActivationsRequest.builder()
                .appId(powerAuthTestClient.getApplicationId())
                .token(MOCK_PUSH_TOKEN)
                .platform(MobilePlatform.APNS)
                .environment(ApnsEnvironment.DEVELOPMENT)
                .build();
        request.getActivationIds().addAll(activationIds);
        boolean actual = pushServerClient.createDeviceForActivations(request);
        assertTrue(actual);
        List<PushDeviceRegistrationEntity> devices = pushDeviceRepository.findByAppCredentialsAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        assertEquals(2, devices.size());
        Set<Long> rowIds = new HashSet<>();
        devices.forEach(device -> rowIds.add(device.getId()));
        boolean actual2 = pushServerClient.createDeviceForActivations(request);
        assertTrue(actual2);
        List<PushDeviceRegistrationEntity> devices2 = pushDeviceRepository.findByAppCredentialsAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        assertEquals(2, devices2.size());
        Set<Long> rowIds2 = new HashSet<>();
        devices2.forEach(device -> rowIds2.add(device.getId()));
        assertEquals(rowIds, rowIds2);
        pushDeviceRepository.deleteAll(pushDeviceRepository.findByAppCredentialsAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN));
    }

    @Test
    public void createDeviceSameActivationsDifferentPushTokenUpdatesTest() throws PushServerClientException {
        List<String> activationIds = new ArrayList<>();
        activationIds.add(powerAuthTestClient.getActivationId());
        activationIds.add(powerAuthTestClient.getActivationId2());
        // This test tests refresh of a device registration
        CreateDeviceForActivationsRequest request = CreateDeviceForActivationsRequest.builder()
                .appId(powerAuthTestClient.getApplicationId())
                .token(MOCK_PUSH_TOKEN)
                .platform(MobilePlatform.APNS)
                .environment(ApnsEnvironment.DEVELOPMENT)
                .build();
        request.getActivationIds().addAll(activationIds);
        boolean actual = pushServerClient.createDeviceForActivations(request);
        assertTrue(actual);
        List<PushDeviceRegistrationEntity> devices = pushDeviceRepository.findByAppCredentialsAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        assertEquals(2, devices.size());
        Set<Long> rowIds = new HashSet<>();
        devices.forEach(device -> rowIds.add(device.getId()));
        CreateDeviceForActivationsRequest request2 = CreateDeviceForActivationsRequest.builder()
                .appId(powerAuthTestClient.getApplicationId())
                .token(MOCK_PUSH_TOKEN_2)
                .platform(MobilePlatform.APNS)
                .environment(ApnsEnvironment.DEVELOPMENT)
                .build();
        request2.getActivationIds().addAll(activationIds);
        boolean actual2 = pushServerClient.createDeviceForActivations(request2);
        assertTrue(actual2);
        List<PushDeviceRegistrationEntity> devices2 = pushDeviceRepository.findByAppCredentialsAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN_2);
        assertEquals(2, devices2.size());
        Set<Long> rowIds2 = new HashSet<>();
        devices2.forEach(device -> rowIds2.add(device.getId()));
        assertEquals(rowIds, rowIds2);
        pushDeviceRepository.deleteAll(pushDeviceRepository.findByAppCredentialsAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN_2));
    }

    @Test
    public void createDeviceDifferentTwoActivationsSamePushTokenUpdatesTest() throws PushServerClientException {
        List<String> activationIds = new ArrayList<>();
        activationIds.add(powerAuthTestClient.getActivationId());
        activationIds.add(powerAuthTestClient.getActivationId2());
        // This test tests refresh of a device registration
        CreateDeviceForActivationsRequest request = CreateDeviceForActivationsRequest.builder()
                .appId(powerAuthTestClient.getApplicationId())
                .token(MOCK_PUSH_TOKEN)
                .platform(MobilePlatform.APNS)
                .environment(ApnsEnvironment.DEVELOPMENT)
                .build();
        request.getActivationIds().addAll(activationIds);
        boolean actual = pushServerClient.createDeviceForActivations(request);
        assertTrue(actual);
        List<PushDeviceRegistrationEntity> devices = pushDeviceRepository.findByAppCredentialsAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        assertEquals(2, devices.size());
        Set<Long> rowIds = new HashSet<>();
        devices.forEach(device -> rowIds.add(device.getId()));
        List<String> activationIds2 = new ArrayList<>();
        activationIds2.add(powerAuthTestClient.getActivationId3());
        activationIds2.add(powerAuthTestClient.getActivationId4());
        CreateDeviceForActivationsRequest request2 = CreateDeviceForActivationsRequest.builder()
                .appId(powerAuthTestClient.getApplicationId())
                .token(MOCK_PUSH_TOKEN)
                .platform(MobilePlatform.APNS)
                .environment(ApnsEnvironment.DEVELOPMENT)
                .build();
        request2.getActivationIds().addAll(activationIds2);
        boolean actual2 = pushServerClient.createDeviceForActivations(request2);
        assertTrue(actual2);
        List<PushDeviceRegistrationEntity> devices2 = pushDeviceRepository.findByAppCredentialsAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        assertEquals(2, devices2.size());
        Set<Long> rowIds2 = new HashSet<>();
        devices2.forEach(device -> rowIds2.add(device.getId()));
        // Row IDs do not match, because the activations are different
        assertNotEquals(rowIds, rowIds2);
        pushDeviceRepository.deleteAll(pushDeviceRepository.findByAppCredentialsAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN));
    }

    @Test
    public void createDeviceDifferentActivationSamePushTokenUpdatesTest() throws PushServerClientException {
        List<String> activationIds = new ArrayList<>();
        activationIds.add(powerAuthTestClient.getActivationId());
        activationIds.add(powerAuthTestClient.getActivationId2());
        // This test tests refresh of a device registration
        CreateDeviceForActivationsRequest request = CreateDeviceForActivationsRequest.builder()
                .appId(powerAuthTestClient.getApplicationId())
                .token(MOCK_PUSH_TOKEN)
                .platform(MobilePlatform.APNS)
                .environment(ApnsEnvironment.DEVELOPMENT)
                .build();
        request.getActivationIds().addAll(activationIds);
        boolean actual = pushServerClient.createDeviceForActivations(request);
        assertTrue(actual);
        List<PushDeviceRegistrationEntity> devices = pushDeviceRepository.findByAppCredentialsAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        assertEquals(2, devices.size());
        Set<Long> rowIds = new HashSet<>();
        devices.forEach(device -> rowIds.add(device.getId()));
        List<String> activationIds2 = new ArrayList<>();
        activationIds2.add(powerAuthTestClient.getActivationId());
        activationIds2.add(powerAuthTestClient.getActivationId4());
        CreateDeviceForActivationsRequest request2 = CreateDeviceForActivationsRequest.builder()
                .appId(powerAuthTestClient.getApplicationId())
                .token(MOCK_PUSH_TOKEN)
                .platform(MobilePlatform.APNS)
                .environment(ApnsEnvironment.DEVELOPMENT)
                .build();
        request2.getActivationIds().addAll(activationIds2);
        boolean actual2 = pushServerClient.createDeviceForActivations(request2);
        assertTrue(actual2);
        List<PushDeviceRegistrationEntity> devices2 = pushDeviceRepository.findByAppCredentialsAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        assertEquals(2, devices2.size());
        Set<Long> rowIds2 = new HashSet<>();
        devices2.forEach(device -> rowIds2.add(device.getId()));
        // Row IDs do not match, because the one of activations is different
        assertNotEquals(rowIds, rowIds2);
        pushDeviceRepository.deleteAll(pushDeviceRepository.findByAppCredentialsAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN));
    }

}
