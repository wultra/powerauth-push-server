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

package io.getlime.push.repository.model;

import jakarta.persistence.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Class representing the PowerAuth 2.0 Push Server Device Registration object.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Entity
@Table(name = "push_device_registration",
       uniqueConstraints = {@UniqueConstraint(columnNames = {"activationId", "pushToken"}),
                            @UniqueConstraint(columnNames = {"activationId"})})
public class PushDeviceRegistrationEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1530682530822178192L;

    /**
     * Platform of the registered device.
     */
    public static class Platform {
        /**
         * iOS Platform
         */
        public static final String iOS = "ios";
        /**
         * Android Platform
         */
        public static final String Android = "android";
    }

    /**
     * Push device ID.
     */
    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "push_device_registration", sequenceName = "push_device_registration_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "push_device_registration")
    private Long id;

    /**
     * Activation ID.
     */
    @Column(name = "activation_id")
    private String activationId;

    /**
     * User ID.
     */
    @Column(name = "user_id")
    private String userId;

    /**
     * App ID.
     */
    @ManyToOne
    @JoinColumn(name = "app_id", referencedColumnName = "id", nullable = false, updatable = false)
    private AppCredentialsEntity appCredentials;

    /**
     * Platform.
     */
    @Column(name = "platform", nullable = false, updatable = false)
    private String platform;

    /**
     * Push token.
     */
    @Column(name = "push_token", nullable = false)
    private String pushToken;

    /**
     * Timestamp last registered.
     */
    @Column(name = "timestamp_last_registered", nullable = false)
    private Date timestampLastRegistered;

    /**
     * Flag indicating if the device is active.
     */
    @Column(name = "is_active")
    private Boolean active;

    /**
     * Get device registration ID.
     * @return Device registration ID.
     */
    public Long getId() {
        return id;
    }

    /**
     * Set device registration ID.
     * @param id Device registration ID.
     */
    public void setId(Long id) {
        this.id = id;
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
     * Get app credentials.
     * @return App credentials.
     */
    public AppCredentialsEntity getAppCredentials() {
        return appCredentials;
    }

    /**
     * Set app credentials.
     * @param appCredentials App credentials
     */
    public void setAppCredentials(AppCredentialsEntity appCredentials) {
        this.appCredentials = appCredentials;
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
    public String getPushToken() {
        return pushToken;
    }

    /**
     * Set push token.
     * @param pushToken Push token.
     */
    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }

    /**
     * Get timestamp last registered.
     * @return Timestamp last registered.
     */
    public Date getTimestampLastRegistered() {
        return timestampLastRegistered;
    }

    /**
     * Set timestamp last registered.
     * @param timestampLastRegistered Timestamp last registered.
     */
    public void setTimestampLastRegistered(Date timestampLastRegistered) {
        this.timestampLastRegistered = timestampLastRegistered;
    }

    /**
     * Get flag indicating if the device registration is active.
     * @return True if the device is active, false otherwise.
     */
    public Boolean getActive() {
        return active;
    }

    /**
     * Set flag indicating if the device registration is active.
     * @param active True if the device is active, false otherwise.
     */
    public void setActive(Boolean active) {
        this.active = active;
    }

}
