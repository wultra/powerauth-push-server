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

import com.wultra.push.model.request.DeleteDeviceRequest;

/**
 * Validator class for delete device requests.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class DeleteDeviceRequestValidator {

    /**
     * Validate {@link DeleteDeviceRequest} instance.
     * @param request Request to be validated.
     * @return Error message, or null in case of no error.
     */
    public static String validate(DeleteDeviceRequest request) {
        if (request == null) {
            return "Request must not be empty.";
        }
        if (request.getAppId() == null) {
            return "App ID must not be null.";
        }
        if (request.getToken() == null || request.getToken().isEmpty()) {
            return "Push token must not be null or empty.";
        }
        return null;
    }

}
