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


import com.wultra.security.powerauth.client.model.enumeration.ActivationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Class representing request object responsible for updating activation status.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Getter
@Setter
public class UpdateDeviceStatusRequest {

    /**
     * Activation ID.
     */
    @NotBlank
    @Schema(description = "Activation ID.", format = "UUID (level 4)", maxLength = 37, example = "099e5e30-47b1-41c7-b49b-3bf28e811fca")
    private String activationId;

    /**
     * Activation status.
     */
    @Schema(description = "Activation status.")
    private ActivationStatus activationStatus;

}
