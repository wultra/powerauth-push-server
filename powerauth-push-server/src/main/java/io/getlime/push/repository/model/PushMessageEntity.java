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

import io.getlime.push.repository.converter.PushMessageStatusConverter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Class representing a database record for a stored push message.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@Entity
@Table(name = "push_message")
public class PushMessageEntity implements Serializable {

    public enum Status {
        PENDING(0),
        SENT(1),
        FAILED(-1);

        private final int status;

        Status(int status) {
            this.status = status;
        }

        public int getStatus() {
            return status;
        }
    }

    private static final long serialVersionUID = -2570093156350796326L;

    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "push_message", sequenceName = "push_message_seq")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "push_message")
    private Long id;

    @Column(name = "device_registration_id", nullable = false, updatable = false)
    private Long deviceRegistrationId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private String userId;

    @Column(name = "activation_id", updatable = false)
    private String activationId;

    @Column(name = "silent", nullable = false, updatable = false)
    private Boolean silent;

    @Column(name = "personal", nullable = false, updatable = false)
    private Boolean personal;

    @Column(name = "encrypted", nullable = false, updatable = false)
    private Boolean encrypted;

    @Column(name = "message_body", nullable = false, updatable = false)
    private String messageBody;

    @Column(name = "timestamp_created", nullable = false, updatable = false)
    private Date timestampCreated;

    @Column(name = "status", nullable = false)
    @Convert(converter = PushMessageStatusConverter.class)
    private Status status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDeviceRegistrationId() {
        return deviceRegistrationId;
    }

    public void setDeviceRegistrationId(Long deviceRegistrationId) {
        this.deviceRegistrationId = deviceRegistrationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getActivationId() {
        return activationId;
    }

    public void setActivationId(String activationId) {
        this.activationId = activationId;
    }

    public Boolean getSilent() {
        return silent;
    }

    public void setSilent(Boolean silent) {
        this.silent = silent;
    }

    public Boolean getPersonal() {
        return personal;
    }

    public void setPersonal(Boolean personal) {
        this.personal = personal;
    }

    public Boolean getEncrypted() {
        return encrypted;
    }

    public void setEncrypted(Boolean encrypted) {
        this.encrypted = encrypted;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public Date getTimestampCreated() {
        return timestampCreated;
    }

    public void setTimestampCreated(Date timestampCreated) {
        this.timestampCreated = timestampCreated;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
