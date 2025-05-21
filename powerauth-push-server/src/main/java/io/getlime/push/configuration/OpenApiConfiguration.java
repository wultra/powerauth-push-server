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


package io.getlime.push.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.servlet.ServletContext;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger configuration class for api documentation
 *
 * @author Martin Tupy, martin.tupy.work@gmail.com
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "PowerAuth Push Server RESTful API Documentation",
                version = "1.0",
                license = @License(
                        name = "APL 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0"
                ),
                description = "Documentation for the PowerAuth Push Server RESTful API published by the PowerAuth Push Server.",
                contact = @Contact(
                        name = "Wultra s.r.o.",
                        url = "https://www.wultra.com"
                )
        )
)
public class OpenApiConfiguration {

    /**
     * Produces push API group configuration.
     *
     * @return Grouped API configuration.
     */
    @Bean
    public GroupedOpenApi pushApiGroup() {
        String[] packages = {"io.getlime.push.controller.rest"};

        return GroupedOpenApi.builder()
                .group("push")
                .packagesToScan(packages)
                .build();
    }

    @Bean
    public OpenAPI openAPI(final ServletContext servletContext) {
        final Server server = new Server()
                .url(servletContext.getContextPath())
                .description("Default Server URL");
        return new OpenAPI()
                .servers(List.of(server));
    }

}
