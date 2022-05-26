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

package io.getlime.push.model.request;

import io.getlime.push.model.entity.PushMessageBody;

/**
 * Request object used for creating a campaign.
 *
 * @author Martin Tupy, martin.tupy.work@gmail.com
 */


public class CreateCampaignRequest {

    private String appId;

    private PushMessageBody message;

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
}
