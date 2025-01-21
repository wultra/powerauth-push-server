/*
 * PowerAuth Enrollment Server
 * Copyright (C) 2022 Wultra s.r.o.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.wultra.push.service.http;

import com.wultra.push.configuration.InterceptorConfiguration;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;

/**
 * Service for configuration of HTTP headers in requests.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class HttpCustomizationService {

    private static final MultiValueMap<String, String> EMPTY_MULTI_MAP = new LinkedMultiValueMap<>();

    private InterceptorConfiguration interceptorConfig;

    /**
     * Autowire interceptor configuration.
     * @param interceptorConfig Interceptor configuration.
     */
    @Autowired(required = false)
    public void setCorrelationHeaderConfig(InterceptorConfiguration interceptorConfig) {
        this.interceptorConfig = interceptorConfig;
    }

    /**
     * Get HTTP headers.
     * @return HTTP headers.
     */
    public MultiValueMap<String, String> getHttpHeaders() {
        if (interceptorConfig == null) {
            // By default, no HTTP header customization is done
            return EMPTY_MULTI_MAP;
        }
        // The correlation header is added when the interceptor configuration is enabled
        final String correlationHeaderName = interceptorConfig.getCorrelationHeaderName();
        final MultiValueMap<String, String> headerMap = new LinkedMultiValueMap<>();
        headerMap.put(correlationHeaderName, Collections.singletonList(MDC.get(correlationHeaderName)));
        return headerMap;
    }

    /**
     * Get HTTP query parameters.
     * @return HTTP query parameters.
     */
    public MultiValueMap<String, String> getQueryParams() {
        // By default, no HTTP query parameter customization is done
        return EMPTY_MULTI_MAP;
    }
}
