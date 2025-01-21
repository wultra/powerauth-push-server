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
package com.wultra.push.model.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import com.wultra.push.model.enumeration.ApnsEnvironment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for {@link UpdateApnsRequest}.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
class UpdateApnsRequestTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testEnvironmentNull() throws Exception {
        final String json = """
                {"environment":null}
                """;

        final UpdateApnsRequest result = objectMapper.readValue(json, UpdateApnsRequest.class);

        assertNull(result.getEnvironment());
    }

    @Test
    void testEnvironmentInvalid() {
        final String json = """
                {"environment":"invalid"}
                """;

        assertThrows(ValueInstantiationException.class, () ->
                objectMapper.readValue(json, UpdateApnsRequest.class));
    }

    @ParameterizedTest
    @CsvSource({"development,DEVELOPMENT", "DEVELOPMENT,DEVELOPMENT", "production,PRODUCTION", "PRODUCTION,PRODUCTION"})
    void testEnvironment(final String jsonParam, final ApnsEnvironment expected) throws Exception {
        final String json = """
                {"environment":"%s"}
                """.formatted(jsonParam);

        final UpdateApnsRequest result = objectMapper.readValue(json, UpdateApnsRequest.class);

        assertEquals(expected, result.getEnvironment());
    }
}
