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

import io.getlime.push.model.request.CreateCampaignRequest;

/**
 * Validator class used in creating a campaign
 *
 * @author Martin Tupy, martin.tupy.work@gmail.com
 */
public class CreateCampaignRequestValidator {
    public static String validate(CreateCampaignRequest request) {
        if (request == null) {
            return "Empty request";
        } else if (request.getAppId() == null || request.getAppId() < 1) {
            return "Empty or invalid appId";
        } else if (request.getMessage() == null) {
            return "Empty message";
        } else if (request.getMessage().getBody() == null) {
            return "Empty body";
        }
        return null;
    }
}
