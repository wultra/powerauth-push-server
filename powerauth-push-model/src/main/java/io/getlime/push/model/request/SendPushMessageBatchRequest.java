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
import io.getlime.push.model.enumeration.Mode;

import java.util.List;

/**
 * Class representing a request for batch of push messages.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class SendPushMessageBatchRequest {

    private String appId;
    private Mode mode = Mode.SYNCHRONOUS;
    private List<PushMessage> batch;

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
     * Get mode of sending.
     * @return Mode.
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Set mode of sending.
     * @param mode Mode.
     */
    public void setMode(Mode mode) {
        this.mode = mode;
    }

    /**
     * Get batch list with push notifications to be sent.
     * @param batch Push notification batch.
     */
    public void setBatch(List<PushMessage> batch) {
        this.batch = batch;
    }

    /**
     * Set batch list with push notifications to be sent.
     * @return Push notification batch.
     */
    public List<PushMessage> getBatch() {
        return batch;
    }
}
