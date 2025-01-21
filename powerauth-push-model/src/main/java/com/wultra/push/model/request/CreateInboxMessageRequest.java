/*
 * Copyright 2022 Wultra s.r.o.
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

import com.wultra.push.model.enumeration.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * Request object for the message.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Data
public class CreateInboxMessageRequest {

    @NotEmpty
    @Size(min = 1, max = 255)
    @Schema(type = "string", example = "end-user-123")
    private String userId;

    @NotNull
    @Schema(type = "string", example = "text")
    private MessageType type;

    @NotNull
    @Size(min = 8, max = 255)
    @Schema(type = "string", example = "Example subject")
    private String subject;

    @NotNull
    @Size(min = 8, max = 255)
    @Schema(type = "string", example = "Example summary")
    private String summary;

    @NotNull
    @Size(min = 8, max = 65536)
    @Schema(type = "string", example = "Example message body")
    @ToString.Exclude
    private String body;

    @NotNull
    @NotEmpty
    private List<@NotNull @Pattern(regexp="^[a-zA-Z0-9_-]{3,255}$") String> applications = new ArrayList<>();

}
