/*
 * Copyright 2024 Wultra s.r.o.
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

import io.getlime.push.model.request.UpdateApnsRequest;

/**
 * Validator class for update APNs request.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class UpdateApnsRequestValidator {

    /**
     * Validate {@link UpdateApnsRequest} instance.
     *
     * @param request Request to be validated.
     * @return Error message, or null in case of no error.
     */
    public static String validate(UpdateApnsRequest request) {
        if (request == null) {
            return "Request must not be null.";
        }
        if (request.getAppId() == null) {
            return "App ID must not be null.";
        }
        if (request.getBundle() == null || request.getBundle().isEmpty()) {
            return "Bundle must not be empty.";
        }
        if (request.getKeyId() == null || request.getKeyId().isEmpty()) {
            return "Key ID must not be empty.";
        }
        if (request.getTeamId() == null || request.getTeamId().isEmpty()) {
            return "Team ID must not be empty.";
        }
        if (request.getPrivateKeyBase64() == null || request.getPrivateKeyBase64().isEmpty()) {
            return "Private key must not be empty.";
        }
        return null;
    }

}
