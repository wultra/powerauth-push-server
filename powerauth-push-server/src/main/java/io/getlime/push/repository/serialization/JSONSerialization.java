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

package io.getlime.push.repository.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.getlime.push.controller.rest.PushCampaignController;
import io.getlime.push.model.entity.PushMessageBody;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper class for data serialization and deserialization.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class JSONSerialization {

    /**
     * Parsing message from JSON to PushMessageBody object.
     *
     * @param message Message to parse
     * @return PushMessageBody
     */
    public static PushMessageBody deserializePushMessageBody(String message) {
        PushMessageBody pushMessageBody = null;
        try {
            pushMessageBody = new ObjectMapper().readValue(message, PushMessageBody.class);
        } catch (IOException e) {
            Logger.getLogger(PushCampaignController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        }
        return pushMessageBody;
    }

    /**
     * Method used for parsing message into JSON.
     *
     * @param message string Message to be serialized.
     * @return JSON containing the message contents.
     */
    public static String serializePushMessageBody(PushMessageBody message) {
        String messageString = null;
        try {
            messageString = new ObjectMapper().writeValueAsString(message);
        } catch (JsonProcessingException e) {
            Logger.getLogger(PushCampaignController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        }
        return messageString;
    }

}
