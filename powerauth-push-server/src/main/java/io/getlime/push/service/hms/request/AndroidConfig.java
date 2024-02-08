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
package io.getlime.push.service.hms.request;

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
public class AndroidConfig {

    @JsonProperty("collapse_key")
    private final Integer collapseKey;

    private final String urgency;

    private final String category;

    private final String ttl;

    @JsonProperty("bi_tag")
    private final String biTag;

    @JsonProperty("fast_app_target")
    private final Integer fastAppTargetType;

    private final String data;

    private final AndroidNotification notification;

    private final String receiptId;
}
