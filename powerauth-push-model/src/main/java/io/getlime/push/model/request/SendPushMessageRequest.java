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

import io.getlime.push.model.entity.PushMessage;

/**
 * Class representing a single push message send request.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class SendPushMessageRequest {

    private Long appId;
    private PushMessage message;

    /**
     * Get app ID.
     * @return App ID.
     */
    public Long getAppId() {
        return appId;
    }

    /**
     * Set app ID.
     * @param appId App ID.
     */
    public void setAppId(Long appId) {
        this.appId = appId;
    }

    /**
     * Get push message to be sent.
     * @return Push message.
     */
    public PushMessage getMessage() {
        return message;
    }

    /**
     * Set push message to be sent.
     * @param message Push message.
     */
    public void setMessage(PushMessage message) {
        this.message = message;
    }
}
