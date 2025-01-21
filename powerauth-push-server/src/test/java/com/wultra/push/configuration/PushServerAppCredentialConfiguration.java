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
package com.wultra.push.configuration;

import com.wultra.push.repository.AppCredentialsRepository;
import com.wultra.push.repository.model.AppCredentialsEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

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
        configure(applicationId, null);
    }

    public void configure(String applicationId, String environment) {
        final AppCredentialsEntity testCredentials;
        if (appCredentialsRepository.findFirstByAppId(applicationId).isPresent()) {
            testCredentials = appCredentialsRepository.findFirstByAppId(applicationId).get();
        } else {
            testCredentials = new AppCredentialsEntity();
        }
        testCredentials.setAppId(applicationId);
        testCredentials.setApnsBundle("test-bundle");
        testCredentials.setApnsPrivateKey("-----BEGIN PRIVATE KEY-----\nMIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQg2Xdp5cQvLRDA+kLZ\nmEICtfn0xvPEwHnMWvc+DGXvkbqhRANCAATBeR4PYm+hF8GqH2gxfu0E8yCmHQpd\nn8AB7B/Yhr5N8T+EdOxcfHRA6ayvUG9dFhuyMi7c2v9Yv8i6cVZmXQgz\n-----END PRIVATE KEY-----\n".getBytes(StandardCharsets.UTF_8));
        testCredentials.setApnsKeyId("test-key");
        testCredentials.setApnsTeamId("test-team-id");
        testCredentials.setApnsEnvironment(environment);
        testCredentials.setFcmProjectId("test-project");
        testCredentials.setFcmPrivateKey(new byte[128]);
        appCredentialsRepository.save(testCredentials);
    }
}
