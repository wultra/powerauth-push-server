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

package com.wultra.push.model.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Response object for a system status call.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Getter
@Setter
public class ServiceStatusResponse {

    /**
     * Application name.
     */
    private String applicationName;

    /**
     * Application display name.
     */
    private String applicationDisplayName;

    /**
     * Application environment name.
     */
    private String applicationEnvironment;

    /**
     * Version.
     */
    private String version;

    /**
     * Build time.
     */
    private Date buildTime;

    /**
     * Current timestamp.
     */
    private Date timestamp;

}
