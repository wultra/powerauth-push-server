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
package io.getlime.push.model.response;

import io.getlime.push.model.entity.PushServerApplication;

/**
 * Get application credentials entity detail response.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class GetApplicationDetailResponse {

    private PushServerApplication application;
    private String iosBundle;
    private String iosKeyId;
    private String iosTeamId;
    private String androidBundle;
    private String androidToken;

    /**
     * Default constructor.
     */
    public GetApplicationDetailResponse() {
    }

    /**
     * Get push server application.
     * @return Push server application.
     */
    public PushServerApplication getApplication() {
        return application;
    }

    /**
     * Set push server application.
     * @param application Push server application.
     */
    public void setApplication(PushServerApplication application) {
        this.application = application;
    }

    /**
     * Get the iOS bundle record.
     * @return The iOS bundle record.
     */
    public String getIosBundle() {
        return iosBundle;
    }

    /**
     * Set the iOS bundle record.
     * @param iosBundle The iOS bundle record.
     */
    public void setIosBundle(String iosBundle) {
        this.iosBundle = iosBundle;
    }

    /**
     * Get the iOS key record.
     * @return The iOS key record.
     */
    public String getIosKeyId() {
        return iosKeyId;
    }

    /**
     * Set the iOS key record.
     * @param iosKeyId The iOS key record.
     */
    public void setIosKeyId(String iosKeyId) {
        this.iosKeyId = iosKeyId;
    }

    /**
     * Get the iOS team ID record.
     * @return The iOS team ID record.
     */
    public String getIosTeamId() {
        return iosTeamId;
    }

    /**
     * Set the iOS team ID record.
     * @param iosTeamId The iOS team ID record.
     */
    public void setIosTeamId(String iosTeamId) {
        this.iosTeamId = iosTeamId;
    }

    /**
     * Get the Android bundle record.
     * @return The Android bundle record.
     */
    public String getAndroidBundle() {
        return androidBundle;
    }

    /**
     * Set the Android bundle record.
     * @param androidBundle The Android bundle record.
     */
    public void setAndroidBundle(String androidBundle) {
        this.androidBundle = androidBundle;
    }

    /**
     * Get the Android token (server key) record.
     * @return The Android token (server key) record.
     */
    public String getAndroidToken() {
        return androidToken;
    }

    /**
     * Set the Android token (server key) record.
     * @param androidToken The Android token (server key) record.
     */
    public void setAndroidToken(String androidToken) {
        this.androidToken = androidToken;
    }
}
