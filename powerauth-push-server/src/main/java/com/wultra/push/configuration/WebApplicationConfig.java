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

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.SerializationFeature;

/**
 * Default Web application configuration.
 *
 * @author Petr Dvorak
 */
@Configuration
public class WebApplicationConfig {

    /**
     * Customize the Jackson 3 JsonMapper to match the application's serialization requirements.
     *
     * @return A customizer for the JsonMapper builder.
     */
    @Bean
    public JsonMapperBuilderCustomizer pushServerJsonMapperCustomizer() {
        return builder -> builder
                .enable(SerializationFeature.INDENT_OUTPUT)
                .changeDefaultPropertyInclusion(v -> JsonInclude.Value.construct(
                        JsonInclude.Include.NON_NULL,
                        JsonInclude.Include.USE_DEFAULTS));
    }

}
