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

/**
 * Push server application credentials entity.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class PushServerApplication {

    private String appId;
    private Boolean ios;
    private Boolean android;

    /**
     * Get application ID.
     * @return Application ID.
     */
    public String getAppId() {
        return appId;
    }

    /**
     * Set application ID.
     * @param appId Application ID.
     */
    public void setAppId(String appId) {
        this.appId = appId;
    }

    /**
     * Get whether iOS is configured.
     * @return Whether iOS is configured.
     */
    public Boolean getIos() {
        return ios;
    }

    /**
     * Set whether iOS is configured.
     * @param ios Whether iOS is configured.
     */
    public void setIos(Boolean ios) {
        this.ios = ios;
    }

    /**
     * Get whether Android is configured.
     * @return Whether Android is configured.
     */
    public Boolean getAndroid() {
        return android;
    }

    /**
     * Set whether Android is configured.
     * @param android Whether Android is configured.
     */
    public void setAndroid(Boolean android) {
        this.android = android;
    }
}
