/*
 * Copyright 2016 Wultra s.r.o.
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

import com.wultra.push.model.enumeration.ApnsEnvironment;
import com.wultra.push.model.enumeration.MobilePlatform;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Request object used for device registration.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateDeviceRequest {

    /**
     * Application ID.
     */
    @NotBlank
    @Schema(description = "Application ID.")
    private String appId;

    /**
     * The push token is the value received from APNS, FCM, or HMS services without any modification.
     */
    @NotBlank
    @Schema(description = "The push token is the value received from APNS, FCM, or HMS services without any modification.")
    private String token;

    @NotNull
    private MobilePlatform platform;

    /**
     * Environment for APNs (optional).
     */
    private ApnsEnvironment environment;

    /**
     * Activation ID.
     */
    @NotBlank
    @Schema(description = "Activation ID.", format = "UUID (level 4)", maxLength = 37, example = "099e5e30-47b1-41c7-b49b-3bf28e811fca")
    private String activationId;

}
