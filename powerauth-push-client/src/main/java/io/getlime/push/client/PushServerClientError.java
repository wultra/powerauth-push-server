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
package io.getlime.push.client;

import io.getlime.core.rest.model.base.entity.Error;

/**
 * Base error class for push client.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class PushServerClientError extends Error {

    private static String ERROR_VALIDATION = "ERROR_VALIDATION";


    public PushServerClientError(String message) {
        super(ERROR_VALIDATION , message);
    }
}
