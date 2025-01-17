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
import io.getlime.push.service.hms.HmsClient;
import lombok.Getter;
import lombok.Setter;

/**
 * Class storing app credentials and clients.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Getter
@Setter
public class AppRelatedPushClient {

    /**
     * Credentials related to application with given ID.
     */
    private AppCredentialsEntity appCredentials;

    /**
     * APNS client instance, used for Apple Push Notification service in development mode.
     */
    private ApnsClient apnsClientDevelopment;

    /**
     * APNS client instance, used for Apple Push Notification service in production mode.
     */
    private ApnsClient apnsClientProduction;

    /**
     * FCM client instance, used for Firebase Cloud Messaging.
     */
    private FcmClient fcmClient;

    /**
     * HMS client instance, used for Huawei Mobile Services.
     */
    private HmsClient hmsClient;

}
