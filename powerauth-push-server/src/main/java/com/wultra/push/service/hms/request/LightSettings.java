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
package com.wultra.push.service.hms.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

/**
 * HMS (Huawei Mobile Services) json mapping object.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
@Getter
@SuperBuilder
@Jacksonized
public class LightSettings {

    private final Color color;

    @JsonProperty("light_on_duration")
    private final String lightOnDuration;

    @JsonProperty("light_off_duration")
    private final String lightOffDuration;

}
