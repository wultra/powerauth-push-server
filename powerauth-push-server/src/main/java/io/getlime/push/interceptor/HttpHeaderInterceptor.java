/*
 * PowerAuth Enrollment Server
 * Copyright (C) 2022 Wultra s.r.o.
 *
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
package io.getlime.push.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * HTTP header interceptor for logging of correlation headers using MDC.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class HttpHeaderInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(HttpHeaderInterceptor.class);

    private final String correlationHeaderName;
    private final String correlationHeaderValueValidation;

    /**
     * HTTP header interceptor constructor.
     * @param correlationHeaderName Correlation header name.
     * @param correlationHeaderValueValidation Correlation header value validation.
     */
    public HttpHeaderInterceptor(String correlationHeaderName, String correlationHeaderValueValidation) {
        this.correlationHeaderName = correlationHeaderName;
        this.correlationHeaderValueValidation = correlationHeaderValueValidation;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        MDC.put(correlationHeaderName, getCorrelationId(request));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        MDC.remove(correlationHeaderName);
    }

    private String getCorrelationId(HttpServletRequest request) {
        final String headerValue = request.getHeader(correlationHeaderName);
        if (headerValue == null) {
            logger.debug("Correlation header {} is null", correlationHeaderName);
            return null;
        }
        if (!headerValue.matches(correlationHeaderValueValidation)) {
            logger.warn("Correlation header {} is invalid: {}", correlationHeaderName, headerValue);
            return null;
        }
        return headerValue;
    }
}