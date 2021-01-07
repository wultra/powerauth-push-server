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

package io.getlime.push.model.request;

import com.wultra.security.powerauth.client.v3.ActivationStatus;

/**
 * Class representing request object responsible for updating activation status.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class UpdateDeviceStatusRequest {

    private String activationId;
    private ActivationStatus activationStatus;

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
     * Get PowerAuth activation status.
     * @return Activation status.
     */
    public ActivationStatus getActivationStatus() {
        return activationStatus;
    }

    /**
     * Set PowerAuth activation status.
     * @param activationStatus Activation status.
     */
    public void setActivationStatus(ActivationStatus activationStatus) {
        this.activationStatus = activationStatus;
    }

}
