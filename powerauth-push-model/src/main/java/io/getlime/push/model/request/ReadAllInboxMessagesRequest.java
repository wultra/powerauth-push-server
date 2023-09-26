/*
 * Copyright 2023 Wultra s.r.o.
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
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request object to read all messages in users inbox.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Data
public class ReadAllInboxMessagesRequest {

    @NotNull
    @NotEmpty
    @Size(min = 1, max = 255)
    @Schema(type = "string", example = "end-user-123")
    private String userId;

    @NotNull
    @NotEmpty
    @Size(min = 1, max = 255)
    @Schema(type = "string", example = "my-app-01")
    private String appId;

}
