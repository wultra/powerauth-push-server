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
package io.getlime.push.model.request;

import io.getlime.push.model.entity.PushMessage;
import io.getlime.push.model.enumeration.Mode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Class representing a request for batch of push messages.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Getter
@Setter
public class SendPushMessageBatchRequest {

    /**
     * Application ID.
     */
    @NotBlank
    @Schema(description = "Application ID.")
    private String appId;

    /**
     * Mode of sending.
     */
    @Schema(description = "Mode of sending.")
    private Mode mode = Mode.SYNCHRONOUS;

    /**
     * Batch list with push notifications to be sent.
     */
    @Schema(description = "Batch list with push notifications to be sent.")
    @NotEmpty
    private List<@NotNull PushMessage> batch;

}
