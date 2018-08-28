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
    private String projectId;
    private String privateKeyBase64;

    /**
     * Default constructor.
     */
    public UpdateAndroidRequest() {
    }

    /**
     * Constructor with details.
     * @param id Application credentials entity ID.
     * @param projectId Android project ID record.
     * @param privateKeyBase64 Base 64 encoded Android private key.
     */
    public UpdateAndroidRequest(Long id, String projectId, String privateKeyBase64) {
        this.id = id;
        this.projectId = projectId;
        this.privateKeyBase64 = privateKeyBase64;
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
     * Get the Android project ID record.
     * @return The Android project ID record.
     */
    public String getProjectId() {
        return projectId;
    }

    /**
     * Set the Android project ID record.
     * @param projectId The Android project ID record.
     */
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    /**
     * Get the base64 encoded Android private key.
     * @return The base64 encoded Android private key.
     */
    public String getPrivateKeyBase64() {
        return privateKeyBase64;
    }

    /**
     * Set the base64 encoded Android private key.
     * @param privateKeyBase64 The base64 encoded Android private key.
     */
    public void setPrivateKeyBase64(String privateKeyBase64) {
        this.privateKeyBase64 = privateKeyBase64;
    }

}
