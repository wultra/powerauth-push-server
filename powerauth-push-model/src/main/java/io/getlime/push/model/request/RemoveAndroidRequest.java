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
package io.getlime.push.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Remove android configuration request.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Getter
@Setter
public class RemoveAndroidRequest {

    /**
     * Application ID.
     */
    @NotBlank
    @Schema(description = "Application ID.")
    private String appId;

    /**
     * No-arg constructor.
     */
    public RemoveAndroidRequest() {
    }

    /**
     * Constructor with application credentials entity ID.
     * @param appId Application credentials entity ID.
     */
    public RemoveAndroidRequest(String appId) {
        this.appId = appId;
    }

}
