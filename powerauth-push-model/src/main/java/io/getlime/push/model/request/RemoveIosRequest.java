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
package io.getlime.push.model.request;

/**
 * Remove iOS configuration request.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class RemoveIosRequest {

    private String appId;

    /**
     * Default constructor.
     */
    public RemoveIosRequest() {
    }

    /**
     * Constructor with application credentials entity ID.
     * @param appId Application credentials entity ID.
     */
    public RemoveIosRequest(String appId) {
        this.appId = appId;
    }

    /**
     * Get application credentials entity ID.
     * @return Application credentials entity ID.
     */
    public String getAppId() {
        return appId;
    }

    /**
     * Set application credentials entity ID.
     * @param appId Application credentials entity ID.
     */
    public void setAppId(String appId) {
        this.appId = appId;
    }

}
