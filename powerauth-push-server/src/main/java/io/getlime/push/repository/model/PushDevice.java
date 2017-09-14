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

package io.getlime.push.repository.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Class representing the PowerAuth 2.0 Push Server Device Registration object.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Entity(name = "push_device")
public class PushDevice implements Serializable {

    private static final long serialVersionUID = 1530682530822178192L;

    public static class Platform {
        public static final String iOS = "ios";
        public static final String Android = "android";
    }

    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "push_device", sequenceName = "push_device_seq")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "push_device")
    private Long id;

    @Column(name = "activation_id")
    private String activationId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "app_id", nullable = false, updatable = false)
    private Long appId;

    @Column(name = "platform", nullable = false)
    private String platform;

    @Column(name = "push_token", nullable = false)
    private String pushToken;

    @Column(name = "last_registered", nullable = false)
    private Date lastRegistered;

    @Column(name = "is_active")
    private Boolean active;

    @Column(name = "encryption_key")
    private String encryptionKey;

    @Column(name = "encryption_key_index")
    private String encryptionKeyIndex;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getActivationId() {
        return activationId;
    }

    public void setActivationId(String activationId) {
        this.activationId = activationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getPushToken() {
        return pushToken;
    }

    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }

    public Date getLastRegistered() {
        return lastRegistered;
    }

    public void setLastRegistered(Date lastRegistered) {
        this.lastRegistered = lastRegistered;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public String getEncryptionKeyIndex() {
        return encryptionKeyIndex;
    }

    public void setEncryptionKeyIndex(String encryptionKeyIndex) {
        this.encryptionKeyIndex = encryptionKeyIndex;
    }
}
