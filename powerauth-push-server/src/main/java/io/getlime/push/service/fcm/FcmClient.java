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

import io.getlime.push.configuration.PushServiceConfiguration;
import io.getlime.push.service.fcm.model.FcmSendRequest;
import io.getlime.push.service.fcm.model.FcmSendResponse;
import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.ipc.netty.http.client.HttpClientOptions;
import reactor.ipc.netty.options.ClientProxyOptions;

import java.util.function.Consumer;

/**
 * FCM server client
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Service
public class FcmClient {

    // private static final String fcm_url = "https://fcm.googleapis.com/fcm/send";
    private static final String fcm_url = "http://localhost:8080/powerauth-push-server/push/service/test";

    private final PushServiceConfiguration configuration;

    private String serverKey;
    private WebClient webClient;
    private String proxyHost;
    private int proxyPort;
    private String proxyUsername;
    private String proxyPassword;

    @Autowired
    public FcmClient(PushServiceConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Configure server key.
     * @param serverKey Server key to be used.
     */
    public void setServerKey(String serverKey) {
        this.serverKey = serverKey;
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
     * Initialize the FCM client and create WebClient instance based on client configuration.
     */
    public void initialize() {
        ClientHttpConnector clientHttpConnector = new ReactorClientHttpConnector(options -> {
            HttpClientOptions.Builder optionsBuilder = options
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, configuration.getFcmConnectTimeout());
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
     * Send given FCM request to the server. The method is asynchronous to avoid blocking REST API response.
     * @param request FCM data request.
     * @param onSuccess Callback called when request succeeds.
     * @param onError Callback called when request fails.
     */
    @Async
    public void exchange(FcmSendRequest request, Consumer<FcmSendResponse> onSuccess, Consumer<Throwable> onError) {
        webClient
                .post()
                .uri(fcm_url)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "key=" + serverKey)
                .body(BodyInserters.fromObject(request))
                .retrieve()
                .bodyToMono(FcmSendResponse.class)
                .doOnSuccess(onSuccess)
                .doOnError(onError)
                .block();
    }
}
