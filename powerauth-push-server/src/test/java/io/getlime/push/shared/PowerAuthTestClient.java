package io.getlime.push.shared;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.BaseEncoding;
import io.getlime.powerauth.soap.v3.*;
import io.getlime.security.powerauth.crypto.client.activation.PowerAuthClientActivation;
import io.getlime.security.powerauth.crypto.lib.config.PowerAuthConfiguration;
import io.getlime.security.powerauth.crypto.lib.encryptor.ecies.EciesEncryptor;
import io.getlime.security.powerauth.crypto.lib.encryptor.ecies.EciesFactory;
import io.getlime.security.powerauth.crypto.lib.encryptor.ecies.model.EciesCryptogram;
import io.getlime.security.powerauth.crypto.lib.encryptor.ecies.model.EciesSharedInfo1;
import io.getlime.security.powerauth.provider.CryptoProviderUtil;
import io.getlime.security.powerauth.rest.api.model.request.v3.ActivationLayer2Request;
import io.getlime.security.powerauth.soap.spring.client.PowerAuthServiceClient;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.interfaces.ECPublicKey;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Service
public class PowerAuthTestClient {

    private final PowerAuthClientActivation activation = new PowerAuthClientActivation();
    private final EciesFactory eciesFactory = new EciesFactory();
    private CryptoProviderUtil keyConversion;
    private PowerAuthServiceClient powerAuthClient;

    private Long applicationId;
    private String applicationKey;
    private String applicationSecret;
    private String masterPublicKey;

    private String activationId;
    private String activationId2;
    private String activationId3;
    private String activationId4;

    private ObjectMapper objectMapper = new ObjectMapper();

    public void initializeClient(String powerAuthServiceUrl) {
        powerAuthClient = new PowerAuthServiceClient();
        powerAuthClient.setDefaultUri(powerAuthServiceUrl);
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("io.getlime.powerauth.soap.v3");
        powerAuthClient.setMarshaller(marshaller);
        powerAuthClient.setUnmarshaller(marshaller);
        keyConversion = PowerAuthConfiguration.INSTANCE.getKeyConvertor();
    }

    public Long initializeApplication(String applicationName, String applicationVersion) {
        // Create application if it does not exist
        List<GetApplicationListResponse.Applications> applications = powerAuthClient.getApplicationList();
        boolean applicationExists = false;
        for (io.getlime.powerauth.soap.v3.GetApplicationListResponse.Applications app: applications) {
            if (app.getApplicationName().equals(applicationName)) {
                applicationExists = true;
                applicationId = app.getId();
            }
        }
        if (!applicationExists) {
            io.getlime.powerauth.soap.v3.CreateApplicationResponse response = powerAuthClient.createApplication(applicationName);
            applicationId = response.getApplicationId();
        }

        // Create application version if it does not exist
        io.getlime.powerauth.soap.v3.GetApplicationDetailResponse detail = powerAuthClient.getApplicationDetail(applicationId);
        masterPublicKey = detail.getMasterPublicKey();
        boolean versionExists = false;
        for (GetApplicationDetailResponse.Versions appVersion: detail.getVersions()) {
            if (appVersion.getApplicationVersionName().equals(applicationVersion)) {
                versionExists = true;
                applicationKey = appVersion.getApplicationKey();
                applicationSecret = appVersion.getApplicationSecret();
                if (!appVersion.isSupported()) {
                    powerAuthClient.supportApplicationVersion(appVersion.getApplicationVersionId());
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
        byte[] devicePublicKeyBytes = keyConversion.convertPublicKeyToBytes(deviceKeyPair.getPublic());
        String devicePublicKeyBase64 = BaseEncoding.base64().encode(devicePublicKeyBytes);

        // Create activation layer 2 request which is decryptable only on PowerAuth server
        ActivationLayer2Request requestL2 = new ActivationLayer2Request();
        requestL2.setActivationName("Test activation");
        requestL2.setDevicePublicKey(devicePublicKeyBase64);

        // Encrypt request data using ECIES in application scope with sharedInfo1 = /pa/activation
        byte[] masterKeyBytes = BaseEncoding.base64().decode(masterPublicKey);
        ECPublicKey masterPK = (ECPublicKey) keyConversion.convertBytesToPublicKey(masterKeyBytes);
        byte[] applicationSecretBytes = applicationSecret.getBytes(StandardCharsets.UTF_8);

        EciesEncryptor eciesEncryptorL2 = eciesFactory.getEciesEncryptorForApplication(masterPK, applicationSecretBytes, EciesSharedInfo1.ACTIVATION_LAYER_2);
        ByteArrayOutputStream baosL2 = new ByteArrayOutputStream();
        objectMapper.writeValue(baosL2, requestL2);
        EciesCryptogram eciesCryptogramL2 = eciesEncryptorL2.encryptRequest(baosL2.toByteArray(), true);

        String ephemeralPublicKey = BaseEncoding.base64().encode(eciesCryptogramL2.getEphemeralPublicKey());
        String encryptedData = BaseEncoding.base64().encode(eciesCryptogramL2.getEncryptedData());
        String mac = BaseEncoding.base64().encode(eciesCryptogramL2.getMac());
        String nonce = BaseEncoding.base64().encode(eciesCryptogramL2.getNonce());

        // Prepare activation
        PrepareActivationResponse prepareResponse = powerAuthClient.prepareActivation(initResponse.getActivationCode(), applicationKey, ephemeralPublicKey, encryptedData, mac, nonce);
        assertNotNull(prepareResponse.getActivationId());

        // Commit activation
        CommitActivationResponse commitResponse = powerAuthClient.commitActivation(initResponse.getActivationId(), "test");
        assertEquals(initResponse.getActivationId(), commitResponse.getActivationId());

        return initResponse.getActivationId();
    }

    public void blockActivation(String activationId) {
        powerAuthClient.blockActivation(activationId, "TEST", "test");
    }

    public void unblockActivation(String activationId) {
        powerAuthClient.unblockActivation(activationId, "test");
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
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
