/*
 * Copyright 2018 Wultra s.r.o.
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
package com.wultra.push.model.validator;

import com.wultra.push.model.entity.PushMessage;

/**
 * Validator class for push message instances.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class PushMessageValidator {

    /**
     * Validate {@link PushMessage} instance.
     *
     * @param pushMessage Push message instance to be validated.
     * @return Error message, or null in case of no error.
     */
    public static String validatePushMessage(PushMessage pushMessage) {
        if (pushMessage == null) {
            return "Push message must not be null.";
        }
        if (pushMessage.getBody() == null) {
            return "Push message payload (body) must not be null.";
        }
        if (pushMessage.getUserId() == null) {
            return "Push message must contain a user ID.";
        }
        return null;
    }

}
