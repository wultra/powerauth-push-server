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

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsAsyncClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.AsyncRestTemplate;

/**
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class FcmClient {

    private final String fcm_url = "https://fcm.googleapis.com/fcm/send";

    private final HttpHeaders headers;
    private final AsyncRestTemplate restTemplate;

    /**
     * Create a new FCM service client.
     * @param serverKey Server key tp be used.
     */
    public FcmClient(String serverKey) {
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "key=" + serverKey);
        restTemplate = new AsyncRestTemplate();
    }

    /**
     * Set information about proxy.
     * @param host Proxy host URL.
     * @param port Proxy port.
     * @param username Proxy username, use 'null' for proxy without authentication.
     * @param password Proxy user password, ignored in case username is 'null'
     */
    public void setProxy(String host, int port, String username, String password) {

        HttpAsyncClientBuilder clientBuilder = HttpAsyncClientBuilder.create();
        clientBuilder.useSystemProperties();
        clientBuilder.setProxy(new HttpHost(host, port));

        if (username != null) {
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            UsernamePasswordCredentials user = new UsernamePasswordCredentials(username, password);
            credsProvider.setCredentials(new AuthScope(host, port), user);
            clientBuilder.setDefaultCredentialsProvider(credsProvider);
            clientBuilder.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());
        }

        CloseableHttpAsyncClient client = clientBuilder.build();

        HttpComponentsAsyncClientHttpRequestFactory factory = new HttpComponentsAsyncClientHttpRequestFactory();
        factory.setHttpAsyncClient(client);

        restTemplate.setAsyncRequestFactory(factory);
    }

    /**
     * Send given FCM request to the server.
     * @param request FCM data request.
     * @return Listenable future for result callbacks.
     */
    public ListenableFuture<ResponseEntity<String>> exchange(FcmSendRequest request) {
        HttpEntity<FcmSendRequest> entity = new HttpEntity<>(request, headers);
        return restTemplate.exchange(fcm_url, HttpMethod.POST, entity, String.class);
    }
}
