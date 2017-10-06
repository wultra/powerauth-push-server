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
 * Class representing campaign sent via push notification.
 *
 * @author Martin Tupy, martin.tupy.work@gmail.com
 */
@Entity
@Table(name = "push_campaign")
public class PushCampaignEntity implements Serializable {

    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "push_campaign", sequenceName = "push_campaign_seq")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "push_campaign")
    private Long id;

    @Column(name = "app_id", nullable = false, updatable = false)
    private Long appId;


    @Column(name = "message", nullable = false, updatable = false)
    private String message;

    @Column(name = "is_sent")
    private boolean sent;

    @Column(name = "timestamp_created", nullable = false, updatable = false)
    private Date timestampCreated;

    @Column(name = "timestamp_sent", updatable = false)
    private Date timestampSent;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public Date getTimestampCreated() {
        return timestampCreated;
    }

    public void setTimestampCreated(Date timestampCreated) {
        this.timestampCreated = timestampCreated;
    }

    public Date getTimestampSent() {
        return timestampSent;
    }

    public void setTimestampSent(Date timestampSent) {
        this.timestampSent = timestampSent;
    }
}
