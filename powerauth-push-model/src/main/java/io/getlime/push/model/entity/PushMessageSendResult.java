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

package io.getlime.push.model.entity;

import io.getlime.push.model.enumeration.Mode;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Class that contains push message sending result data.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PushMessageSendResult extends BasePushMessageSendResult {

    /**
     * Data associated with push messages sent to APNs.
     */
    private final PlatformResult apns;

    /**
     * Data associated with push messages sent to FCM.
     */
    private final PlatformResult fcm;

    /**
     * Data associated with push messages sent to HMS.
     */
    private final PlatformResult hms;

    /**
     * No-arg constructor.
     */
    public PushMessageSendResult() {
        this.apns = new PlatformResult();
        this.fcm = new PlatformResult();
        this.hms =  new PlatformResult();
    }

    /**
     * Primary constructor.
     *
     * @param mode Mode of push message sending.
     */
    public PushMessageSendResult(Mode mode) {
        super(mode);
        this.apns = new PlatformResult();
        this.fcm = new PlatformResult();
        this.hms =  new PlatformResult();
    }

    /**
     * Result for the platform.
     */
    @Data
    public static class PlatformResult {

        /**
         * Number of messages that were sent successfully.
         */
        private int sent;

        /**
         * Number of messages that were sent with failure
         */
        private int failed;

        /**
         * Number of messages that are still in pending state after attempted sending
         */
        private int pending;

        /**
         * Total number of messages that were attempted to send
         */
        private int total;

    }
}
