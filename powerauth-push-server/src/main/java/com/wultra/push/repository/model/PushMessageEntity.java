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

package com.wultra.push.repository.model;

import com.wultra.push.repository.converter.PushMessageStatusConverter;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Class representing a database record for a stored push message.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Entity
@Table(name = "push_message")
@Getter
@Setter
public class PushMessageEntity implements Serializable {

    /**
     * Enumeration for push message entity status.
     */
    public enum Status {

        /**
         * Pending
         */
        PENDING(0),

        /**
         * Sent
         */
        SENT(1),

        /**
         * Failed
         */
        FAILED(-1);

        private final int status;

        /**
         * Constructor with status.
         * @param status Status.
         */
        Status(int status) {
            this.status = status;
        }

        /**
         * Get status.
         * @return Status.
         */
        public int getStatus() {
            return status;
        }
    }

    @Serial
    private static final long serialVersionUID = -2570093156350796326L;

    /**
     * Push message ID.
     */
    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "push_message", sequenceName = "push_message_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "push_message")
    private Long id;

    /**
     * Device ID.
     */
    @Column(name = "device_registration_id", nullable = false, updatable = false)
    private Long deviceId;

    /**
     * User ID.
     */
    @Column(name = "user_id", updatable = false)
    private String userId;

    /**
     * Activation ID.
     */
    @Column(name = "activation_id", updatable = false)
    private String activationId;

    /**
     * Flag indicating if the message is silent.
     */
    @Column(name = "is_silent", updatable = false)
    private Boolean silent;

    /**
     * Flag indicating if the message is personal.
     */
    @Column(name = "is_personal", updatable = false)
    private Boolean personal;

    /**
     * Message body.
     */
    @Column(name = "message_body", nullable = false, updatable = false)
    private String messageBody;

    /**
     * Timestamp created.
     */
    @Column(name = "timestamp_created", nullable = false, updatable = false)
    private Date timestampCreated;

    /**
     * Status.
     */
    @Column(name = "status", nullable = false)
    @Convert(converter = PushMessageStatusConverter.class)
    private Status status;

}
