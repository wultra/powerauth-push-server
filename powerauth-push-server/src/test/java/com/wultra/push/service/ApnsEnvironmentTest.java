/*
 * Copyright 2024 Wultra s.r.o.
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
package com.wultra.push.service;

import com.eatthepath.pushy.apns.ApnsClient;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.wultra.push.api.PowerAuthTestClient;
import com.wultra.push.client.PushServerClient;
import com.wultra.push.client.PushServerClientException;
import com.wultra.push.client.PushServerTestClientFactory;
import com.wultra.push.configuration.PushServerAppCredentialConfiguration;
import com.wultra.push.errorhandling.exceptions.PushServerException;
import com.wultra.push.model.entity.PushMessage;
import com.wultra.push.model.entity.PushMessageBody;
import com.wultra.push.model.enumeration.ApnsEnvironment;
import com.wultra.push.model.enumeration.MobilePlatform;
import com.wultra.push.model.enumeration.Mode;
import com.wultra.push.model.request.CreateDeviceRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Test for configuration of production APNs environment vs development push messages.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@TestPropertySource(properties = "server.port=54726")
@ActiveProfiles("test")
public class ApnsEnvironmentTest {

    private static final String MOCK_PUSH_TOKEN = "1234567890987654321234567890";

    @Autowired
    private PushServerTestClientFactory testClientFactory;

    @Autowired
    private PushServerAppCredentialConfiguration appCredentialConfig;

    @Autowired
    private LoadingCache<String, AppRelatedPushClient> appRelatedPushClientCache;

    @SpyBean
    private PushSendingWorker pushSendingWorker;

    private PowerAuthTestClient powerAuthTestClient;
    private PushServerClient pushServerClient;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() throws Exception {
        pushServerClient = testClientFactory.createPushServerClient("http://localhost:" + port);
        powerAuthTestClient = testClientFactory.createPowerAuthTestClient();
        appRelatedPushClientCache.invalidateAll();
    }

    @Test
    public void testApnsConfigDevelopmentDevice() throws PushServerClientException, PushServerException {
        appCredentialConfig.configure(powerAuthTestClient.getApplicationId(), "development");
        prepareMocks();
        CreateDeviceRequest request = CreateDeviceRequest.builder()
                .appId(powerAuthTestClient.getApplicationId())
                .token(MOCK_PUSH_TOKEN)
                .platform(MobilePlatform.APNS)
                .environment(ApnsEnvironment.DEVELOPMENT)
                .activationId(powerAuthTestClient.getActivationId())
                .build();
        boolean result = pushServerClient.createDevice(request);
        assertTrue(result);

        pushServerClient.sendPushMessage(powerAuthTestClient.getApplicationId(), Mode.SYNCHRONOUS, preparePushMessage());
        ArgumentCaptor<ApnsClient> apnsClientCaptor = ArgumentCaptor.forClass(ApnsClient.class);
        verify(pushSendingWorker).sendMessageToApns(apnsClientCaptor.capture(), any(), any(), any(), any(), any(), any());
        assertEquals(appRelatedPushClientCache.get(powerAuthTestClient.getApplicationId()).getApnsClientDevelopment(), apnsClientCaptor.getValue());
    }

    @Test
    public void testApnsConfigProductionDevice() throws PushServerClientException, PushServerException {
        appCredentialConfig.configure(powerAuthTestClient.getApplicationId(), "development");
        prepareMocks();
        CreateDeviceRequest request = CreateDeviceRequest.builder()
                .appId(powerAuthTestClient.getApplicationId())
                .token(MOCK_PUSH_TOKEN)
                .platform(MobilePlatform.APNS)
                .environment(ApnsEnvironment.PRODUCTION)
                .activationId(powerAuthTestClient.getActivationId())
                .build();
        boolean result = pushServerClient.createDevice(request);
        assertTrue(result);

        pushServerClient.sendPushMessage(powerAuthTestClient.getApplicationId(), Mode.SYNCHRONOUS, preparePushMessage());
        ArgumentCaptor<ApnsClient> apnsClientCaptor = ArgumentCaptor.forClass(ApnsClient.class);
        verify(pushSendingWorker).sendMessageToApns(apnsClientCaptor.capture(), any(), any(), any(), any(), any(), any());
        assertEquals(appRelatedPushClientCache.get(powerAuthTestClient.getApplicationId()).getApnsClientProduction(), apnsClientCaptor.getValue());
    }

    @Test
    public void testApnsConfigDevelopmentAppConfig() throws PushServerClientException, PushServerException {
        appCredentialConfig.configure(powerAuthTestClient.getApplicationId(), "development");
        prepareMocks();
        CreateDeviceRequest request = CreateDeviceRequest.builder()
                .appId(powerAuthTestClient.getApplicationId())
                .token(MOCK_PUSH_TOKEN)
                .platform(MobilePlatform.APNS)
                .activationId(powerAuthTestClient.getActivationId())
                .build();
        boolean result = pushServerClient.createDevice(request);
        assertTrue(result);

        pushServerClient.sendPushMessage(powerAuthTestClient.getApplicationId(), Mode.SYNCHRONOUS, preparePushMessage());
        ArgumentCaptor<ApnsClient> apnsClientCaptor = ArgumentCaptor.forClass(ApnsClient.class);
        verify(pushSendingWorker).sendMessageToApns(apnsClientCaptor.capture(), any(), any(), any(), any(), any(), any());
        assertEquals(appRelatedPushClientCache.get(powerAuthTestClient.getApplicationId()).getApnsClientDevelopment(), apnsClientCaptor.getValue());
    }

    @Test
    public void testApnsConfigProductionAppConfig() throws PushServerClientException, PushServerException {
        appCredentialConfig.configure(powerAuthTestClient.getApplicationId(), "production");
        prepareMocks();
        CreateDeviceRequest request = CreateDeviceRequest.builder()
                .appId(powerAuthTestClient.getApplicationId())
                .token(MOCK_PUSH_TOKEN)
                .platform(MobilePlatform.APNS)
                .activationId(powerAuthTestClient.getActivationId())
                .build();
        boolean result = pushServerClient.createDevice(request);
        assertTrue(result);

        pushServerClient.sendPushMessage(powerAuthTestClient.getApplicationId(), Mode.SYNCHRONOUS, preparePushMessage());
        ArgumentCaptor<ApnsClient> apnsClientCaptor = ArgumentCaptor.forClass(ApnsClient.class);
        verify(pushSendingWorker).sendMessageToApns(apnsClientCaptor.capture(), any(), any(), any(), any(), any(), any());
        assertEquals(appRelatedPushClientCache.get(powerAuthTestClient.getApplicationId()).getApnsClientProduction(), apnsClientCaptor.getValue());
    }

    @Test
    public void testApnsConfigDevelopmentGlobal() throws PushServerClientException, PushServerException {
        appCredentialConfig.configure(powerAuthTestClient.getApplicationId(), null);
        prepareMocks();
        CreateDeviceRequest request = CreateDeviceRequest.builder()
                .appId(powerAuthTestClient.getApplicationId())
                .token(MOCK_PUSH_TOKEN)
                .platform(MobilePlatform.APNS)
                .activationId(powerAuthTestClient.getActivationId())
                .build();
        boolean result = pushServerClient.createDevice(request);
        assertTrue(result);

        pushServerClient.sendPushMessage(powerAuthTestClient.getApplicationId(), Mode.SYNCHRONOUS, preparePushMessage());
        ArgumentCaptor<ApnsClient> apnsClientCaptor = ArgumentCaptor.forClass(ApnsClient.class);
        verify(pushSendingWorker).sendMessageToApns(apnsClientCaptor.capture(), any(), any(), any(), any(), any(), any());
        assertEquals(appRelatedPushClientCache.get(powerAuthTestClient.getApplicationId()).getApnsClientDevelopment(), apnsClientCaptor.getValue());
    }

    private void prepareMocks() throws PushServerException {
        doCallRealMethod().when(pushSendingWorker).prepareApnsClient(any(), any());
        doCallRealMethod().when(pushSendingWorker).prepareFcmClient(any(), any());
        doAnswer((Answer<Void>) invocation -> {
            PushSendingCallback callback = invocation.getArgument(6);
            callback.didFinishSendingMessage(PushSendingCallback.Result.OK);
            return null;
        }).when(pushSendingWorker).sendMessageToApns(any(), any(), any(), any(), any(), any(), any());
    }

    private PushMessage preparePushMessage() {
        final PushMessageBody pushMessageBody = new PushMessageBody();
        pushMessageBody.setTitle("Balance update");
        pushMessageBody.setBody("Your balance is now $745.00");

        final PushMessage pushMessage = new PushMessage();
        pushMessage.setUserId(PushServerTestClientFactory.TEST_USER_ID);
        pushMessage.setActivationId(powerAuthTestClient.getActivationId());
        pushMessage.setBody(pushMessageBody);

        return pushMessage;
    }

}
