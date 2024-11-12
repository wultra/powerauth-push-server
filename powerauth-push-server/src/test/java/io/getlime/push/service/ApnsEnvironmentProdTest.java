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
package io.getlime.push.service;

import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.push.api.PowerAuthTestClient;
import io.getlime.push.client.PushServerClient;
import io.getlime.push.client.PushServerClientException;
import io.getlime.push.client.PushServerTestClientFactory;
import io.getlime.push.configuration.PushServerAppCredentialConfiguration;
import io.getlime.push.model.entity.PushMessage;
import io.getlime.push.model.entity.PushMessageBody;
import io.getlime.push.model.entity.PushMessageSendResult;
import io.getlime.push.model.enumeration.ApnsEnvironment;
import io.getlime.push.model.enumeration.MobilePlatform;
import io.getlime.push.model.enumeration.Mode;
import io.getlime.push.model.request.CreateDeviceRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for configuration of production APNs environment vs development push messages.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(locations = "classpath:application-test-apns-prod.properties")
@ActiveProfiles("test")
public class ApnsEnvironmentProdTest {

    private static final String MOCK_PUSH_TOKEN = "1234567890987654321234567890";

    @Autowired
    private PushServerTestClientFactory testClientFactory;

    @Autowired
    private PushServerAppCredentialConfiguration appCredentialConfig;

    private PowerAuthTestClient powerAuthTestClient;
    private PushServerClient pushServerClient;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() throws Exception {
        pushServerClient = testClientFactory.createPushServerClient("http://localhost:" + port);
        powerAuthTestClient = testClientFactory.createPowerAuthTestClient();
        appCredentialConfig.configure(powerAuthTestClient.getApplicationId());
    }

    @Test
    public void testApnsProdNoDevelopmentPushMessages() throws PushServerClientException {
        CreateDeviceRequest request = CreateDeviceRequest.builder()
                .appId(powerAuthTestClient.getApplicationId())
                .token(MOCK_PUSH_TOKEN)
                .platform(MobilePlatform.APNS)
                .environment(ApnsEnvironment.DEVELOPMENT)
                .activationId(powerAuthTestClient.getActivationId())
                .build();
        boolean result = pushServerClient.createDevice(request);
        assertTrue(result);

        final PushMessageBody pushMessageBody = new PushMessageBody();
        pushMessageBody.setTitle("Balance update");
        pushMessageBody.setBody("Your balance is now $745.00");

        final PushMessage pushMessage = new PushMessage();
        pushMessage.setUserId(PushServerTestClientFactory.TEST_USER_ID);
        pushMessage.setActivationId(powerAuthTestClient.getActivationId());
        pushMessage.setBody(pushMessageBody);

        final ObjectResponse<PushMessageSendResult> actual = pushServerClient.sendPushMessage(powerAuthTestClient.getApplicationId(), Mode.SYNCHRONOUS, pushMessage);
        assertEquals("OK", actual.getStatus());
        assertEquals(0, actual.getResponseObject().getApns().getSent());
        assertEquals(1, actual.getResponseObject().getApns().getFailed());
    }

}
