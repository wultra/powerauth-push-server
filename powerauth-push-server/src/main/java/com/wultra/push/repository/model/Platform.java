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
package com.wultra.push.repository.model;

/**
 * Platform enum for {@link PushDeviceRegistrationEntity#getPlatform()}.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
public enum Platform {

    /**
     * iOS Platform.
     *
     * @deprecated use {@link #APNS}
     */
    @Deprecated
    IOS,

    /**
     * Android Platform.
     *
     * @deprecated use {@link #FCM}
     */
    @Deprecated
    ANDROID,

    /**
     * Huawei Platform.
     *
     * @deprecated use {$link HMS}
     */
    @Deprecated
    HUAWEI,

    /**
     * Apple Push Notification service (APNs) platform.
     */
    APNS,

    /**
     * Google Firebase Cloud Message (FCM) platform.
     */
    FCM,

    /**
     * Huawei Mobile Services (HMS) platform.
     */
    HMS

}
