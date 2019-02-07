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

import java.util.List;
import java.util.Map;

/**
 * Class containing response body from FCM server in case of error.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class FcmErrorResponse {

    // FCM error type
    private static final String FCM_ERROR_TYPE =
            "type.googleapis.com/google.firebase.fcm.v1.FcmErrorCode";

    // FCM error codes, see class com.google.firebase.messaging.FirebaseMessaging for original definition of error codes
    public static final String INTERNAL_ERROR = "internal-error";
    public static final String UNKNOWN_ERROR = "unknown-error";
    public static final String REGISTRATION_TOKEN_NOT_REGISTERED = "registration-token-not-registered";
    public static final String INVALID_APNS_CREDENTIALS = "invalid-apns-credentials";
    public static final String INVALID_ARGUMENT = "invalid-argument";
    public static final String MESSAGE_RATE_EXCEEDED = "message-rate-exceeded";
    public static final String MISMATCHED_CREDENTIAL = "mismatched-credential";
    public static final String SERVER_UNAVAILABLE = "server-unavailable";

    @Key("error")
    private Map<String, Object> error;

    /**
     * Get FCM error code.
     * @return FCM error code.
     */
    public String getErrorCode() {
        if (error == null) {
            return null;
        }
        Object details = error.get("details");
        if (details instanceof List) {
            for (Object detail : (List) details) {
                if (detail instanceof Map) {
                    Map detailMap = (Map) detail;
                    if (FCM_ERROR_TYPE.equals(detailMap.get("@type"))) {
                        return (String) detailMap.get("errorCode");
                    }
                }
            }
        }
        return (String) error.get("status");
    }

    /**
     * Get FCM error message.
     *
     * @return FCM error message.
     */
    public String getErrorMessage() {
        if (error != null) {
            return (String) error.get("message");
        }
        return null;
    }
}
