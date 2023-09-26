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
import java.io.Serializable;
import java.util.Date;

/**
 * Class representing campaign users to get a certain message.
 *
 * @author Martin Tupy, martin.tupy.work@gmail.com
 */

@Entity
@Table(name = "push_campaign_user")
public class PushCampaignUserEntity implements Serializable {

    /**
     * Campaign user ID.
     */
    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "push_campaign_user", sequenceName = "push_campaign_user_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "push_campaign_user")
    private Long id;

    /**
     * Campaign ID.
     */
    @Column(name = "campaign_id", nullable = false, updatable = false)
    private Long campaignId;

    /**
     * User ID.
     */
    @Column(name = "user_id", nullable = false, updatable = false)
    private String userId;

    /**
     * Timestamp created.
     */
    @Column(name = "timestamp_created", nullable = false, updatable = false)
    private Date timestampCreated;

    /**
     * Get campaign user ID.
     * @return Campaign user ID.
     */
    public Long getId() {
        return id;
    }

    /**
     * Set campaign user ID.
     * @param id Campaign user ID.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get campaign ID.
     * @return Campaign ID.
     */
    public Long getCampaignId() {
        return campaignId;
    }

    /**
     * Set campaign ID.
     * @param campaignId Campaign ID.
     */
    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
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
}
