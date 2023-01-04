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
import java.util.Date;

/**
 * Class representing campaign sent via push notification.
 *
 * @author Martin Tupy, martin.tupy.work@gmail.com
 */
@Entity
@Table(name = "push_campaign")
public class PushCampaignEntity implements Serializable {

    /**
     * Campaign ID.
     */
    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "push_campaign", sequenceName = "push_campaign_seq")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "push_campaign")
    private Long id;

    /**
     * App ID.
     */
    @ManyToOne
    @JoinColumn(name = "app_id", referencedColumnName = "id", nullable = false, updatable = false)
    private AppCredentialsEntity appCredentials;

    /**
     * Message.
     */
    @Column(name = "message", nullable = false, updatable = false)
    private String message;

    /**
     * Flag indicating if campaign was sent.
     */
    @Column(name = "is_sent")
    private boolean sent;

    /**
     * Timestamp the campaign was created.
     */
    @Column(name = "timestamp_created", nullable = false, updatable = false)
    private Date timestampCreated;

    /**
     * Timestamp the campaign was sent.
     */
    @Column(name = "timestamp_sent")
    private Date timestampSent;

    /**
     * Timestamp the campaign was completed.
     */
    @Column(name = "timestamp_completed")
    private Date timestampCompleted;

    /**
     * Get campaign ID.
     * @return Campaign ID.
     */
    public Long getId() {
        return id;
    }

    /**
     * Set campaign ID.
     * @param id Campaign ID.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get app ID.
     * @return App ID.
     */
    public AppCredentialsEntity getAppCredentials() {
        return appCredentials;
    }

    /**
     * Set app credentials.
     * @param appCredentials App credentials.
     */
    public void setAppCredentials(AppCredentialsEntity appCredentials) {
        this.appCredentials = appCredentials;
    }

    /**
     * Get message.
     * @return Message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set message.
     * @param message Message.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Check if the campaign is sent.
     * @return True if the campaign is set, false otherwise.
     */
    public boolean isSent() {
        return sent;
    }

    /**
     * Set if the campaign is set.
     * @param sent True if the campaign is set, false otherwise.
     */
    public void setSent(boolean sent) {
        this.sent = sent;
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
     * Get timestamp sent.
     * @return Timestamp sent.
     */
    public Date getTimestampSent() {
        return timestampSent;
    }

    /**
     * Set timestamp sent.
     * @param timestampSent Timestamp sent.
     */
    public void setTimestampSent(Date timestampSent) {
        this.timestampSent = timestampSent;
    }

    /**
     * Get timestamp completed.
     * @return Timestamp completed.
     */
    public Date getTimestampCompleted() {
        return timestampCompleted;
    }

    /**
     * Set timestamp completed.
     * @param timestampCompleted Timestamp completed.
     */
    public void setTimestampCompleted(Date timestampCompleted) {
        this.timestampCompleted = timestampCompleted;
    }
}
