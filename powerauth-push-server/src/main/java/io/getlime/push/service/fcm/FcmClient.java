/*
 * Copyright 2017 Wultra s.r.o.
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

package io.getlime.push.service.fcm;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.messaging.Message;
import com.wultra.core.rest.client.base.DefaultRestClient;
import com.wultra.core.rest.client.base.RestClient;
import com.wultra.core.rest.client.base.RestClientException;
import io.getlime.push.configuration.PushServiceConfiguration;
import io.getlime.push.errorhandling.exceptions.FcmInitializationFailedException;
import io.getlime.push.errorhandling.exceptions.FcmMissingTokenException;
import io.getlime.push.errorhandling.exceptions.PushServerException;
import io.getlime.push.service.fcm.model.FcmSuccessResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.time.Duration;
import java.util.Collections;
import java.util.function.Consumer;

/**
 * FCM server client.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class FcmClient {

    private static final Logger logger = LoggerFactory.getLogger(FcmClient.class);

    // FCM project ID
    private final String projectId;

    // FCM private key for communication with Google backends
    private final byte[] privateKey;

    // FCM send message URL
    private String fcmSendMessageUrl;

    // Google Credentials instance for obtaining access tokens
    private GoogleCredentials googleCredentials;

    // Push server configuration
    private final PushServiceConfiguration pushServiceConfiguration;

    // FCM converter for model classes
    private final FcmModelConverter fcmConverter;

    // RestClient instance
    private RestClient restClient;

    // Proxy settings
    private String proxyHost;
    private int proxyPort;
    private String proxyUsername;
    private String proxyPassword;

    /**
     * Constructor with FCM specific attributes.
     * @param projectId Android Project ID.
     * @param privateKey Android Private Key.
     * @param pushServiceConfiguration Push service configuration.
     * @param fcmConverter FCM model converter helper.
     */
    public FcmClient(String projectId, byte[] privateKey, PushServiceConfiguration pushServiceConfiguration, FcmModelConverter fcmConverter) {
        this.projectId = projectId;
        this.privateKey = privateKey;
        this.pushServiceConfiguration = pushServiceConfiguration;
        this.fcmConverter = fcmConverter;
    }


    /**
     * Configure proxy settings.
     * @param proxyHost Proxy host.
     * @param proxyPort Proxy proxy.
     * @param proxyUsername Proxy username, use 'null' for proxy without authentication.
     * @param proxyPassword Proxy user password, ignored in case username is 'null'.
     */
    public void setProxySettings(String proxyHost, int proxyPort, String proxyUsername, String proxyPassword) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyUsername = proxyUsername;
        this.proxyPassword = proxyPassword;
    }

    /**
     * Initialize RestClient instance and configure it based on client configuration.
     * @throws PushServerException Thrown in case REST client initialization fails.
     */
    public void initializeRestClient() throws PushServerException {
        DefaultRestClient.Builder builder = DefaultRestClient.builder()
                .connectionTimeout(Duration.ofMillis(pushServiceConfiguration.getFcmConnectTimeout()));
        if (proxyHost != null) {
            DefaultRestClient.ProxyBuilder proxyBuilder = builder.proxy().host(proxyHost).port(proxyPort);
            if (proxyUsername != null) {
                proxyBuilder.username(proxyUsername).password(proxyPassword);
            }
        }
        try {
            restClient = builder.build();
        } catch (RestClientException ex) {
            throw new PushServerException("REST client initialization failed", ex);
        }
    }

    /**
     * Set the FCM send message endpoint URL.
     * @param fcmSendMessageUrl FCM send message endpoint URL.
     */
    public void setFcmSendMessageUrl(String fcmSendMessageUrl) {
        this.fcmSendMessageUrl = fcmSendMessageUrl;
    }

    /**
     * Initialize Google Credential based on FCM private key.
     * @throws FcmInitializationFailedException In case initialization of Google Credential fails.
     */
    public void initializeGoogleCredential() throws FcmInitializationFailedException {
        try {
            InputStream is = new ByteArrayInputStream(privateKey);
            HttpTransport httpTransport;
            if (proxyHost != null) {
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
                httpTransport = new NetHttpTransport.Builder().setProxy(proxy).build();
                if (proxyUsername != null && proxyPassword != null) {
                    setProxyAuthentication();
                }
            } else {
                httpTransport = new NetHttpTransport.Builder().build();
            }
            googleCredentials = GoogleCredentials
                    .fromStream(is, () -> httpTransport)
                    .createScoped(Collections.singletonList("https://www.googleapis.com/auth/firebase.messaging"));
            googleCredentials.refresh();
        } catch (IOException ex) {
            throw new FcmInitializationFailedException("Error occurred while initializing Google Credential using FCM private key: " + ex.getMessage(), ex);
        }
    }

    /**
     * Set proxy authentication for FCM.
     */
    private void setProxyAuthentication() {
        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                if (getRequestorType() == RequestorType.PROXY) {
                    if (getRequestingHost().equals(proxyHost) && getRequestingPort() == proxyPort) {
                        return new PasswordAuthentication(proxyUsername, proxyPassword.toCharArray());
                    }
                }
                return null;
            }
        });
    }

    /**
     * Refresh and retrieve access token for FCM.
     * @return FCM access token.
     * @throws FcmMissingTokenException In case FCM access token cannot be retrieved.
     */
    private AccessToken getAccessToken() throws FcmMissingTokenException {
        if (googleCredentials == null) {
            // In case FCM registration failed, access token is not available.
            // Exception is not thrown to allow test execution.
            return null;
        }
        try {
            googleCredentials.refreshIfExpired();
            return googleCredentials.getAccessToken();
        } catch (IOException ex) {
            throw new FcmMissingTokenException("Error occurred while refreshing FCM access token: " + ex.getMessage(), ex);
        }
    }

    /**
     * Send given FCM request to the server. The method is asynchronous to avoid blocking REST API response.
     * @param message FCM message.
     * @param validationOnly Whether to perform only validation.
     * @param onSuccess Callback called when request succeeds.
     * @param onError Callback called when request fails.
     * @throws FcmMissingTokenException Thrown when FCM is not configured.
     */
    public void exchange(Message message, boolean validationOnly, Consumer<ResponseEntity<FcmSuccessResponse>> onSuccess, Consumer<Throwable> onError) throws FcmMissingTokenException {
        if (restClient == null) {
            logger.error("Push message delivery failed because RestClient is not initialized.");
            return;
        }
        if (projectId == null) {
            logger.error("Push message delivery failed because FCM project ID is not configured.");
            return;
        }
        if (fcmSendMessageUrl == null) {
            logger.error("Push message delivery failed because FCM send message URL is not configured.");
            return;
        }

        Flux<DataBuffer> body = fcmConverter.convertMessageToFlux(message, validationOnly);
        if (body == null) {
            logger.error("Push message delivery failed because message is invalid.");
            return;
        }

        AccessToken accessToken = getAccessToken();
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        if (accessToken != null) {
            headers.add("Authorization", "Bearer " + accessToken.getTokenValue());
        }

        try {
            ParameterizedTypeReference<FcmSuccessResponse> responseType = new ParameterizedTypeReference<>(){};
            restClient.postNonBlocking(fcmSendMessageUrl, body, null, headers, responseType, onSuccess, onError);
        } catch (RestClientException ex) {
            logger.debug(ex.getMessage(), ex);
            logger.error("Push message delivery failed because of a RestClient error: " + ex.getMessage());
        }
    }

}
