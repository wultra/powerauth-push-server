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

package io.getlime.push.model.response;

import io.getlime.push.model.entity.PushMessageBody;

/**
 * Response object used for getting a campaign
 *
 * @author Martin Tupy, martin.tupy.work@gmail.com
 */
public class CampaignResponse {

    private Long id;
    private String appId;
    private boolean sent;
    private PushMessageBody message;

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
    public String getAppId() {
        return appId;
    }

    /**
     * Set app ID.
     * @param appId App ID.
     */
    public void setAppId(String appId) {
        this.appId = appId;
    }

    /**
     * Check if the message is sent.
     * @return True if sent, false otherwise.
     */
    public boolean isSent() {
        return sent;
    }

    /**
     * Set if the message was sent.
     * @param sent True if sent, false otherwise.
     */
    public void setSent(boolean sent) {
        this.sent = sent;
    }

    /**
     * Get push message body.
     * @return Push message body.
     */
    public PushMessageBody getMessage() {
        return message;
    }

    /**
     * Set push message body.
     * @param message Push message body.
     */
    public void setMessage(PushMessageBody message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CampaignResponse that = (CampaignResponse) o;

        if (sent != that.sent) return false;
        if (!id.equals(that.id)) return false;
        if (!appId.equals(that.appId)) return false;
        return message.equals(that.message);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
