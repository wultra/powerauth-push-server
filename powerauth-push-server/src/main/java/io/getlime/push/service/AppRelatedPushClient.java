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
package io.getlime.push.service;

import com.eatthepath.pushy.apns.ApnsClient;
import io.getlime.push.repository.model.AppCredentialsEntity;
import io.getlime.push.service.fcm.FcmClient;

/**
 * Class storing app credentials and clients.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class AppRelatedPushClient {

    /**
     * Credentials related to application with given ID.
     */
    private AppCredentialsEntity appCredentials;

    /**
     * APNS client instance, used for iOS applications.
     */
    private ApnsClient apnsClient;

    /**
     * FCM client instance, used for Android applications.
     */
    private FcmClient fcmClient;

    /**
     * Get app credentials.
     * @return App credentials.
     */
    public AppCredentialsEntity getAppCredentials() {
        return appCredentials;
    }

    /**
     * Set app credentials.
     * @param appCredentials App credentials.
     */
    public void setAppCredentials(AppCredentialsEntity appCredentials) {
        this.appCredentials = appCredentials;
    }

    /**
     * Get APNs client.
     * @return APNs client.
     */
    public ApnsClient getApnsClient() {
        return apnsClient;
    }

    /**
     * Set APNs client.
     * @param apnsClient APNs client.
     */
    public void setApnsClient(ApnsClient apnsClient) {
        this.apnsClient = apnsClient;
    }

    /**
     * Get FCM client.
     * @return FCM client.
     */
    public FcmClient getFcmClient() {
        return fcmClient;
    }

    /**
     * Set FCM client.
     * @param fcmClient FCM client.
     */
    public void setFcmClient(FcmClient fcmClient) {
        this.fcmClient = fcmClient;
    }
}
