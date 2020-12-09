/*
 * Copyright 2020 Wultra s.r.o.
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
package io.getlime.push.service.fcm.model;

import com.google.firebase.messaging.Message;

/**
 * Model class for FCM requests with message and validation mode.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class MessageWithValidation {

    private Message message;
    private boolean validate_only;

    /**
     * Default constructor.
     */
    public MessageWithValidation() {
    }

    /**
     * Constructor with message and validation mode.
     * @param message Message.
     * @param validateOnly Validation mode.
     */
    public MessageWithValidation(Message message, boolean validateOnly) {
        this.message = message;
        this.validate_only = validateOnly;
    }

    /**
     * Get the message.
     * @return Message.
     */
    public Message getMessage() {
        return message;
    }

    /**
     * Set the message.
     * @param message Message.
     */
    public void setMessage(Message message) {
        this.message = message;
    }

    /**
     * Get validation mode.
     * @return Validation mode.
     */
    public boolean isValidateOnly() {
        return validate_only;
    }

    /**
     * Set validation mode.
     * @param validateOnly Validation mode.
     */
    public void setValidateOnly(boolean validateOnly) {
        this.validate_only = validateOnly;
    }
}
