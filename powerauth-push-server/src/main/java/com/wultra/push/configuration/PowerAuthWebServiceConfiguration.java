/*
 * Copyright 2016 Wultra s.r.o.
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
package com.wultra.push.configuration;

import com.wultra.security.powerauth.client.PowerAuthClient;
import com.wultra.security.powerauth.client.model.error.PowerAuthClientException;
import com.wultra.security.powerauth.rest.client.PowerAuthRestClient;
import com.wultra.security.powerauth.rest.client.PowerAuthRestClientConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Default PowerAuth Service configuration.
 *
 * @author Petr Dvorak
 */
@Configuration
@ComponentScan(basePackages = {"com.wultra.security", "com.wultra.push"})
@Slf4j
public class PowerAuthWebServiceConfiguration {

    @Value("${powerauth.service.url}")
    private String powerAuthRestUrl;

    @Value("${powerauth.service.restClientConfig.responseTimeout}")
    private Duration powerAuthServiceTimeout;

    @Value("${powerauth.service.restClientConfig.maxIdleTime}")
    private Duration powerAuthServiceMaxIdleTime;

    @Value("${powerauth.service.ssl.acceptInvalidSslCertificate}")
    private boolean acceptInvalidSslCertificate;

    @Value("${powerauth.service.security.clientToken}")
    private String clientToken;

    @Value("${powerauth.service.security.clientSecret}")
    private String clientSecret;

    /**
     * Initialize PowerAuth REST client.
     * @return PowerAuth REST client.
     */
    @Bean
    public PowerAuthClient powerAuthClient() throws PowerAuthClientException {
        logger.info("Configuring PowerAuthRestClient for URL: {}", powerAuthRestUrl);
        final PowerAuthRestClientConfiguration config = new PowerAuthRestClientConfiguration();
        config.setResponseTimeout(powerAuthServiceTimeout);
        config.setMaxIdleTime(powerAuthServiceMaxIdleTime);
        config.setPowerAuthClientToken(clientToken);
        config.setPowerAuthClientSecret(clientSecret);
        config.setAcceptInvalidSslCertificate(acceptInvalidSslCertificate);
        return new PowerAuthRestClient(powerAuthRestUrl, config);
    }

}
