/*
 * Copyright 2018 Wultra s.r.o.
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

package io.getlime.push.service.fcm;

import com.google.api.client.googleapis.util.Utils;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonGenerator;
import com.google.api.client.json.JsonParser;
import com.google.common.collect.ImmutableMap;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.wultra.core.rest.client.base.RestClientException;
import io.getlime.push.service.fcm.model.FcmErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Service for converting FCM model classes.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Service
public class FcmModelConverter {

    private static final Logger logger = LoggerFactory.getLogger(FcmModelConverter.class);

    // Google Json Factory (FCM model classes are not compatible with Jackson)
    private final JsonFactory jsonFactory = Utils.getDefaultJsonFactory();

    /**
     * Convert Rest client exception to FCM error code.
     *
     * @param exception Rest client exception.
     * @return FCM error code.
     */
    public MessagingErrorCode convertExceptionToErrorCode(RestClientException exception) {
        final FcmErrorResponse response = new FcmErrorResponse();
        MessagingErrorCode code;
        try {
            final String error = exception.getResponse();
            final JsonParser parser = jsonFactory.createJsonParser(error);
            parser.parseAndClose(response);
            logger.debug("FCM messaging error code: {}", response.getMessagingErrorCode());
            logger.debug("FCM status: {}", response.getStatus());
            logger.debug("FCM error message: {}", response.getErrorMessage());
            code = response.getMessagingErrorCode();
            if (code == null) {
                code = MessagingErrorCode.INTERNAL;
            }
        } catch (IOException ex) {
            logger.error("Error occurred while parsing error response: {}", ex.getMessage(), ex);
            code = MessagingErrorCode.INTERNAL;
        }
        return code;
    }

    /**
     * Convert Android notification to String.
     *
     * @param notification Android notification.
     * @return String representing Android notification.
     */
    public String convertNotificationToString(AndroidNotification notification) {
        if (notification == null) {
            return null;
        }
        try {
            final StringWriter writer = new StringWriter();
            final JsonGenerator gen = jsonFactory.createJsonGenerator(writer);
            gen.serialize(notification);
            gen.close();
            return writer.toString();
        } catch (IOException ex) {
            logger.error("Json serialization failed: {}", ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Convert Message to payload for WebClient.
     *
     * @param message      Message to send.
     * @param validateOnly Whether to perform only validation.
     * @return Flux of DataBuffer.
     */
    public Flux<DataBuffer> convertMessageToFlux(Message message, boolean validateOnly) {
        ImmutableMap.Builder<String, Object> payloadBuilder = ImmutableMap.<String, Object>builder().put("message", message);
        if (validateOnly) {
            payloadBuilder.put("validate_only", true);
        }
        ImmutableMap<String, Object> payload = payloadBuilder.build();
        String convertedMessage;
        try {
            StringWriter writer = new StringWriter();
            JsonGenerator gen = jsonFactory.createJsonGenerator(writer);
            gen.serialize(payload);
            gen.close();
            convertedMessage = writer.toString();
        } catch (IOException ex) {
            logger.error("Json serialization failed: {}", ex.getMessage(), ex);
            return null;
        }
        DefaultDataBufferFactory factory = new DefaultDataBufferFactory();
        DefaultDataBuffer dataBuffer = factory.wrap(ByteBuffer.wrap(convertedMessage.getBytes(StandardCharsets.UTF_8)));
        return Flux.just(dataBuffer);
    }

}
