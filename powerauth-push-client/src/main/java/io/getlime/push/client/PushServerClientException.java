/*
 * Copyright 2017 Lime - HighTech Solutions s.r.o.
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
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class PushServerClientException extends Exception {

    private Error error;

    public PushServerClientException(Error error) {
        this.error = error;
    }

    public PushServerClientException(String message, Error error) {
        super(message);
        this.error = error;
    }

    public PushServerClientException(String message, Throwable cause, Error error) {
        super(message, cause);
        this.error = error;
    }

    public PushServerClientException(Throwable cause, Error error) {
        super(cause);
        this.error = error;
    }

    public PushServerClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, Error error) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.error = error;
    }

    public void setError(Error error) {
        this.error = error;
    }

    public Error getError() {
        return error;
    }
}
