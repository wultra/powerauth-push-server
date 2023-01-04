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

package io.getlime.push.service.fcm.model;

import com.google.api.client.util.Key;
import com.google.common.collect.ImmutableMap;
import com.google.firebase.messaging.MessagingErrorCode;

import java.util.List;
import java.util.Map;

/**
 * Class containing response body from FCM server in case of error.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class FcmErrorResponse {

    // See class com.google.firebase.messaging.internal.MessagingServiceErrorResponse
    private static final Map<String, MessagingErrorCode> MESSAGING_ERROR_CODES =
            ImmutableMap.<String, MessagingErrorCode>builder()
                    .put("APNS_AUTH_ERROR", MessagingErrorCode.THIRD_PARTY_AUTH_ERROR)
                    .put("INTERNAL", MessagingErrorCode.INTERNAL)
                    .put("INVALID_ARGUMENT", MessagingErrorCode.INVALID_ARGUMENT)
                    .put("QUOTA_EXCEEDED", MessagingErrorCode.QUOTA_EXCEEDED)
                    .put("SENDER_ID_MISMATCH", MessagingErrorCode.SENDER_ID_MISMATCH)
                    .put("THIRD_PARTY_AUTH_ERROR", MessagingErrorCode.THIRD_PARTY_AUTH_ERROR)
                    .put("UNAVAILABLE", MessagingErrorCode.UNAVAILABLE)
                    .put("UNREGISTERED", MessagingErrorCode.UNREGISTERED)
                    .build();

    private static final String FCM_ERROR_TYPE =
            "type.googleapis.com/google.firebase.fcm.v1.FcmError";

    @Key("error")
    private Map<String, Object> error;

    /**
     * Get status.
     *
     * @return Status in case of error, or null.
     */
    public String getStatus() {
        if (error == null) {
            return null;
        }
        return (String) error.get("status");
    }

    /**
     * Get message error code from the response.
     *
     * @return Error code.
     */
    public MessagingErrorCode getMessagingErrorCode() {
        if (error == null) {
            return null;
        }
        Object details = error.get("details");
        if (details instanceof List) {
            for (Object detail : (List<?>) details) {
                if (detail instanceof Map) {
                    Map<?, ?> detailMap = (Map<?, ?>) detail;
                    if (FCM_ERROR_TYPE.equals(detailMap.get("@type"))) {
                        String errorCode = (String) detailMap.get("errorCode");
                        return MESSAGING_ERROR_CODES.get(errorCode);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get error message.
     *
     * @return Value of the error message, or null.
     */
    public String getErrorMessage() {
        if (error != null) {
            return (String) error.get("message");
        }
        return null;
    }
}
