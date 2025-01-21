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

package com.wultra.push.service.fcm.model;

/**
 * Class containing response body from FCM server in case of success.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class FcmSuccessResponse {

    private String name;

    /**
     * Get the "name" string containing response details.
     * @return Message details.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the "name" string containing response details.
     * @param name Message details.
     */
    public void setName(String name) {
        this.name = name;
    }
}
