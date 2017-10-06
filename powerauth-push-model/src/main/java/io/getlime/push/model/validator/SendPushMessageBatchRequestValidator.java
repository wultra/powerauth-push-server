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

package io.getlime.push.model.validator;

import io.getlime.push.model.entity.PushMessage;
import io.getlime.push.model.request.SendPushMessageBatchRequest;

/**
 * Validator class used in sending batch PushMessages.
 *
 * @author Martin Tupy, martin.tupy.work@gmail.com
 */
public class SendPushMessageBatchRequestValidator {
    public static String validate(SendPushMessageBatchRequest pushMessageBatch) {
        if (pushMessageBatch == null) {
            return "Empty request";
        } else if (pushMessageBatch.getAppId() == null) {
            return "Empty appId";
        } else if (pushMessageBatch.getBatch().size() > 20) {
            return "Too many messages in batch - do no send more than 20 messages at once to avoid server congestion.";
        } else {
            for (PushMessage pushMessage : pushMessageBatch.getBatch()) {
                if (pushMessage == null) {
                    return "Empty message";
                } else if (pushMessage.getBody() == null) {
                    return "Empty body";
                }
            }
        }
        return null;
    }
}
