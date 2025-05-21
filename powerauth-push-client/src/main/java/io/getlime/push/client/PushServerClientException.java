/*
 * Copyright 2017 Wultra s.r.o.
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
 * Class representing a simple push server client error exception.
 * The detailed information about the error can be obtained by calling
 * 'getError' method.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
public class PushServerClientException extends Exception {

    /**
     * Error object.
     */
    private final Error error;

    /**
     * Constructor with message.
     * @param message Message.
     */
    public PushServerClientException(String message) {
        super(message);
        this.error = new PushServerClientError(message);
    }

    /**
     * Constructor with error object.
     * @param error Error object.
     */
    public PushServerClientException(Error error) {
        super(error != null ? error.getMessage() : null);
        this.error = error;
    }

    /**
     * Constructor with message and error object.
     * @param message Message.
     * @param error Error object.
     */
    public PushServerClientException(String message, Error error) {
        super(message);
        this.error = error;
    }

    /**
     * Constructor with message, cause and error object.
     * @param message Message.
     * @param cause Cause.
     * @param error Error object.
     */
    public PushServerClientException(String message, Throwable cause, Error error) {
        super(message, cause);
        this.error = error;
    }

    /**
     * Constructor with message, cause and error object.
     * @param message Message.
     * @param cause Cause.
     */
    public PushServerClientException(String message, Throwable cause) {
        super(message, cause);
        this.error = new PushServerClientError(message);
    }

    /**
     * Constructor with cause and error object.
     * @param cause Cause.
     * @param error Error object.
     */
    public PushServerClientException(Throwable cause, Error error) {
        super(cause);
        this.error = error;
    }

    /**
     * Constructor with message, cause, error object, that also contains
     * flags if there is writable stack trace and if suppression is possible.
     * @param message Message.
     * @param cause Cause.
     * @param enableSuppression Flag indicating if suppression is enabled.
     * @param writableStackTrace Flag indicating if the stack trace is writable.
     * @param error Error object.
     */
    public PushServerClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, Error error) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.error = error;
    }

    /**
     * Get error object.
     * @return Error object.
     */
    public Error getError() {
        return error;
    }
}
