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
 * Class representing campaign users to get a certain message.
 *
 * @author Martin Tupy, martin.tupy.work@gmail.com
 */

@Entity
@Table(name = "push_campaign_user")
public class PushCampaignUserEntity implements Serializable {

    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "push_campaign_user", sequenceName = "push_campaign_user_seq")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "push_campaign_user")
    private Long id;

    @Column(name = "campaign_id", nullable = false, updatable = false)
    private Long campaignId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private String userId;

    @Column(name = "app_id", nullable = false, updatable = false)
    private Long appId;

    @Column(name = "timestamp_created", nullable = false, updatable = false)
    private Date timestampCreated;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getTimestampCreated() {
        return timestampCreated;
    }

    public void setTimestampCreated(Date timestampCreated) {
        this.timestampCreated = timestampCreated;
    }
}
