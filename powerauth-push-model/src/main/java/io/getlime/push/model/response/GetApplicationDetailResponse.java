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
package io.getlime.push.model.response;

import io.getlime.push.model.entity.PushServerApplication;
import io.getlime.push.model.enumeration.ApnsEnvironment;
import lombok.Data;

/**
 * Get application credentials entity detail response.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Data
public class GetApplicationDetailResponse {

    /**
     * Push server application.
     */
    private PushServerApplication application;

    /**
     * iOS bundle.
     */
    private String iosBundle;

    /**
     * iOS key ID.
     */
    private String iosKeyId;

    /**
     * iOS team ID.
     */
    private String iosTeamId;

    /**
     * APNs environment.
     */
    private ApnsEnvironment iosEnvironment;

    /**
     * Android project ID record.
     */
    private String androidProjectId;

    /**
     * Huawei project ID.
     */
    private String huaweiProjectId;

}
