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

package io.getlime.push.model.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * Push server application credentials entity.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Getter
@Setter
public class PushServerApplication {

    /**
     * Application ID.
     */
    private String appId;

    /**
     * Whether iOS is configured.
     */
    private Boolean ios;

    /**
     * Whether Android is configured.
     */
    private Boolean android;

    /**
     * Whether Huawei is configured.
     */
    private Boolean huawei;

}
