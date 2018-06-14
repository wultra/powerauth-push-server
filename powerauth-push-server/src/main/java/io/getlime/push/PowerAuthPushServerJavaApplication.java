/*
 * Copyright 2016 Lime - HighTech Solutions s.r.o.
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
package io.getlime.push;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.LogManager;

/**
 * Spring Boot main class
 */
@SpringBootApplication
@EnableBatchProcessing
public class PowerAuthPushServerJavaApplication {

    static {
        // TODO temporary workaround for Spring boot duplicate logging issue on Tomcat:
        // https://github.com/spring-projects/spring-boot/issues/13470
        Logger rootLogger = LogManager.getLogManager().getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            // By default Tomcat adds ConsoleHandler to root logger which needs to be removed to avoid duplicate logging.
            // Removal of handlers during SLF4J initialization does not happen automatically after migration to Spring boot 2.0.0.
            // Once the issue is fixed in Spring boot project, we should remove this workaround.
            if (handler instanceof ConsoleHandler) {
                rootLogger.removeHandler(handler);
            }
        }
    }

    /**
     * Main method
     *
     * @param args Arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(PowerAuthPushServerJavaApplication.class, args);
    }
}
