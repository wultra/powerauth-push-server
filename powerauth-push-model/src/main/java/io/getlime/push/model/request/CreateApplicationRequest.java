/*
 * Copyright 2018 Lime - HighTech Solutions s.r.o.
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

/**
 * Create application request.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class CreateApplicationRequest {

    private Long appId;

    /**
     * Default constructor.
     */
    public CreateApplicationRequest() {
    }

    /**
     * Constructor with application ID.
     * @param appId Application ID.
     */
    public CreateApplicationRequest(Long appId) {
        this.appId = appId;
    }

    /**
     * Get application ID.
     * @return Application ID.
     */
    public Long getAppId() {
        return appId;
    }

    /**
     * Set application ID.
     * @param appId Application ID.
     */
    public void setAppId(Long appId) {
        this.appId = appId;
    }
}
