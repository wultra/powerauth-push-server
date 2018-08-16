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
 * Update Android configuration request.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class UpdateAndroidRequest {

    private Long id;
    private String bundle;
    private String token;

    /**
     * Default constructor.
     */
    public UpdateAndroidRequest() {
    }

    /**
     * Constructor with details.
     * @param id Application credentials entity ID.
     * @param bundle Android bundle record.
     * @param token Android token record (server key).
     */
    public UpdateAndroidRequest(Long id, String bundle, String token) {
        this.id = id;
        this.bundle = bundle;
        this.token = token;
    }

    /**
     * Get application credentials entity ID.
     * @return Application credentials entity ID.
     */
    public Long getId() {
        return id;
    }

    /**
     * Set application credentials entity ID.
     * @param id Application credentials entity ID.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get the Android bundle record.
     * @return The Android bundle record.
     */
    public String getBundle() {
        return bundle;
    }

    /**
     * Set the Android bundle record.
     * @param bundle The Android bundle record.
     */
    public void setBundle(String bundle) {
        this.bundle = bundle;
    }

    /**
     * Get the Android token record.
     * @return The Android token record.
     */
    public String getToken() {
        return token;
    }

    /**
     * Set the Android token record.
     * @param token The Android token record.
     */
    public void setToken(String token) {
        this.token = token;
    }
}
