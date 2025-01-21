/*
 * Copyright 2022 Wultra s.r.o.
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
package com.wultra.push.client;

import com.wultra.push.api.PowerAuthTestClient;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Security;

/**
 * Factory for PowerAuth test client.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class PushServerTestClientFactory {

    public static final String TEST_APPLICATION_NAME = "Push_Server_Tests";
    public static final String TEST_APPLICATION_VERSION = "default";
    public static final String TEST_USER_ID = "Test_User";

    @Value("${powerauth.service.url}")
    private String powerAuthRestUrl;

    public PushServerClient createPushServerClient(String baseUrl) throws PushServerClientException {
        return new PushServerClient(baseUrl);
    }

    public PowerAuthTestClient createPowerAuthTestClient() throws Exception {
        final PowerAuthTestClient powerAuthTestClient = new PowerAuthTestClientRest();
        Security.addProvider(new BouncyCastleProvider());
        powerAuthTestClient.initializeClient(powerAuthRestUrl);
        String applicationId = powerAuthTestClient.initializeApplication(TEST_APPLICATION_NAME, TEST_APPLICATION_VERSION);
        String activationId = powerAuthTestClient.createActivation(TEST_USER_ID);
        String activationId2 = powerAuthTestClient.createActivation(TEST_USER_ID);
        String activationId3 = powerAuthTestClient.createActivation(TEST_USER_ID);
        String activationId4 = powerAuthTestClient.createActivation(TEST_USER_ID);
        powerAuthTestClient.setApplicationId(applicationId);
        powerAuthTestClient.setActivationId(activationId);
        powerAuthTestClient.setActivationId2(activationId2);
        powerAuthTestClient.setActivationId3(activationId3);
        powerAuthTestClient.setActivationId4(activationId4);
        return powerAuthTestClient;
    }
}
