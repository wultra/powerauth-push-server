/*
 * Copyright 2016 Lime - HighTech Solutions s.r.o.
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
 * Class representing request object responsible for device registration removal.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class RemoveDeviceRegistrationRequest {

    private Long appId;
    private String token;

    /**
     * Get app ID.
     * @return App ID.
     */
    public Long getAppId() {
        return appId;
    }

    /**
     * Set app ID.
     * @param appId App ID.
     */
    public void setAppId(Long appId) {
        this.appId = appId;
    }

    /**
     * Get push token value.
     * @return Push token.
     */
    public String getToken() {
        return token;
    }

    /**
     * Set push token value.
     * @param token Push token.
     */
    public void setToken(String token) {
        this.token = token;
    }
}
