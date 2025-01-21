/*
 * Copyright 2021 Wultra s.r.o.
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

package com.wultra.push.model.enumeration;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Enum representing mobile platforms.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public enum MobilePlatform {

    /**
     * iOS Platform.
     *
     * @deprecated use {@link #APNS}
     */
    @JsonProperty("ios")
    @Deprecated
    IOS,

    /**
     * Android Platform.
     *
     * @deprecated use {@link #FCM}
     */
    @JsonProperty("android")
    @Deprecated
    ANDROID,

    /**
     * Huawei Platform.
     *
     * @deprecated use {@link #HMS}
     */
    @JsonProperty("huawei")
    @Deprecated
    HUAWEI,

    /**
     * APNs Platform.
     */
    @JsonProperty("apns")
    APNS,

    /**
     * FCM Platform.
     */
    @JsonProperty("fcm")
    FCM,

    /**
     * HMS Platform.
     */
    @JsonProperty("hms")
    HMS

}
