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
 * Class representing campaign devices to sent a certain message.
 *
 * @author Martin Tupy, martin.tupy.work@gmail.com
 */

@Entity(name = "push_campaign_device")
public class PushCampaignDeviceEntity {

    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "push_campaign", sequenceName = "push_campaign_seq")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "push_campaign")
    private Long id;

    @Column(name = "campaign_id", nullable = false)
    private Long campaignId;

    @Column(name = "platform", nullable = false)
    private String platform;

    @Column(name = "push_token", nullable = false)
    private String pushToken;

    @Column(name = "status")
    private int status;

    @Column(name = "timestamp_created", nullable = false)
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

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getPushToken() {
        return pushToken;
    }

    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getTimestampCreated() {
        return timestampCreated;
    }

    public void setTimestampCreated(Date timestampCreated) {
        this.timestampCreated = timestampCreated;
    }
}
