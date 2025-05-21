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

package io.getlime.push.repository.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.getlime.push.errorhandling.exceptions.PushServerException;
import io.getlime.push.model.entity.PushMessageBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Helper class for data serialization and deserialization.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Service
public class JsonSerialization {

    private static final Logger logger = LoggerFactory.getLogger(JsonSerialization.class);

    private final ObjectMapper objectMapper;

    /**
     * Constructor with ObjectMapper instance.
     * @param objectMapper Object mapper.
     */
    @Autowired
    public JsonSerialization(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Parsing message from JSON to PushMessageBody object.
     *
     * @param message Message to parse
     * @return PushMessageBody
     * @throws PushServerException In case object mapping fails.
     */
    public PushMessageBody deserializePushMessageBody(String message) throws PushServerException {
        PushMessageBody pushMessageBody;
        try {
            pushMessageBody = objectMapper.readValue(message, PushMessageBody.class);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new PushServerException("Failed parsing from JSON", e);
        }
        return pushMessageBody;
    }

    /**
     * Method used for parsing message into JSON.
     *
     * @param message string Message to be serialized.
     * @return JSON containing the message contents.
     * @throws PushServerException In case object mapping fails.
     */
    public String serializePushMessageBody(PushMessageBody message) throws PushServerException {
        String messageString;
        try {
            messageString = objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
            throw new PushServerException("Failed parsing into JSON", e);
        }
        return messageString;
    }
}
