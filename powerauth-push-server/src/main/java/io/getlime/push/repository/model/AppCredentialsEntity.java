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
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Class representing application tokens used to authenticate against APNs, FCM, or HMS services.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Entity
@Table(name = "push_app_credentials")
@Getter
@Setter
public class AppCredentialsEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -8904070389354612019L;

    /**
     * Entity ID.
     */
    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "push_app_credentials", sequenceName = "push_credentials_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "push_app_credentials")
    private Long id;

    /**
     * App ID.
     */
    @Column(name = "app_id", nullable = false, updatable = false)
    private String appId;

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
     * Project ID defined in Huawei AppGallery Connect.
     */
    @Column(name = "hms_project_id")
    private String hmsProjectId;

    /**
     * Huawei OAuth 2.0 Client ID.
     */
    @Column(name = "hms_client_id")
    private String hmsClientId;

    /**
     * Huawei OAuth 2.0 Client Secret.
     */
    @Column(name = "hms_client_secret")
    private String hmsClientSecret;

    @Column(name = "timestamp_created", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime timestampCreated = LocalDateTime.now();

    @Column(name = "timestamp_last_updated")
    private LocalDateTime timestampLastUpdated;

}
