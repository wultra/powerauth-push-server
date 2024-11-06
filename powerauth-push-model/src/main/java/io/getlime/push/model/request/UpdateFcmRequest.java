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
import lombok.*;

/**
 * Update FCM configuration request.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFcmRequest {

    /**
     * Application ID.
     */
    @NotBlank
    @Schema(description = "Application ID.")
    private String appId;

    /**
     * FCM project ID.
     */
    @NotBlank
    @Schema(description = "FCM project ID.")
    private String projectId;

    /**
     * Base64 encoded FCM private key.
     */
    @NotBlank
    @Schema(description = "Base64 encoded FCM private key.")
    private String privateKeyBase64;

}
