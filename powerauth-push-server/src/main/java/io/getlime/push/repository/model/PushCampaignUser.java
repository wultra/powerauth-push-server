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
import java.util.Date;

/**
 * Class representing campaign users to get a certain message.
 *
 * @author Martin Tupy, martin.tupy.work@gmail.com
 */

@Entity(name = "push_campaign_users")
public class PushCampaignUser {

    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "push_campaign_users", sequenceName = "push_campaign_users_seq")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "push_campaign_users")
    private Long id;

    @Column(name = "campaign_id", nullable = false)
    private Long campaignId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "timestamp_added", nullable = false)
    private Date timestampAdded;

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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getTimestampAdded() {
        return timestampAdded;
    }

    public void setTimestampAdded(Date timestampAdded) {
        this.timestampAdded = timestampAdded;
    }
}
