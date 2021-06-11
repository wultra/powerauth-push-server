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

import io.getlime.push.repository.converter.PushMessageStatusConverter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Class representing a database record for a stored push message.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Entity
@Table(name = "push_message")
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

    private static final long serialVersionUID = -2570093156350796326L;

    /**
     * Push message ID.
     */
    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "push_message", sequenceName = "push_message_seq")
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

    /**
     * Get message ID.
     * @return Message ID.
     */
    public Long getId() {
        return id;
    }

    /**
     * Set message ID.
     * @param id Message ID.
     */
    public void setId(Long id) {
        this.id = id;
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
     * Get info if the message is silent.
     * @return True if the message is silent, false otherwise.
     */
    public Boolean getSilent() {
        return silent;
    }

    /**
     * Set info if the message is silent.
     * @param silent True if the message is silent, false otherwise.
     */
    public void setSilent(Boolean silent) {
        this.silent = silent;
    }

    /**
     * Get info if the message is personal.
     * @return True if the message is personal, false otherwise.
     */
    public Boolean getPersonal() {
        return personal;
    }

    /**
     * Set info if the message is personal.
     * @param personal True if the message is personal, false otherwise.
     */
    public void setPersonal(Boolean personal) {
        this.personal = personal;
    }

    /**
     * Get message body.
     * @return Message body.
     */
    public String getMessageBody() {
        return messageBody;
    }

    /**
     * Set message body.
     * @param messageBody Message body.
     */
    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    /**
     * Get timestamp created.
     * @return Timestamp created.
     */
    public Date getTimestampCreated() {
        return timestampCreated;
    }

    /**
     * Set timestamp created.
     * @param timestampCreated Timestamp created.
     */
    public void setTimestampCreated(Date timestampCreated) {
        this.timestampCreated = timestampCreated;
    }

    /**
     * Get status.
     * @return Status.
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Set status.
     * @param status Status.
     */
    public void setStatus(Status status) {
        this.status = status;
    }
}
