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
package io.getlime.push.repository.model.aggregate;

/**
 * Object used in sending campaigns, as an aggregate object for storing information about
 * user, device (including platform), app and campaign.
 *
 * The main purpose of this object is a performance optimization. We pull it from multiple
 * database tables in order to minimize the number of required database queries during batch
 * processing.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class UserDevice {

    private String userId;
    private Long deviceId;
    private String activationId;
    private Long campaignId;
    private Long appId;
    private String platform;
    private String token;

    public UserDevice(String userId, Long deviceId, String activationId, Long campaignId, Long appId, String platform, String token) {
        this.userId = userId;
        this.deviceId = deviceId;
        this.activationId = activationId;
        this.campaignId = campaignId;
        this.appId = appId;
        this.platform = platform;
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public String getActivationId() {
        return activationId;
    }

    public void setActivationId(String activationId) {
        this.activationId = activationId;
    }

    public Long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
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
        return token.equals(that.token);
    }

    @Override
    public int hashCode() {
        int result = campaignId.hashCode();
        result = 31 * result + appId.hashCode();
        result = 31 * result + platform.hashCode();
        result = 31 * result + token.hashCode();
        return result;
    }
}
