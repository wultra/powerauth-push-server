/*
 * Copyright 2020 Wultra s.r.o.
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
package io.getlime.push.configuration;

import io.getlime.push.client.PushServerClient;
import io.getlime.push.client.PushServerClientException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Push server test configuration.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Configuration
public class PushSeverTestConfiguration {

    @Value("${powerauth.push.service.url}")
    private String pushServiceUrl;

    /**
     * Initialize PowerAuth 2.0 Push server client.
     * @return Push server client.
     */
    @Bean
    public PushServerClient pushServerClient() throws PushServerClientException {
        return new PushServerClient(pushServiceUrl);
    }
}
