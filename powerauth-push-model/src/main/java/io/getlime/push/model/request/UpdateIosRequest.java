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
 * Update iOS configuration request.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class UpdateIosRequest {

    private Long id;
    private String bundle;
    private String keyId;
    private String teamId;
    private String privateKeyBase64;

    /**
     * Default constructor.
     */
    public UpdateIosRequest() {
    }

    /**
     * Constructor with details.
     * @param id Application credentials entity ID.
     * @param bundle The iOS bundle record.
     * @param keyId The iOS key ID record.
     * @param teamId The iOS team ID record.
     * @param privateKeyBase64 Base64 encoded private key.
     */
    public UpdateIosRequest(Long id, String bundle, String keyId, String teamId, String privateKeyBase64) {
        this.id = id;
        this.bundle = bundle;
        this.keyId = keyId;
        this.teamId = teamId;
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
     * Get the iOS bundle record.
     * @return The iOS bundle record.
     */
    public String getBundle() {
        return bundle;
    }

    /**
     * Set the iOS bundle record.
     * @param bundle The iOS bundle record.
     */
    public void setBundle(String bundle) {
        this.bundle = bundle;
    }

    /**
     * Get the iOS key ID record.
     * @return The iOS key ID record.
     */
    public String getKeyId() {
        return keyId;
    }

    /**
     * Set the iOS key ID record.
     * @param keyId The iOS key ID record.
     */
    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    /**
     * Get the iOS team ID record.
     * @return The iOS team ID record.
     */
    public String getTeamId() {
        return teamId;
    }

    /**
     * Set the iOS team ID record.
     * @param teamId The iOS team ID record.
     */
    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    /**
     * Get base64 encoded private key.
     * @return Base 64 encoded private key.
     */
    public String getPrivateKeyBase64() {
        return privateKeyBase64;
    }

    /**
     * Set base64 encoded private key.
     * @param privateKeyBase64 Base 64 encoded private key.
     */
    public void setPrivateKeyBase64(String privateKeyBase64) {
        this.privateKeyBase64 = privateKeyBase64;
    }
}
