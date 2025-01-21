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
package com.wultra.push.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Get application credentials entity detail request.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Data
public class GetApplicationDetailRequest {

    /**
     * Application ID.
     */
    @NotBlank
    @Schema(description = "Application ID.")
    private String appId;

    /**
     * Whether to include APNs details.
     */
    @Schema(description = "Whether to include APNs details.")
    private boolean includeApns;

    /**
     * Whether to include FCM details.
     */
    @Schema(description = "Whether to include FCM details.")
    private boolean includeFcm;

    /**
     * Whether to include Huawei details.
     */
    @Schema(description = "Whether to include HMS details.")
    private boolean includeHms;

    /**
     * No-arg constructor.
     */
    public GetApplicationDetailRequest() {
    }

    /**
     * Constructor with application credentials entity ID.
     * @param appId Application credentials entity ID.
     */
    public GetApplicationDetailRequest(String appId) {
        this.appId = appId;
    }

    /**
     * Constructor with details.
     * @param appId Application credentials entity ID.
     * @param includeApns Whether to include APNs details.
     * @param includeFcm Whether to include FCM details.
     * @param includeHms Whether to include HSM details.
     */
    public GetApplicationDetailRequest(String appId, boolean includeApns, boolean includeFcm, boolean includeHms) {
        this.appId = appId;
        this.includeApns = includeApns;
        this.includeFcm = includeFcm;
        this.includeHms = includeHms;
    }

}
