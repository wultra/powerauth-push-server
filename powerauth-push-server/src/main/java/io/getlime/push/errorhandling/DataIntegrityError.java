/*
 * Copyright 2020 Wultra s.r.o.
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

package io.getlime.push.errorhandling;

import io.getlime.core.rest.model.base.entity.Error;

/**
 * Exception for data integrity error.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class DataIntegrityError extends Error {

    /**
     * Error code class.
     */
    public static class Code extends Error.Code {

        /**
         * Error code value.
         */
        public static final String ERROR_DATA_INTEGRITY = "ERROR_DATA_INTEGRITY";

        /**
         * Default constructor.
         */
        public Code() {
        }
    }
}
