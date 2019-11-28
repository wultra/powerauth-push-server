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

package io.getlime.push.client;

import io.getlime.push.repository.PushDeviceRepository;
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
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Class used for testing multi-activation registrations.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test-multiple-activations.properties")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PushServerMultipleActivationsTests {

    private static final String MOCK_PUSH_TOKEN = "1234567890987654321234567890";
    private static final String MOCK_PUSH_TOKEN_2 = "9876543212345678901234567890";

    @Autowired
    private PushDeviceRepository pushDeviceRepository;

    @LocalServerPort
    private int port;

    @Value("${powerauth.service.url}")
    private String powerAuthServiceUrl;

    @Autowired
    private PushServerTestClientFactory testClientFactory;

    @MockBean
    private PushServerClient pushServerClient;

    @MockBean
    private PowerAuthTestClient powerAuthTestClient;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        pushServerClient = testClientFactory.createPushServerClient("http://localhost:" + port);
        powerAuthTestClient = testClientFactory.createPowerAuthTestClient();
    }

    @Test
    public void createDeviceWithMultipleActivationsTest() throws Exception {
        List<String> activationIds = new ArrayList<>();
        activationIds.add(powerAuthTestClient.getActivationId());
        activationIds.add(powerAuthTestClient.getActivationId2());
        boolean result = pushServerClient.createDeviceForActivations(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN, MobilePlatform.iOS, activationIds);
        assertTrue(result);
        pushDeviceRepository.findByAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN).forEach(pushDeviceRepository::delete);
    }

    @Test(expected = PushServerClientException.class)
    public void createDeviceWithMultipleActivationsInvalidTest() throws Exception {
        pushServerClient.createDeviceForActivations(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN, MobilePlatform.iOS, Collections.emptyList());
    }

    @Test
    public void createDeviceSameActivationsSamePushTokenUpdatesTest() throws PushServerClientException {
        List<String> activationIds = new ArrayList<>();
        activationIds.add(powerAuthTestClient.getActivationId());
        activationIds.add(powerAuthTestClient.getActivationId2());
        // This test tests refresh of a device registration
        boolean actual = pushServerClient.createDeviceForActivations(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN, MobilePlatform.iOS, activationIds);
        assertTrue(actual);
        List<PushDeviceRegistrationEntity> devices = pushDeviceRepository.findByAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        assertEquals(2, devices.size());
        Set<Long> rowIds = new HashSet<>();
        devices.forEach(device -> rowIds.add(device.getId()));
        boolean actual2 = pushServerClient.createDeviceForActivations(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN, MobilePlatform.iOS, activationIds);
        assertTrue(actual2);
        List<PushDeviceRegistrationEntity> devices2 = pushDeviceRepository.findByAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        assertEquals(2, devices2.size());
        Set<Long> rowIds2 = new HashSet<>();
        devices2.forEach(device -> rowIds2.add(device.getId()));
        assertEquals(rowIds, rowIds2);
        pushDeviceRepository.findByAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN).forEach(pushDeviceRepository::delete);
    }

    @Test
    public void createDeviceSameActivationsDifferentPushTokenUpdatesTest() throws PushServerClientException {
        List<String> activationIds = new ArrayList<>();
        activationIds.add(powerAuthTestClient.getActivationId());
        activationIds.add(powerAuthTestClient.getActivationId2());
        // This test tests refresh of a device registration
        boolean actual = pushServerClient.createDeviceForActivations(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN, MobilePlatform.iOS, activationIds);
        assertTrue(actual);
        List<PushDeviceRegistrationEntity> devices = pushDeviceRepository.findByAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        assertEquals(2, devices.size());
        Set<Long> rowIds = new HashSet<>();
        devices.forEach(device -> rowIds.add(device.getId()));
        boolean actual2 = pushServerClient.createDeviceForActivations(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN_2, MobilePlatform.iOS, activationIds);
        assertTrue(actual2);
        List<PushDeviceRegistrationEntity> devices2 = pushDeviceRepository.findByAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN_2);
        assertEquals(2, devices2.size());
        Set<Long> rowIds2 = new HashSet<>();
        devices2.forEach(device -> rowIds2.add(device.getId()));
        assertEquals(rowIds, rowIds2);
        pushDeviceRepository.findByAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN_2).forEach(pushDeviceRepository::delete);
    }

    @Test
    public void createDeviceDifferentTwoActivationsSamePushTokenUpdatesTest() throws PushServerClientException {
        List<String> activationIds = new ArrayList<>();
        activationIds.add(powerAuthTestClient.getActivationId());
        activationIds.add(powerAuthTestClient.getActivationId2());
        // This test tests refresh of a device registration
        boolean actual = pushServerClient.createDeviceForActivations(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN, MobilePlatform.iOS, activationIds);
        assertTrue(actual);
        List<PushDeviceRegistrationEntity> devices = pushDeviceRepository.findByAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        assertEquals(2, devices.size());
        Set<Long> rowIds = new HashSet<>();
        devices.forEach(device -> rowIds.add(device.getId()));
        List<String> activationIds2 = new ArrayList<>();
        activationIds2.add(powerAuthTestClient.getActivationId3());
        activationIds2.add(powerAuthTestClient.getActivationId4());
        boolean actual2 = pushServerClient.createDeviceForActivations(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN, MobilePlatform.iOS, activationIds2);
        assertTrue(actual2);
        List<PushDeviceRegistrationEntity> devices2 = pushDeviceRepository.findByAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        assertEquals(2, devices2.size());
        Set<Long> rowIds2 = new HashSet<>();
        devices2.forEach(device -> rowIds2.add(device.getId()));
        // Row IDs do not match, because the activations are different
        assertNotEquals(rowIds, rowIds2);
        pushDeviceRepository.findByAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN).forEach(pushDeviceRepository::delete);
    }

    @Test
    public void createDeviceDifferentActivationSamePushTokenUpdatesTest() throws PushServerClientException {
        List<String> activationIds = new ArrayList<>();
        activationIds.add(powerAuthTestClient.getActivationId());
        activationIds.add(powerAuthTestClient.getActivationId2());
        // This test tests refresh of a device registration
        boolean actual = pushServerClient.createDeviceForActivations(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN, MobilePlatform.iOS, activationIds);
        assertTrue(actual);
        List<PushDeviceRegistrationEntity> devices = pushDeviceRepository.findByAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        assertEquals(2, devices.size());
        Set<Long> rowIds = new HashSet<>();
        devices.forEach(device -> rowIds.add(device.getId()));
        List<String> activationIds2 = new ArrayList<>();
        activationIds2.add(powerAuthTestClient.getActivationId());
        activationIds2.add(powerAuthTestClient.getActivationId4());
        boolean actual2 = pushServerClient.createDeviceForActivations(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN, MobilePlatform.iOS, activationIds2);
        assertTrue(actual2);
        List<PushDeviceRegistrationEntity> devices2 = pushDeviceRepository.findByAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        assertEquals(2, devices2.size());
        Set<Long> rowIds2 = new HashSet<>();
        devices2.forEach(device -> rowIds2.add(device.getId()));
        // Row IDs do not match, because the one of activations is different
        assertNotEquals(rowIds, rowIds2);
        pushDeviceRepository.findByAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN).forEach(pushDeviceRepository::delete);
    }

    @Test(expected = PushServerClientException.class)
    public void createDeviceMixedRegistrationEndpointsTest() throws PushServerClientException {
        List<String> activationIds = new ArrayList<>();
        activationIds.add(powerAuthTestClient.getActivationId());
        activationIds.add(powerAuthTestClient.getActivationId2());
        // This test tests refresh of a device registration
        boolean actual = pushServerClient.createDeviceForActivations(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN, MobilePlatform.iOS, activationIds);
        assertTrue(actual);
        List<PushDeviceRegistrationEntity> devices = pushDeviceRepository.findByAppIdAndPushToken(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN);
        assertEquals(2, devices.size());
        pushServerClient.createDevice(powerAuthTestClient.getApplicationId(), MOCK_PUSH_TOKEN, MobilePlatform.iOS, powerAuthTestClient.getActivationId3());
    }

}
