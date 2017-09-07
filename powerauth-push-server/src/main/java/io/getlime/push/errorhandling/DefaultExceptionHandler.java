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
package io.getlime.push.errorhandling;

import io.getlime.core.rest.model.base.entity.Error;
import io.getlime.core.rest.model.base.response.ErrorResponse;
import io.getlime.push.errorhandling.exceptions.PushServerException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of a default exception handler for the push server service.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
@ControllerAdvice(basePackages = { "io.getlime.push.controller.rest" })
public class DefaultExceptionHandler {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)  // 500
    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public ErrorResponse handleConflict(Exception e) {
        ErrorResponse response = new ErrorResponse(new Error(Error.Code.ERROR_GENERIC, e.getMessage()));
        Logger.getLogger(DefaultExceptionHandler.class.getName()).log(Level.SEVERE, null, e);
        return response;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)  // 400
    @ExceptionHandler(PushServerException.class)
    @ResponseBody
    public ErrorResponse handlePushException(Exception e) {
        ErrorResponse response = new ErrorResponse(new Error(Error.Code.ERROR_GENERIC, e.getMessage()));
        Logger.getLogger(DefaultExceptionHandler.class.getName()).log(Level.SEVERE, null, e);
        return response;
    }


    @ResponseStatus(HttpStatus.NOT_FOUND)  // 404
    @ExceptionHandler(EmptyResultDataAccessException.class)
    @ResponseBody
    public ErrorResponse handleDatabaseNotFound(Exception e) {
        ErrorResponse response = new ErrorResponse(new Error(DatabaseError.Code.ERROR_DATABASE, e.getMessage()));
        Logger.getLogger(DefaultExceptionHandler.class.getName()).log(Level.SEVERE, null, e);
        return response;
    }
}
