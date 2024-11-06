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

import io.getlime.push.model.enumeration.ApnsEnvironment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Update APNs configuration request.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateApnsRequest {

    /**
     * Application ID.
     */
    @NotBlank
    @Schema(description = "Application ID.")
    private String appId;

    /**
     * iOS bundle.
     */
    @NotBlank
    @Schema(description = "iOS bundle.")
    private String bundle;

    /**
     * APNs key ID.
     */
    @NotBlank
    @Schema(description = "APNs key ID.")
    private String keyId;

    /**
     * Team ID.
     */
    @NotBlank
    @Schema(description = "Team ID.")
    private String teamId;

    /**
     * APNs environment.
     */
    @Schema(description = "APNs environment.")
    private ApnsEnvironment environment;

    /**
     * Base64 encoded private key.
     */
    @NotBlank
    @Schema(description = "Base64 encoded private key.")
    private String privateKeyBase64;

}
