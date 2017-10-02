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

package io.getlime.push.service.fcm.model.base;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Results of sent messages
 *
 * @author Martin Tupy, martin.tupy.work@gmail.com
 */
public class FcmResult {

    public static final String KEY_UPDATE_TOKEN = "updateToken";

    @JsonProperty(value = "message_id")
    private String messageId;

    @JsonProperty(value = "registration_id")
    private String registrationId;

    @JsonProperty(value = "error")
    private String fcmError;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    public String getFcmError() {
        return fcmError;
    }

    public void setFcmError(String fcmError) {
        this.fcmError = fcmError;
    }
}
