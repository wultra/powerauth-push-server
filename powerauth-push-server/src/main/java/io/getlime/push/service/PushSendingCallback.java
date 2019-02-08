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

package io.getlime.push.service;

/**
 * Interface used for handling result of the push message sending attempt.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public interface PushSendingCallback {

    /**
     * Enum representing push message sending result.
     */
    enum Result {

        /**
         * Message was successfully sent. Optionally if contextData has key updateToken, token will be updated in database of device registrations.
         */
        OK,

        /**
         * Message was not sent (or even not real attempt was made) and remains in the pending state. Sending could be retried later.
         */
        PENDING,

        /**
         * Message sending failed, but token was not made invalid and should be kept.
         */
        FAILED,

        /**
         * Message sending failed, token is no longer valid and should be deleted.
         */
        FAILED_DELETE
    }

    /**
     * Called after the push message sending attempt is made.
     *
     * @param result Result of the push message sending.
     */
    void didFinishSendingMessage(Result result);
}
