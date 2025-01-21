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

package com.wultra.push.errorhandling.exceptions;

/**
 * General exception class thrown in specific push server methods,
 * accompanied by specific message.
 *
 * @author Martin Tupy, martin.tupy.work@gmail.com
 */
public class PushServerException extends Exception {

    /**
     * Constructor with message.
     * @param message Message.
     */
    public PushServerException(String message) {
        super(message);
    }

    /**
     * Constructor with message and cause.
     * @param message Message.
     * @param cause Cause.
     */
    public PushServerException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with cause.
     * @param cause Cause.
     */
    public PushServerException(Throwable cause) {
        super(cause);
    }
}
