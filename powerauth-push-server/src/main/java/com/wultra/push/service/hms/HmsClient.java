/*
 * Copyright 2024 Wultra s.r.o.
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
package com.wultra.push.service.hms;

import com.wultra.push.configuration.PushServiceConfiguration;
import com.wultra.push.repository.model.AppCredentialsEntity;
import com.wultra.push.service.hms.request.Message;
import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ClientCredentialsReactiveOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.transport.ProxyProvider;

import java.time.Duration;
import java.util.Map;

/**
 * HMS (Huawei Mobile Services) server client.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
@Slf4j
public class HmsClient {

    private static final String OAUTH_REGISTRATION_ID = "hms";
    private static final String OAUTH_USER_AGENT = "Wultra Push-Server";

    /**
     * Success error code.
     *
     * @see <a href="https://developer.huawei.com/consumer/en/doc/HMSCore-References/https-send-api-0000001050986197#section13968115715131">HMS Documentation</a>
     */
    public static final String SUCCESS_CODE = "80000000";

    final WebClient webClient;
    final String messageUrl;

    public HmsClient(final PushServiceConfiguration pushServiceConfiguration, final AppCredentialsEntity credentials) {
        webClient = createWebClient(credentials.getHmsClientId(), credentials.getHmsClientSecret(), pushServiceConfiguration);
        final String projectId = credentials.getHmsProjectId();
        messageUrl = String.format(pushServiceConfiguration.getHmsSendMessageUrl(), projectId);
    }

    public Mono<HmsSendResponse> sendMessage(final Message message, final boolean validationOnly) {
        final Map<String, Object> body = Map.of("validate_only", validationOnly, "message", message);
        return webClient.post()
                .uri(messageUrl)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(HmsSendResponse.class);
    }

    private static WebClient createWebClient(
            final String oAuthClientId,
            final String oAuthClientSecret,
            final PushServiceConfiguration configuration) {

        final AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager authorizedClientManager = authorizedClientServiceReactiveOAuth2AuthorizedClientManager(oAuthClientId, oAuthClientSecret, configuration);

        final ServerOAuth2AuthorizedClientExchangeFilterFunction oAuth2ExchangeFilterFunction = new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oAuth2ExchangeFilterFunction.setDefaultClientRegistrationId(OAUTH_REGISTRATION_ID);

        return createWebClient(oAuth2ExchangeFilterFunction, configuration);
    }

    private static AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager authorizedClientServiceReactiveOAuth2AuthorizedClientManager(
            final String oAuthClientId,
            final String oAuthClientSecret,
            final PushServiceConfiguration configuration) {

        final ClientRegistration clientRegistration = ClientRegistration.withRegistrationId(OAUTH_REGISTRATION_ID)
                .tokenUri(configuration.getHmsTokenUrl())
                .clientName(OAUTH_USER_AGENT)
                .clientId(oAuthClientId)
                .clientSecret(oAuthClientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .build();

        final ReactiveClientRegistrationRepository clientRegistrations = new InMemoryReactiveClientRegistrationRepository(clientRegistration);
        final ReactiveOAuth2AuthorizedClientService clientService = new InMemoryReactiveOAuth2AuthorizedClientService(clientRegistrations);

        final ClientCredentialsReactiveOAuth2AuthorizedClientProvider authorizedClientProvider = new ClientCredentialsReactiveOAuth2AuthorizedClientProvider();

        final AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(clientRegistrations, clientService);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
        return authorizedClientManager;
    }

    private static WebClient createWebClient(final ExchangeFilterFunction filter, PushServiceConfiguration configuration) {
        final Duration connectionTimeout = configuration.getHmsConnectTimeout();
        final Duration responseTimeout = configuration.getHmsResponseTimeout();
        final Duration maxIdleTime = configuration.getHmsMaxIdleTime();
        logger.info("Setting connectionTimeout: {}, responseTimeout: {}, maxIdleTime: {}", connectionTimeout, responseTimeout, maxIdleTime);

        final ConnectionProvider connectionProvider = ConnectionProvider.builder("custom")
                .maxIdleTime(maxIdleTime)
                .build();
        HttpClient httpClient = HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Math.toIntExact(connectionTimeout.toMillis()))
                .responseTimeout(responseTimeout);

        if (configuration.isHmsProxyEnabled()) {
            logger.debug("Configuring proxy {}:{}", configuration.getHmsProxyHost(), configuration.getHmsProxyPort());
            httpClient = httpClient.proxy(proxySpec -> {
                final ProxyProvider.Builder proxyBuilder = proxySpec
                        .type(ProxyProvider.Proxy.HTTP)
                        .host(configuration.getHmsProxyHost())
                        .port(configuration.getHmsProxyPort());
                if (StringUtils.isNotBlank(configuration.getHmsProxyUsername())) {
                    proxyBuilder.username(configuration.getHmsProxyUsername());
                    proxyBuilder.password(s -> configuration.getHmsProxyPassword());
                }

                proxyBuilder.build();
            });
        }

        final ReactorClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);

        return WebClient.builder()
                .clientConnector(connector)
                .filter(filter)
                .build();
    }

}
