/*
 * Copyright (C) 2022 Wultra s.r.o.
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

import io.getlime.push.interceptor.HttpHeaderInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration of interceptors.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Component
@ConditionalOnProperty(
        value = "powerauth.service.correlation-header.enabled",
        havingValue = "true"
)
public class InterceptorConfiguration implements WebMvcConfigurer {

    @Value("${powerauth.service.correlation-header.name:X-Correlation-ID}")
    private String correlationHeaderName;

    @Value("${powerauth.service.correlation-header.value.validation-regexp:[a-zA-Z0-9\\-]{8,1024}}")
    private String correlationHeaderValueValidation;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        final HandlerInterceptor httpHeaderInterceptor = new HttpHeaderInterceptor(correlationHeaderName, correlationHeaderValueValidation);
        registry.addInterceptor(httpHeaderInterceptor);
    }

    /**
     * Get correlation header name.
     * @return Correlation header name.
     */
    public String getCorrelationHeaderName() {
        return correlationHeaderName;
    }
}