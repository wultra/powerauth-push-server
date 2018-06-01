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
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.ipc.netty.options.ClientProxyOptions;

import java.util.function.Consumer;

/**
 * FCM server client
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class FcmClient {

    private static final String fcm_url = "https://fcm.googleapis.com/fcm/send";

    private final String serverKey;
    private WebClient webClient;
    private PushServiceConfiguration configuration;

    @Autowired
    public void setPushServiceConfiguration(PushServiceConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Create a new FCM service client.
     * @param serverKey Server key to be used.
     */
    public FcmClient(String serverKey) {
        this.serverKey = serverKey;
        ClientHttpConnector clientHttpConnector = new ReactorClientHttpConnector(options -> options
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, configuration.getFcmConnectTimeout())
        );
        webClient = WebClient.builder().clientConnector(clientHttpConnector).build();
    }

    /**
     * Create a new FCM service client which connects via proxy.
     * @param serverKey Server key to be used.
     * @param host Proxy host URL.
     * @param port Proxy port.
     * @param username Proxy username, use 'null' for proxy without authentication.
     * @param password Proxy user password, ignored in case username is 'null'
     */
    public FcmClient(String serverKey, String host, int port, String username, String password) {
        this.serverKey = serverKey;
        ClientHttpConnector clientHttpConnector = new ReactorClientHttpConnector(options -> options
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, configuration.getFcmConnectTimeout())
                .httpProxy((addressSpec -> {
                    ClientProxyOptions.Builder proxyOptionsBuilder = addressSpec.host(host).port(port);
                    if (username == null) {
                        return proxyOptionsBuilder;
                    } else {
                        return proxyOptionsBuilder.username(username).password(s -> password);
                    }
                })).build());
        webClient = WebClient.builder().clientConnector(clientHttpConnector).build();
    }

    /**
     * Send given FCM request to the server. The method is asynchronous to avoid blocking REST API response.
     * @param request FCM data request.
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
