package io.getlime.push.shared;

import io.getlime.push.client.PushServerClient;
import io.getlime.security.powerauth.crypto.lib.config.PowerAuthConfiguration;
import io.getlime.security.powerauth.provider.CryptoProviderUtilFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Security;

@Service
public class PushServerTestClientFactory {

    private static final String TEST_APPLICATION_NAME = "Push_Server_Tests";
    private static final String TEST_APPLICATION_VERSION = "default";
    private static final String TEST_USER_ID = "Test_User";

    @Value("${powerauth.service.url}")
    private String powerAuthServiceUrl;

    public PushServerClient createPushServerClient(String baseUrl) {
        return new PushServerClient(baseUrl);
    }

    public PowerAuthTestClient createPowerAuthTestClient() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        PowerAuthConfiguration.INSTANCE.setKeyConvertor(CryptoProviderUtilFactory.getCryptoProviderUtils());

        PowerAuthTestClient powerAuthTestClient = new PowerAuthTestClient();
        powerAuthTestClient.initializeClient(powerAuthServiceUrl);
        Long applicationId = powerAuthTestClient.initializeApplication(TEST_APPLICATION_NAME, TEST_APPLICATION_VERSION);
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
