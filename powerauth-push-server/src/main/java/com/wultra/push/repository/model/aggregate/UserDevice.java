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
package com.wultra.push.repository.model.aggregate;

import com.wultra.push.repository.model.Platform;
import lombok.Getter;
import lombok.Setter;

/**
 * Object used in sending campaigns, as an aggregate object for storing information about
 * user, device (including platform), app and campaign.
 * <p>
 * The main purpose of this object is a performance optimization. We pull it from multiple
 * database tables in order to minimize the number of required database queries during batch
 * processing.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Getter
@Setter
public class UserDevice {

    /**
     * User ID.
     */
    private String userId;

    /**
     * Device ID.
     */
    private Long deviceId;

    /**
     * Activation ID.
     */
    private String activationId;

    /**
     * Campaign ID.
     */
    private Long campaignId;

    /**
     * Application ID.
     */
    private Long appId;

    /**
     * Platform.
     */
    private Platform platform;

    /**
     * APNs environment (optional).
     */
    private String environment;

    /**
     * Push token.
     */
    private String token;

    /**
     * User device constructor.
     * @param userId User ID.
     * @param deviceId Device ID.
     * @param activationId Activation ID.
     * @param campaignId Campaign ID.
     * @param appId App ID.
     * @param platform Platform.
     * @param environment APNs environment (optional).
     * @param token Push token.
     */
    public UserDevice(String userId, Long deviceId, String activationId, Long campaignId, Long appId, Platform platform, String environment, String token) {
        this.userId = userId;
        this.deviceId = deviceId;
        this.activationId = activationId;
        this.campaignId = campaignId;
        this.appId = appId;
        this.platform = platform;
        this.environment = environment;
        this.token = token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserDevice that = (UserDevice) o;

        if (!campaignId.equals(that.campaignId)) return false;
        if (!appId.equals(that.appId)) return false;
        if (!platform.equals(that.platform)) return false;
        if (environment != that.environment) return false;
        return token.equals(that.token);
    }

    @Override
    public int hashCode() {
        int result = campaignId.hashCode();
        result = 31 * result + appId.hashCode();
        result = 31 * result + platform.hashCode();
        result = 31 * result + environment.hashCode();
        result = 31 * result + token.hashCode();
        return result;
    }
}
