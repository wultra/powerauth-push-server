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
import lombok.Getter;
import lombok.Setter;

/**
 * Update Android configuration request.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Getter
@Setter
public class UpdateAndroidRequest {

    /**
     * Application ID.
     */
    @NotBlank
    @Schema(description = "Application ID.")
    private String appId;

    /**
     * Android project ID.
     */
    @NotBlank
    @Schema(description = "Android project ID.")
    private String projectId;

    /**
     * Base64 encoded Android private key.
     */
    @NotBlank
    @Schema(description = "Base64 encoded Android private key.")
    private String privateKeyBase64;

    /**
     * No-arg constructor.
     */
    public UpdateAndroidRequest() {
    }

    /**
     * Constructor with details.
     * @param appId Application credentials entity ID.
     * @param projectId Android project ID record.
     * @param privateKeyBase64 Base 64 encoded Android private key.
     */
    public UpdateAndroidRequest(String appId, String projectId, String privateKeyBase64) {
        this.appId = appId;
        this.projectId = projectId;
        this.privateKeyBase64 = privateKeyBase64;
    }

}
