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

import javax.persistence.*;
import java.io.Serializable;

/**
 * Class representing application tokens used to authenticate against APNs or FCM services.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Entity
@Table(name = "push_app_credentials")
public class AppCredentialsEntity implements Serializable {
    private static final long serialVersionUID = -8904070389354612019L;

    /**
     * Entity ID.
     */
    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "push_app_credentials", sequenceName = "push_credentials_seq")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "push_app_credentials")
    private Long id;

    /**
     * App ID.
     */
    @Column(name = "app_id", nullable = false, updatable = false)
    private Long appId;

    /**
     * iOS private key.
     */
    @Column(name = "ios_private_key")
    private byte[] iosPrivateKey;

    /**
     * iOS Team ID.
     */
    @Column(name = "ios_team_id")
    private String iosTeamId;

    /**
     * iOS Key ID.
     */
    @Column(name = "ios_key_id")
    private String iosKeyId;

    /**
     * iOS bundle ID.
     */
    @Column(name = "ios_bundle")
    private String iosBundle;

    /**
     * iOS APNs environment.
     */
    @Column(name = "ios_environment")
    private String iosEnvironment;

    /**
     * Android private key.
     */
    @Column(name = "android_private_key")
    private byte[] androidPrivateKey;

    /**
     * Android project ID.
     */
    @Column(name = "android_project_id")
    private String androidProjectId;

    /**
     * Get credentials ID.
     * @return Credentials ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Set credentials ID
     * @param id Credentials ID
     */
    public void setId(Long id) {
        this.id = id;
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
     * Get iOS bundle ID.
     * @return iOS bundle ID.
     */
    public String getIosBundle() {
        return iosBundle;
    }

    /**
     * Set iOS bundle ID.
     * @param iosBundle iOS bundle ID.
     */
    public void setIosBundle(String iosBundle) {
        this.iosBundle = iosBundle;
    }

    /**
     * Get APNs environment.
     * @return APNs environment.
     */
    public String getIosEnvironment() {
        return iosEnvironment;
    }

    /**
     * Set APNs environment.
     * @param iosEnvironment APNs environment.
     */
    public void setIosEnvironment(String iosEnvironment) {
        this.iosEnvironment = iosEnvironment;
    }

    /**
     * Get iOS private key.
     * @return iOS private key.
     */
    public byte[] getIosPrivateKey() {
        return iosPrivateKey;
    }

    /**
     * Set iOS private key.
     * @param iosPrivateKey iOS private key.
     */
    public void setIosPrivateKey(byte[] iosPrivateKey) {
        this.iosPrivateKey = iosPrivateKey;
    }

    /**
     * Get iOS team ID.
     * @return iOS team ID.
     */
    public String getIosTeamId() {
        return iosTeamId;
    }

    /**
     * Set iOS team ID.
     * @param iosTeamId iOS team ID.
     */
    public void setIosTeamId(String iosTeamId) {
        this.iosTeamId = iosTeamId;
    }

    /**
     * Get iOS key ID.
     * @return iOS key ID.
     */
    public String getIosKeyId() {
        return iosKeyId;
    }

    /**
     * Set iOS key ID.
     * @param iosKeyId iOS key ID.
     */
    public void setIosKeyId(String iosKeyId) {
        this.iosKeyId = iosKeyId;
    }

    /**
     * Get Android private key.
     * @return Android private key.
     */
    public byte[] getAndroidPrivateKey() {
        return androidPrivateKey;
    }

    /**
     * Set Android private key.
     * @param androidPrivateKey Android private key.
     */
    public void setAndroidPrivateKey(byte[] androidPrivateKey) {
        this.androidPrivateKey = androidPrivateKey;
    }

    /**
     * Get Android project ID.
     * @return Android project ID.
     */
    public String getAndroidProjectId() {
        return androidProjectId;
    }

    /**
     * Set Android project ID.
     * @param androidProjectId Android project ID.
     */
    public void setAndroidProjectId(String androidProjectId) {
        this.androidProjectId = androidProjectId;
    }
}
