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
package io.getlime.push.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wultra.security.powerauth.client.PowerAuthClient;
import com.wultra.security.powerauth.client.model.entity.Application;
import com.wultra.security.powerauth.client.model.entity.ApplicationVersion;
import com.wultra.security.powerauth.client.model.error.PowerAuthClientException;
import com.wultra.security.powerauth.client.model.request.InitActivationRequest;
import com.wultra.security.powerauth.client.model.request.PrepareActivationRequest;
import com.wultra.security.powerauth.client.model.response.CommitActivationResponse;
import com.wultra.security.powerauth.client.model.response.CreateApplicationVersionResponse;
import com.wultra.security.powerauth.client.model.response.InitActivationResponse;
import com.wultra.security.powerauth.client.model.response.PrepareActivationResponse;
import com.wultra.security.powerauth.rest.client.PowerAuthRestClient;
import io.getlime.push.api.PowerAuthTestClient;
import io.getlime.security.powerauth.crypto.client.activation.PowerAuthClientActivation;
import io.getlime.security.powerauth.crypto.lib.encryptor.ClientEncryptor;
import io.getlime.security.powerauth.crypto.lib.encryptor.EncryptorFactory;
import io.getlime.security.powerauth.crypto.lib.encryptor.model.EncryptedRequest;
import io.getlime.security.powerauth.crypto.lib.encryptor.model.EncryptorId;
import io.getlime.security.powerauth.crypto.lib.encryptor.model.EncryptorParameters;
import io.getlime.security.powerauth.crypto.lib.encryptor.model.v3.ClientEncryptorSecrets;
import io.getlime.security.powerauth.crypto.lib.util.KeyConvertor;
import io.getlime.security.powerauth.rest.api.model.request.ActivationLayer2Request;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.interfaces.ECPublicKey;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * REST test client for PowerAuth server.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class PowerAuthTestClientRest implements PowerAuthTestClient {

    private final PowerAuthClientActivation activation = new PowerAuthClientActivation();
    private final EncryptorFactory encryptorFactory = new EncryptorFactory();
    private final KeyConvertor keyConvertor = new KeyConvertor();
    private PowerAuthClient powerAuthClient;

    private String applicationId;
    private String applicationKey;
    private String applicationSecret;
    private String masterPublicKey;

    private String activationId;
    private String activationId2;
    private String activationId3;
    private String activationId4;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void initializeClient(String powerAuthRestUrl) throws PowerAuthClientException {
        powerAuthClient = new PowerAuthRestClient(powerAuthRestUrl);
    }

    public String initializeApplication(String applicationName, String applicationVersion) throws PowerAuthClientException {
        // Create application if it does not exist
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
        com.wultra.security.powerauth.client.model.response.GetApplicationDetailResponse detail = powerAuthClient.getApplicationDetail(applicationId);
        masterPublicKey = detail.getMasterPublicKey();
        boolean versionExists = false;
        for (ApplicationVersion appVersion: detail.getVersions()) {
            if (appVersion.getApplicationVersionId().equals(applicationVersion)) {
                versionExists = true;
                applicationKey = appVersion.getApplicationKey();
                applicationSecret = appVersion.getApplicationSecret();
                if (!appVersion.isSupported()) {
                    powerAuthClient.supportApplicationVersion(applicationId, appVersion.getApplicationVersionId());
                }
            }
        }
        if (!versionExists) {
            CreateApplicationVersionResponse versionResponse = powerAuthClient.createApplicationVersion(applicationId, applicationVersion);
            applicationKey = versionResponse.getApplicationKey();
            applicationSecret = versionResponse.getApplicationSecret();
        }

        return applicationId;
    }

    public String createActivation(String userId) throws Exception {
        // Create activations for test
        InitActivationRequest initRequest = new InitActivationRequest();
        initRequest.setApplicationId(applicationId);
        initRequest.setUserId(userId);
        InitActivationResponse initResponse = powerAuthClient.initActivation(initRequest);

        // Generate device key pair
        KeyPair deviceKeyPair = activation.generateDeviceKeyPair();
        byte[] devicePublicKeyBytes = keyConvertor.convertPublicKeyToBytes(deviceKeyPair.getPublic());
        final String devicePublicKeyBase64 = Base64.getEncoder().encodeToString(devicePublicKeyBytes);

        // Create activation layer 2 request which is decryptable only on PowerAuth server
        final ActivationLayer2Request requestL2 = new ActivationLayer2Request();
        requestL2.setActivationName("Test activation");
        requestL2.setDevicePublicKey(devicePublicKeyBase64);

        // Encrypt request data using ECIES in application scope with sharedInfo1 = /pa/activation
        final byte[] masterKeyBytes = Base64.getDecoder().decode(masterPublicKey);
        ECPublicKey masterPK = (ECPublicKey) keyConvertor.convertBytesToPublicKey(masterKeyBytes);
        byte[] applicationSecretBytes = applicationSecret.getBytes(StandardCharsets.UTF_8);

        final String protocolVersion = "3.1";
        final EncryptorParameters encryptorParameters = new EncryptorParameters(protocolVersion, applicationKey, null, null);
        final ClientEncryptorSecrets encryptorSecrets = new ClientEncryptorSecrets(masterPK, applicationSecretBytes);
        final ClientEncryptor clientEncryptor = encryptorFactory.getClientEncryptor(EncryptorId.ACTIVATION_LAYER_2, encryptorParameters, encryptorSecrets);
        final ByteArrayOutputStream baosL2 = new ByteArrayOutputStream();
        objectMapper.writeValue(baosL2, requestL2);
        final EncryptedRequest encryptedRequest = clientEncryptor.encryptRequest(baosL2.toByteArray());

        final PrepareActivationRequest prepareRequest = new PrepareActivationRequest();
        prepareRequest.setActivationCode(initResponse.getActivationCode());
        prepareRequest.setApplicationKey(applicationKey);
        prepareRequest.setEphemeralPublicKey(encryptedRequest.getEphemeralPublicKey());
        prepareRequest.setEncryptedData(encryptedRequest.getEncryptedData());
        prepareRequest.setMac(encryptedRequest.getMac());
        prepareRequest.setNonce(encryptedRequest.getNonce());
        prepareRequest.setProtocolVersion(protocolVersion);

        final PrepareActivationResponse prepareResponse = powerAuthClient.prepareActivation(prepareRequest);
        assertNotNull(prepareResponse.getActivationId());

        // Commit activation
        CommitActivationResponse commitResponse = powerAuthClient.commitActivation(initResponse.getActivationId(), "test");
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
