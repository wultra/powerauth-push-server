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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wultra.security.powerauth.client.model.response.v4.GetApplicationDetailResponse;
import com.wultra.security.powerauth.client.v4.PowerAuthClient;
import com.wultra.security.powerauth.client.model.entity.Application;
import com.wultra.security.powerauth.client.model.entity.ApplicationVersion;
import com.wultra.security.powerauth.client.model.error.PowerAuthClientException;
import com.wultra.security.powerauth.client.model.request.InitActivationRequest;
import com.wultra.security.powerauth.client.model.request.v4.PrepareActivationRequest;
import com.wultra.security.powerauth.client.model.response.CommitActivationResponse;
import com.wultra.security.powerauth.client.model.response.CreateApplicationVersionResponse;
import com.wultra.security.powerauth.client.model.response.InitActivationResponse;
import com.wultra.security.powerauth.client.model.response.v4.PrepareActivationResponse;
import com.wultra.security.powerauth.crypto.lib.v4.encryptor.model.context.AeadSecrets;
import com.wultra.security.powerauth.crypto.lib.v4.encryptor.model.request.AeadEncryptedRequest;
import com.wultra.security.powerauth.crypto.lib.v4.encryptor.model.response.AeadEncryptedResponse;
import com.wultra.security.powerauth.rest.client.v4.PowerAuthRestClient;
import com.wultra.push.api.PowerAuthTestClient;
import com.wultra.security.powerauth.crypto.lib.encryptor.ClientEncryptor;
import com.wultra.security.powerauth.crypto.lib.encryptor.EncryptorFactory;
import com.wultra.security.powerauth.crypto.lib.encryptor.model.EncryptorId;
import com.wultra.security.powerauth.crypto.lib.encryptor.model.EncryptorParameters;
import com.wultra.security.powerauth.rest.api.model.request.v4.ActivationLayer2Request;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * REST test client for PowerAuth server.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class PowerAuthTestClientRest implements PowerAuthTestClient {

    private final EncryptorFactory encryptorFactory = new EncryptorFactory();
    private PowerAuthClient powerAuthClient;

    private String applicationId;
    private String applicationKey;

    private String activationId;
    private String activationId2;
    private String activationId3;
    private String activationId4;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void initializeClient(String powerAuthRestUrl) throws PowerAuthClientException {
        powerAuthClient = new PowerAuthRestClient(powerAuthRestUrl);
    }

    public String initializeApplication(String applicationName, String applicationVersion) throws PowerAuthClientException {
        // Create athe application if it does not exist
        final List<Application> applications = powerAuthClient.getApplicationList().getApplications();
        boolean applicationExists = false;
        for (Application app: applications) {
            if (app.getApplicationId().equals(applicationName)) {
                applicationExists = true;
                applicationId = app.getApplicationId();
            }
        }
        if (!applicationExists) {
            com.wultra.security.powerauth.client.model.response.CreateApplicationResponse response = powerAuthClient.createApplication(applicationName);
            applicationId = response.getApplicationId();
        }

        // Create application version if it does not exist
        final GetApplicationDetailResponse detail = powerAuthClient.getApplicationDetail(applicationId);
        boolean versionExists = false;
        for (ApplicationVersion appVersion: detail.getVersions()) {
            if (appVersion.getApplicationVersionId().equals(applicationVersion)) {
                versionExists = true;
                applicationKey = appVersion.getApplicationKey();
                if (!appVersion.isSupported()) {
                    powerAuthClient.supportApplicationVersion(applicationId, appVersion.getApplicationVersionId());
                }
            }
        }
        if (!versionExists) {
            CreateApplicationVersionResponse versionResponse = powerAuthClient.createApplicationVersion(applicationId, applicationVersion);
            applicationKey = versionResponse.getApplicationKey();
        }

        return applicationId;
    }

    public String createActivation(String userId) throws Exception {
        // Create activations for test
        final InitActivationRequest initRequest = new InitActivationRequest();
        initRequest.setApplicationId(applicationId);
        initRequest.setUserId(userId);
        InitActivationResponse initResponse = powerAuthClient.initActivation(initRequest);

        // Create activation layer 2 request
        final ActivationLayer2Request requestL2 = new ActivationLayer2Request();
        requestL2.setActivationName("Test activation");

        // Create a mock encrypted activation reqeust
        final String protocolVersion = "4.0";
        final EncryptorParameters encryptorParameters = new EncryptorParameters(protocolVersion, applicationKey, null, UUID.randomUUID().toString());
        final AeadSecrets encryptorSecrets = new AeadSecrets(new byte[32], new byte[16]);
        final ClientEncryptor<AeadEncryptedRequest, AeadEncryptedResponse> clientEncryptor = encryptorFactory.getClientEncryptor(EncryptorId.ACTIVATION_LAYER_2, encryptorParameters, encryptorSecrets);
        final ByteArrayOutputStream baosL2 = new ByteArrayOutputStream();
        objectMapper.writeValue(baosL2, requestL2);
        final AeadEncryptedRequest encryptedRequest = clientEncryptor.encryptRequest(baosL2.toByteArray());

        final PrepareActivationRequest prepareRequest = new PrepareActivationRequest();
        prepareRequest.setActivationCode(initResponse.getActivationCode());
        prepareRequest.setApplicationKey(applicationKey);
        prepareRequest.setEncryptedData(encryptedRequest.getEncryptedData());
        prepareRequest.setNonce(encryptedRequest.getNonce());
        prepareRequest.setProtocolVersion(protocolVersion);

        final PrepareActivationResponse prepareResponse = powerAuthClient.prepareActivation(prepareRequest);
        assertNotNull(prepareResponse.getActivationId());

        // Commit activation
        final CommitActivationResponse commitResponse = powerAuthClient.commitActivation(initResponse.getActivationId(), "test");
        assertEquals(initResponse.getActivationId(), commitResponse.getActivationId());

        return initResponse.getActivationId();
    }

    public void blockActivation(String activationId) throws PowerAuthClientException {
        powerAuthClient.blockActivation(activationId, "TEST", "test");
    }

    public void unblockActivation(String activationId) throws PowerAuthClientException {
        powerAuthClient.unblockActivation(activationId, "test");
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getActivationId() {
        return activationId;
    }

    public void setActivationId(String activationId) {
        this.activationId = activationId;
    }

    public String getActivationId2() {
        return activationId2;
    }

    public void setActivationId2(String activationId2) {
        this.activationId2 = activationId2;
    }

    public String getActivationId3() {
        return activationId3;
    }

    public void setActivationId3(String activationId3) {
        this.activationId3 = activationId3;
    }

    public String getActivationId4() {
        return activationId4;
    }

    public void setActivationId4(String activationId4) {
        this.activationId4 = activationId4;
    }
}
