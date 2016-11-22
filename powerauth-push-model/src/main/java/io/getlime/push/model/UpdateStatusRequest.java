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

package io.getlime.push.model;

/**
 * Class representing request object responsible for updating activation status.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class UpdateStatusRequest {

    private String activationId;
    private String status;

    /**
     * Get PowerAuth 2.0 Activation ID.
     * @return Activation ID.
     */
    public String getActivationId() {
        return activationId;
    }

    /**
     * Set PowerAuth 2.0 Activation ID.
     * @param activationId Activation ID.
     */
    public void setActivationId(String activationId) {
        this.activationId = activationId;
    }

    /**
     * Get PowerAuth 2.0 Activation status (CREATED, OTP_USED, ACTIVE, BLOCKED, REMOVED).
     * @return PowerAuth 2.0 Activation status (CREATED, OTP_USED, ACTIVE, BLOCKED, REMOVED).
     */
    public String getStatus() {
        return status;
    }

    /**
     * Set PowerAuth 2.0 Activation status (CREATED, OTP_USED, ACTIVE, BLOCKED, REMOVED).
     * @param status PowerAuth 2.0 Activation status (CREATED, OTP_USED, ACTIVE, BLOCKED, REMOVED).
     */
    public void setStatus(String status) {
        this.status = status;
    }
}
