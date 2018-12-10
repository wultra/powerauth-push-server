/*
 * Copyright 2017 Lime - HighTech Solutions s.r.o.
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

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.firebase.messaging.Message;
import io.getlime.push.configuration.PushServiceConfiguration;
import io.getlime.push.errorhandling.exceptions.FcmInitializationFailedException;
import io.getlime.push.errorhandling.exceptions.FcmMissingTokenException;
import io.getlime.push.service.fcm.model.FcmSuccessResponse;
import io.netty.channel.ChannelOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.ipc.netty.http.client.HttpClientOptions;
import reactor.ipc.netty.options.ClientProxyOptions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.function.Consumer;

/**
 * FCM server client.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class FcmClient {

    private static final Logger logger = LoggerFactory.getLogger(FcmClient.class);

    // FCM URL for posting push messages
    private static final String FCM_URL = "https://fcm.googleapis.com/v1/projects/%s/messages:send";

    // Time buffer for refresh access tokens
    private static final long REFRESH_TOKEN_TIME_BUFFER_SECONDS = 60L;

    // FCM project ID
    private final String projectId;

    // FCM private key for communication with Google backends
    private final byte[] privateKey;

    // FCM send message URL
    private final String fcmSendMessageUrl;

    // Google Credential instance for obtaining access tokens
    private GoogleCredential googleCredential;

    // Push server configuration
    private final PushServiceConfiguration pushServiceConfiguration;

    // FCM converter for model classes
    private final FcmModelConverter fcmConverter;

    // WebClient instance
    private WebClient webClient;

    // Proxy settings
    private String proxyHost;
    private int proxyPort;
    private String proxyUsername;
    private String proxyPassword;

    public FcmClient(String projectId, byte[] privateKey, PushServiceConfiguration pushServiceConfiguration, FcmModelConverter fcmConverter) {
        this.projectId = projectId;
        this.privateKey = privateKey;
        this.pushServiceConfiguration = pushServiceConfiguration;
        this.fcmSendMessageUrl = String.format(FCM_URL, projectId);
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
     * Initialize WebClient instance and configure it based on client configuration.
     */
    public void initializeWebClient() {
        ClientHttpConnector clientHttpConnector = new ReactorClientHttpConnector(options -> {
            HttpClientOptions.Builder optionsBuilder = options
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, pushServiceConfiguration.getFcmConnectTimeout());
            if (proxyHost != null) {
                optionsBuilder.httpProxy((addressSpec -> {
                    ClientProxyOptions.Builder proxyOptionsBuilder = addressSpec.host(proxyHost).port(proxyPort);
                    if (proxyUsername == null) {
                        return proxyOptionsBuilder;
                    } else {
                        return proxyOptionsBuilder.username(proxyUsername).password(s -> proxyPassword);
                    }
                }));
            }
        });
        webClient = WebClient.builder().clientConnector(clientHttpConnector).build();
    }

    /**
     * Initialize Google Credential based on FCM private key.
     * @throws FcmInitializationFailedException In case initialization of Google Credential fails.
     */
    public void initializeGoogleCredential() throws FcmInitializationFailedException {
        try {
            InputStream is = new ByteArrayInputStream(privateKey);
            googleCredential = GoogleCredential
                    .fromStream(is)
                    .createScoped(Collections.singletonList("https://www.googleapis.com/auth/firebase.messaging"));
        } catch (IOException ex) {
            throw new FcmInitializationFailedException("Error occurred while initializing Google Credential using FCM private key: " + ex.getMessage(), ex);
        }
    }

    /**
     * Refresh and retrieve access token for FCM.
     * @return FCM access token.
     * @throws FcmMissingTokenException In case FCM access token cannot be retrieved.
     */
    private String getAccessToken() throws FcmMissingTokenException {
        if (googleCredential == null) {
            // In case FCM registration failed, access token is not available
            throw new FcmMissingTokenException("FCM access token is not available because Google Credential initialization failed");
        }
        try {
            String accessToken = googleCredential.getAccessToken();
            Long expiresIn = googleCredential.getExpiresInSeconds();
            if (accessToken != null && expiresIn != null && expiresIn > REFRESH_TOKEN_TIME_BUFFER_SECONDS) {
                // return existing access token, it is still valid
                return accessToken;
            }
            // refresh access token, it either does not exist or it is expired
            googleCredential.refreshToken();
            return googleCredential.getAccessToken();
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
    public void exchange(Message message, boolean validationOnly, Consumer<FcmSuccessResponse> onSuccess, Consumer<Throwable> onError) throws FcmMissingTokenException {
        if (webClient == null) {
            logger.error("Push message delivery failed because WebClient is not initialized.");
            return;
        }
        if (projectId == null) {
            logger.error("Push message delivery failed because FCM project ID is not configured.");
            return;
        }

        String accessToken = getAccessToken();

        Flux<DataBuffer> body = fcmConverter.convertMessageToFlux(message, validationOnly);
        if (body == null) {
            logger.error("Push message delivery failed because message is invalid.");
            return;
        }

        webClient
                .post()
                .uri(fcmSendMessageUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(BodyInserters.fromDataBuffers(body))
                .retrieve()
                .bodyToMono(FcmSuccessResponse.class)
                .subscribe(onSuccess, onError);
    }

}
