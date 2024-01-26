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
 * Update iOS configuration request.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Getter
@Setter
public class UpdateIosRequest {

    /**
     *
     */
    @NotBlank
    @Schema(description = "")
    private String appId;

    /**
     * iOS bundle.
     */
    @NotBlank
    @Schema(description = "iOS bundle.")
    private String bundle;

    /**
     * iOS key ID.
     */
    @NotBlank
    @Schema(description = "iOS key ID.")
    private String keyId;

    /**
     * iOS team ID.
     */
    @NotBlank
    @Schema(description = "iOS team ID.")
    private String teamId;

    /**
     * APNs environment.
     */
    @Schema(description = "APNs environment.")
    private String environment;

    /**
     * Base64 encoded private key.
     */
    @NotBlank
    @Schema(description = "Base64 encoded private key.")
    private String privateKeyBase64;

    /**
     * Default constructor.
     */
    public UpdateIosRequest() {
    }

    /**
     * Constructor with details.
     * @param appId Application credentials entity ID.
     * @param bundle The iOS bundle record.
     * @param keyId The iOS key ID record.
     * @param teamId The iOS team ID record.
     * @param environment The APNs environment (per-app config).
     * @param privateKeyBase64 Base64 encoded private key.
     */
    public UpdateIosRequest(String appId, String bundle, String keyId, String teamId, String environment, String privateKeyBase64) {
        this.appId = appId;
        this.bundle = bundle;
        this.keyId = keyId;
        this.teamId = teamId;
        this.environment = environment;
        this.privateKeyBase64 = privateKeyBase64;
    }

}
