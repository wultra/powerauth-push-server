/*
 * Copyright 2022 Wultra s.r.o.
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

import io.getlime.push.repository.AppCredentialsRepository;
import io.getlime.push.repository.model.AppCredentialsEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Configuration class for push server app credentials.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Component
public class PushServerAppCredentialConfiguration {

    private final AppCredentialsRepository appCredentialsRepository;

    @Autowired
    public PushServerAppCredentialConfiguration(AppCredentialsRepository appCredentialsRepository) {
        this.appCredentialsRepository = appCredentialsRepository;
    }

    public void configure(String applicationId) {
        if (appCredentialsRepository.findFirstByAppId(applicationId).isEmpty()) {
            final AppCredentialsEntity testCredentials = new AppCredentialsEntity();
            testCredentials.setAppId(applicationId);
            testCredentials.setApnsBundle("test-bundle");
            testCredentials.setFcmProjectId("test-project");
            testCredentials.setFcmPrivateKey(new byte[128]);
            appCredentialsRepository.save(testCredentials);
        }
    }
}
