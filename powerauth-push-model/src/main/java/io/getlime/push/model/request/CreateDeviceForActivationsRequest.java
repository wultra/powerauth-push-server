/*
 * Copyright 2019 Wultra s.r.o.
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

import io.getlime.push.model.enumeration.MobilePlatform;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Request object used for device registration in case multiple associated activations are used.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Getter
@Setter
public class CreateDeviceForActivationsRequest {

    /**
     * Application ID.
     */
    @NotBlank
    @Schema(description = "Application ID.")
    private String appId;

    /**
     * The push token is the value received from APNS, or FCM services without any modification.
     */
    @NotBlank
    @Schema(description = "The push token is the value received from APNS, or FCM services without any modification.")
    private String token;

    /**
     * The platform.
     */
    @NotNull
    private MobilePlatform platform;

    /**
     * Activation IDs.
     */
    @NotEmpty
    @Schema(description = "Activation IDs.")
    private final List<String> activationIds = new ArrayList<>();

}
