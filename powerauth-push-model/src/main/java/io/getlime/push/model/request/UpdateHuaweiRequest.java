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
package io.getlime.push.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Update Huawei configuration request.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
@Getter
@Setter
public class UpdateHuaweiRequest {

    /**
     * Application ID.
     */
    @NotBlank
    @Schema(description = "Application ID.")
    private String appId;

    /**
     * Huawei project ID.
     */
    @NotBlank
    @Schema(description = "Huawei project ID.")
    private String projectId;

    /**
     * Huawei OAuth 2.0 client ID.
     */
    @NotBlank
    @Schema(description = "Huawei OAuth 2.0 client ID.")
    private String clientId;

    /**
     * Huawei OAuth 2.0 client secret.
     */
    @NotBlank
    @Schema(description = "Huawei OAuth 2.0 client secret.")
    private String clientSecret;

}
