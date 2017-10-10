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
package io.getlime.push.service;

import com.turo.pushy.apns.ApnsClient;
import io.getlime.push.repository.model.AppCredentialEntity;
import io.getlime.push.service.fcm.FcmClient;

/**
 * Class storing app credentials and clients
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class AppRelatedPushClient {

    private AppCredentialEntity appCredentials;
    private ApnsClient apnsClient;
    private FcmClient fcmClient;

    public AppCredentialEntity getAppCredentials() {
        return appCredentials;
    }

    public void setAppCredentials(AppCredentialEntity appCredentials) {
        this.appCredentials = appCredentials;
    }

    public ApnsClient getApnsClient() {
        return apnsClient;
    }

    public void setApnsClient(ApnsClient apnsClient) {
        this.apnsClient = apnsClient;
    }

    public FcmClient getFcmClient() {
        return fcmClient;
    }

    public void setFcmClient(FcmClient fcmClient) {
        this.fcmClient = fcmClient;
    }
}
