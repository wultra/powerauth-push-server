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
import com.google.api.client.googleapis.util.Utils;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.JsonParser;
import com.google.common.collect.ImmutableMap;
import com.google.firebase.messaging.Message;
import io.getlime.push.configuration.PushServiceConfiguration;
import io.getlime.push.errorhandling.exceptions.FcmInitializationFailedException;
import io.getlime.push.errorhandling.exceptions.FcmMissingTokenException;
import io.getlime.push.service.PushMessageSenderService;
import io.getlime.push.service.fcm.model.FcmErrorResponse;
import io.getlime.push.service.fcm.model.FcmSuccessResponse;
import io.netty.channel.ChannelOption;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.ipc.netty.http.client.HttpClientOptions;
import reactor.ipc.netty.options.ClientProxyOptions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FCM server client.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class FcmClient {

    // FCM URL parts for posting push messages
    private static final String FCM_URL = "https://fcm.googleapis.com/v1/projects/%s/messages:send";

    // FCM private key for communication with Google backends
    private final byte[] privateKey;

    // FCM project ID
    private final String projectId;

    // FCM send message URL
    private final String fcmSendMessageUrl;

    private static final Map<String, String> FCM_ERROR_CODES = ImmutableMap.<String, String>builder()
            // FCM v1 canonical error codes
            .put("NOT_FOUND", FcmErrorResponse.REGISTRATION_TOKEN_NOT_REGISTERED)
            .put("PERMISSION_DENIED", FcmErrorResponse.MISMATCHED_CREDENTIAL)
            .put("RESOURCE_EXHAUSTED", FcmErrorResponse.MESSAGE_RATE_EXCEEDED)
            .put("UNAUTHENTICATED", FcmErrorResponse.INVALID_APNS_CREDENTIALS)

            // FCM v1 new error codes
            .put("APNS_AUTH_ERROR", FcmErrorResponse.INVALID_APNS_CREDENTIALS)
            .put("INTERNAL", FcmErrorResponse.INTERNAL_ERROR)
            .put("INVALID_ARGUMENT", FcmErrorResponse.INVALID_ARGUMENT)
            .put("QUOTA_EXCEEDED", FcmErrorResponse.MESSAGE_RATE_EXCEEDED)
            .put("SENDER_ID_MISMATCH", FcmErrorResponse.MISMATCHED_CREDENTIAL)
            .put("UNAVAILABLE", FcmErrorResponse.SERVER_UNAVAILABLE)
            .put("UNREGISTERED", FcmErrorResponse.REGISTRATION_TOKEN_NOT_REGISTERED)
            .build();

    // Google Credential instance for obtaining access tokens
    private GoogleCredential googleCredential;

    // Push server configuration
    private final PushServiceConfiguration pushServiceConfiguration;

    // WebClient instance
    private WebClient webClient;

    // Google Json Factory (FCM model classes are not compatible with Jackson)
    private final JsonFactory jsonFactory = Utils.getDefaultJsonFactory();

    // Proxy settings
    private String proxyHost;
    private int proxyPort;
    private String proxyUsername;
    private String proxyPassword;

    public FcmClient(byte[] privateKey, String projectId, PushServiceConfiguration pushServiceConfiguration) {
        this.privateKey = privateKey;
        this.projectId = projectId;
        this.pushServiceConfiguration = pushServiceConfiguration;
        this.fcmSendMessageUrl = String.format(FCM_URL, projectId);
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
            googleCredential.refreshToken();
            return googleCredential.getAccessToken();
        } catch (IOException ex) {
            throw new FcmMissingTokenException("Error occurred while refreshing FCM access token: " + ex.getMessage(), ex);
        }
    }


    /**
     * Send given FCM request to the server. The method is asynchronous to avoid blocking REST API response.
     * @param message FCM message.
     * @param dryRun Whether to perform a dry run.
     * @param onSuccess Callback called when request succeeds.
     * @param onError Callback called when request fails.
     * @throws FcmMissingTokenException Thrown when FCM is not configured.
     */
    public void exchange(Message message, boolean dryRun, Consumer<FcmSuccessResponse> onSuccess, Consumer<Throwable> onError) throws FcmMissingTokenException {
        if (webClient == null) {
            Logger.getLogger(FcmClient.class.getName()).log(Level.SEVERE, "Push message delivery failed because WebClient is not initialized.");
            return;
        }
        if (projectId == null) {
            Logger.getLogger(FcmClient.class.getName()).log(Level.SEVERE, "Push message delivery failed because FCM project ID is not configured.");
            return;
        }

        String accessToken = getAccessToken();

        Flux<DataBuffer> body = convertMessageToPayload(message, dryRun);
        if (body == null) {
            Logger.getLogger(FcmClient.class.getName()).log(Level.SEVERE, "Push message delivery failed because message is invalid.");
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

    /**
     * Converts WebClient Exception to FCM error code.
     * @param exception WebClient response exception.
     * @return FCM error code.
     */
    public String convertErrorToCode(WebClientResponseException exception) {
        FcmErrorResponse response = new FcmErrorResponse();
        String code;
        try {
            String error = exception.getResponseBodyAsString();
            JsonParser parser = jsonFactory.createJsonParser(error);
            parser.parseAndClose(response);
            code = FCM_ERROR_CODES.get(response.getErrorCode());
            if (code == null) {
                code = FcmErrorResponse.UNKNOWN_ERROR;
            }
        } catch (IOException ex) {
            Logger.getLogger(PushMessageSenderService.class.getName()).log(Level.SEVERE, "Error occurred while parsing error: " + ex.getMessage(), ex);
            code = FcmErrorResponse.UNKNOWN_ERROR;
        }
        return code;
    }

    /**
     * Convert Message to payload for WebClient.
     * @param message Message to send.
     * @param dryRun Whether to perform a dry run.
     * @return Flux of DataBuffer.
     */
    private Flux<DataBuffer> convertMessageToPayload(Message message, boolean dryRun) {
        ImmutableMap.Builder<String, Object> payloadBuilder = ImmutableMap.<String, Object>builder().put("message", message);
        if (dryRun) {
            payloadBuilder.put("validate_only", dryRun);
        }
        ImmutableMap<String, Object> payload = payloadBuilder.build();
        String convertedMessage;
        try {
            StringWriter writer = new StringWriter();
            JsonGenerator gen = jsonFactory.createJsonGenerator(writer);
            gen.serialize(payload);
            gen.close();
            convertedMessage = writer.toString();
        } catch (IOException ex) {
            Logger.getLogger(FcmClient.class.getName()).log(Level.SEVERE, "Json serialization failed: "+ex.getMessage(), ex);
            return null;
        }
        DefaultDataBufferFactory factory = new DefaultDataBufferFactory();
        DefaultDataBuffer dataBuffer = factory.wrap(ByteBuffer.wrap(convertedMessage.getBytes(StandardCharsets.UTF_8)));
        return Flux.just(dataBuffer);
    }

}
