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
package io.getlime.push.errorhandling;

import com.wultra.security.powerauth.client.model.error.PowerAuthClientException;
import io.getlime.core.rest.model.base.entity.Error;
import io.getlime.core.rest.model.base.response.ErrorResponse;
import io.getlime.push.errorhandling.exceptions.InboxMessageNotFoundException;
import io.getlime.push.errorhandling.exceptions.PushServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Implementation of a default exception handler for the push server service.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@ControllerAdvice(basePackages = { "io.getlime.push.controller.rest" })
public class DefaultExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(DefaultExceptionHandler.class);

    /**
     * Handle any unexpected throwable errors.
     * @param t Throwable.
     * @return Error response.
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)  // 500
    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public ErrorResponse handleUnexpectedError(Throwable t) {
        logger.error(t.getMessage(), t);
        return new ErrorResponse(Error.Code.ERROR_GENERIC, t);
    }

    /**
     * Handle exceptions related to push server operation.
     * @param e {@link PushServerException}
     * @return Error response.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)  // 400
    @ExceptionHandler(PushServerException.class)
    @ResponseBody
    public ErrorResponse handlePushException(Exception e) {
        logger.error(e.getMessage(), e);
        return new ErrorResponse(Error.Code.ERROR_GENERIC, e);
    }

    /**
     * Handle database errors in case entities are not fount.
     * @param e Empty result returned.
     * @return Error response.
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)  // 404
    @ExceptionHandler(EmptyResultDataAccessException.class)
    @ResponseBody
    public ErrorResponse handleDatabaseNotFound(Exception e) {
        logger.error(e.getMessage(), e);
        return new ErrorResponse(DatabaseError.Code.ERROR_DATABASE, e);
    }

    /**
     * Handle errors caused by entity collisions.
     * @param e Data integrity violation occurred.
     * @return Error response.
     */
    @ResponseStatus(HttpStatus.CONFLICT) // 409
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseBody
    public ErrorResponse handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        logger.error(e.getMessage(), e);
        return new ErrorResponse(DataIntegrityError.Code.ERROR_DATA_INTEGRITY, e);
    }

    /**
     * Handle errors related to communication with PowerAuth Server.
     * @param e Error caused by issue when communication with PowerAuth Server.
     * @return Error response.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)  // 400
    @ExceptionHandler(PowerAuthClientException.class)
    @ResponseBody
    public ErrorResponse handlePowerAuthClientException(Exception e) {
        logger.error(e.getMessage(), e);
        return new ErrorResponse(WebServiceError.Code.ERROR_PA_SERVER_COMM, e);
    }

    /**
     * Handle exceptions related to inbox.
     * @param e {@link InboxMessageNotFoundException}
     * @return Error response.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)  // 400
    @ExceptionHandler(InboxMessageNotFoundException.class)
    @ResponseBody
    public ErrorResponse handleInboxMessageNotFoundException(InboxMessageNotFoundException e) {
        logger.info("Message was not found in the inbox. " + e.getMessage());
        logger.debug("Exception detail: ", e);
        return new ErrorResponse(InboxError.Code.ERROR_MESSAGE_NOT_FOUND, e);
    }

}
