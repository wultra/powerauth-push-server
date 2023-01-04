/*
 * Copyright 2022 Wultra s.r.o.
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
package io.getlime.push.api;

import com.wultra.security.powerauth.client.model.error.PowerAuthClientException;

/**
 * Interface from PowerAuth server used for Push server tests.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public interface PowerAuthTestClient {

    void initializeClient(String powerAuthRestUrl) throws PowerAuthClientException;

    String initializeApplication(String applicationName, String applicationVersion) throws PowerAuthClientException;

    String createActivation(String userId) throws Exception;

    void blockActivation(String activationId) throws PowerAuthClientException;

    void unblockActivation(String activationId) throws PowerAuthClientException;

    String getApplicationId();

    void setApplicationId(String applicationId);

    String getActivationId();

    void setActivationId(String activationId);

    String getActivationId2();

    void setActivationId2(String activationId2);

    String getActivationId3();

    void setActivationId3(String activationId3);

    String getActivationId4();

    void setActivationId4(String activationId4);
    
}
