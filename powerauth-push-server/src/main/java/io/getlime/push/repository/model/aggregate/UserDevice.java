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
package io.getlime.push.repository.model.aggregate;

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
public class UserDevice {

    private String userId;
    private Long deviceId;
    private String activationId;
    private Long campaignId;
    private Long appId;
    private String platform;
    private String token;

    /**
     * User device constructor.
     * @param userId User ID.
     * @param deviceId Device ID.
     * @param activationId Activation ID.
     * @param campaignId Campaign ID.
     * @param appId App ID.
     * @param platform Platform.
     * @param token Push token.
     */
    public UserDevice(String userId, Long deviceId, String activationId, Long campaignId, Long appId, String platform, String token) {
        this.userId = userId;
        this.deviceId = deviceId;
        this.activationId = activationId;
        this.campaignId = campaignId;
        this.appId = appId;
        this.platform = platform;
        this.token = token;
    }

    /**
     * Get user ID.
     * @return User ID.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Set user ID.
     * @param userId User ID.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Get device ID.
     * @return Device ID.
     */
    public Long getDeviceId() {
        return deviceId;
    }

    /**
     * Set device ID.
     * @param deviceId Device ID.
     */
    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Get activation ID.
     * @return Activation ID.
     */
    public String getActivationId() {
        return activationId;
    }

    /**
     * Set activation ID.
     * @param activationId Activation ID.
     */
    public void setActivationId(String activationId) {
        this.activationId = activationId;
    }

    /**
     * Get campaign ID.
     * @return Campaign ID.
     */
    public Long getCampaignId() {
        return campaignId;
    }

    /**
     * Set campaign ID.
     * @param campaignId Campaign ID.
     */
    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }

    /**
     * Get app ID.
     * @return App ID.
     */
    public Long getAppId() {
        return appId;
    }

    /**
     * Set app ID.
     * @param appId App ID.
     */
    public void setAppId(Long appId) {
        this.appId = appId;
    }

    /**
     * Get platform.
     * @return Platform.
     */
    public String getPlatform() {
        return platform;
    }

    /**
     * Set platform.
     * @param platform Platform.
     */
    public void setPlatform(String platform) {
        this.platform = platform;
    }

    /**
     * Get push token.
     * @return Push token.
     */
    public String getToken() {
        return token;
    }

    /**
     * Set push token.
     * @param token Push token.
     */
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
