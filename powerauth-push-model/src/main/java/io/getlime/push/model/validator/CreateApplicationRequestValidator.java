/*
 * Copyright 2019 Lime - Wultra s.r.o.
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

import io.getlime.push.model.request.CreateApplicationRequest;

/**
 * Validator class for create application request.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class CreateApplicationRequestValidator {

    /**
     * Validate {@link CreateApplicationRequest} instance.
     *
     * @param request Request to be validated.
     * @return Error message, or null in case of no error.
     */
    public static String validate(CreateApplicationRequest request) {
        if (request == null) {
            return "Request must not be null.";
        }
        if (request.getAppId() == null) {
            return "App ID must not be null.";
        }
        if (request.getAppId() < 1) {
            return "App ID must be a positive number.";
        }
        return null;
    }

}
