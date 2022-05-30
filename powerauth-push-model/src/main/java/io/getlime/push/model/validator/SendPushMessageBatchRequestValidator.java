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

package io.getlime.push.model.validator;

import io.getlime.push.model.entity.PushMessage;
import io.getlime.push.model.request.SendPushMessageBatchRequest;

/**
 * Validator class for batch push message requests.
 *
 * @author Petr Dvorak, petr@wultra.com
 * @author Martin Tupy, martin.tupy.work@gmail.com
 */
public class SendPushMessageBatchRequestValidator {

    /**
     * Validate {@link SendPushMessageBatchRequest} instance.
     *
     * @param pushMessageBatch Request to be validated.
     * @return Error message, or null in case of no error.
     */
    public static String validate(SendPushMessageBatchRequest pushMessageBatch) {
        if (pushMessageBatch == null) {
            return "Request must not be null.";
        }
        if (pushMessageBatch.getAppId() == null) {
            return "App ID must not be null.";
        }
        if (pushMessageBatch.getBatch() == null) {
            return "Batch with push messages must not be null.";
        }
        if (pushMessageBatch.getBatch().size() == 0) {
            return "There are no push messages in the batch.";
        }
        if (pushMessageBatch.getBatch().size() > 20) {
            return "Too many messages in batch - do no send more than 20 messages at once to avoid server congestion.";
        }
        int i = 0; // validate individual messages
        for (PushMessage message: pushMessageBatch.getBatch()) {
            String error = PushMessageValidator.validatePushMessage(message);
            if (error != null) {
                return "Push message at index: " + i + " is not valid. Nested error is: " + error;
            }
            i++;
        }
        
        return null;
    }

}
