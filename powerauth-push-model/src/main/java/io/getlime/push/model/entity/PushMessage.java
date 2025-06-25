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

package io.getlime.push.model.entity;

import io.getlime.push.model.enumeration.Priority;

import jakarta.validation.constraints.NotNull;

/**
 * Class representing a single push message payload.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class PushMessage {

    private String userId;
    private String activationId;
    private Priority priority = Priority.HIGH;
    private PushMessageAttributes attributes;
    private PushMessageBody body;

    /**
     * No-arg constructor.
     */
    public PushMessage() {
        attributes = new PushMessageAttributes();
    }

    /**
     * Get user ID.
     * @return User ID.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Set user ID.
     * @param userId User ID.
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Get PowerAuth activation ID.
     * @return Activation ID.
     */
    public String getActivationId() {
        return activationId;
    }

    /**
     * Set PowerAuth activation ID.
     * @param activationId Activation ID.
     */
    public void setActivationId(String activationId) {
        this.activationId = activationId;
    }

    /**
     * Get push message priority.
     * @return Priority.
     */
    public Priority getPriority() {
        return priority;
    }

    /**
     * Set push message priority.
     * @param priority Priority.
     */
    public void setPriority(@NotNull Priority priority) {
        this.priority = priority;
    }

    /**
     * Get the push body contents.
     * @return Push body contents.
     */
    public PushMessageBody getBody() {
        return body;
    }

    /**
     * Set the push body contents.
     * @param body Push body contents.
     */
    public void setBody(PushMessageBody body) {
        this.body = body;
    }

    /**
     * Get push body attributes.
     * @return Attributes.
     */
    public PushMessageAttributes getAttributes() {
        return attributes;
    }

    /**
     * Set push body attributes.
     * @param attributes Attributes.
     */
    public void setAttributes(PushMessageAttributes attributes) {
        this.attributes = attributes;
    }
}
