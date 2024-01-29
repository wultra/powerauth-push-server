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
@Getter
@Setter
public class PushDeviceRegistrationEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1530682530822178192L;

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
    @Convert(converter = PlatformConverter.class)
    private Platform platform;

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

}
