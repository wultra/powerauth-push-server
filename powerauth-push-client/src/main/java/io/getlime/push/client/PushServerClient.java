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

package io.getlime.push.client;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.getlime.push.model.*;
import io.getlime.push.model.entity.PushMessage;

import java.io.IOException;
import java.util.List;

/**
 * Simple class for interacting with the push server.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class PushServerClient {

    /**
     * Default constructor.
     */
    public PushServerClient() {
        Unirest.setObjectMapper(new ObjectMapper() {
            private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    return jacksonObjectMapper.readValue(value, valueType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public String writeValue(Object value) {
                try {
                    return jacksonObjectMapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /**
     * Constructor with a push server base URL.
     *
     * @param serviceBaseUrl URL pointing to the running push server instance, for example "http://localhost:8080/powerauth-push-server".
     */
    public PushServerClient(String serviceBaseUrl) {
        this();
        this.serviceBaseUrl = serviceBaseUrl;
    }

    private String serviceBaseUrl;

    /**
     * Set the service base URL.
     * @param serviceBaseUrl Base URL.
     */
    public void setServiceBaseUrl(String serviceBaseUrl) {
        this.serviceBaseUrl = serviceBaseUrl;
    }

    /**
     * Register anonymous device to the push server.
     * @param appId PowerAuth 2.0 application app ID.
     * @param token Token received from the push service provider (APNs, FCM).
     * @param platform Mobile platform (iOS, Android).
     * @return True if device registration was successful, false otherwise.
     */
    public boolean registerDevice(Long appId, String token, MobilePlatform platform) {
        return registerDevice(appId, token, platform, null);
    }

    /**
     * Register device associated with activation ID to the push server.
     * @param appId PowerAuth 2.0 application app ID.
     * @param token Token received from the push service provider (APNs, FCM).
     * @param platform Mobile platform (iOS, Android).
     * @param activationId PowerAuth 2.0 activation ID.
     * @return True if device registration was successful, false otherwise.
     */
    public boolean registerDevice(Long appId, String token, MobilePlatform platform, String activationId) {
        CreateDeviceRegistrationRequest request = new CreateDeviceRegistrationRequest();
        request.setAppId(appId);
        request.setToken(token);
        request.setPlatform(platform.value());
        request.setActivationId(activationId);
        StatusResponse response = sendObjectImpl("/push/device/create", request);
        return response.getStatus().equals(StatusResponse.OK);
    }

    /**
     * Send a single push message to application with given ID.
     * @param appId PowerAuth 2.0 application app ID.
     * @param pushMessage Push message to be sent.
     * @return SendMessageResponse in case everything went OK, ErrorResponse in case of an error.
     */
    public StatusResponse sendNotification(Long appId, PushMessage pushMessage) {
        SendPushMessageRequest request = new SendPushMessageRequest();
        request.setAppId(appId);
        request.setPush(pushMessage);
        return sendObjectImpl("/push/message/send", request);
    }

    /**
     * Send a push message batch to application with given ID.
     * @param appId PowerAuth 2.0 application app ID.
     * @param batch Push message batch to be sent.
     * @return SendMessageResponse in case everything went OK, ErrorResponse in case of an error.
     */
    public StatusResponse sendNotificationBatch(Long appId, List<PushMessage> batch) {
        SendBatchMessageRequest request = new SendBatchMessageRequest();
        request.setAppId(appId);
        request.setBatch(batch);
        return sendObjectImpl("/push/message/batch/send", request);
    }

    private StatusResponse sendObjectImpl(String url, Object request) {
        try {
            // Fetch post response from given URL and for provided request object
            HttpResponse<SendMessageResponse> response = Unirest.post(serviceBaseUrl + url)
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .body(request)
                    .asObject(SendMessageResponse.class);
            if (response.getStatus() == 200) {
                return response.getBody();
            } else {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                ErrorResponse errResp = mapper.readValue(response.getRawBody(), ErrorResponse.class);
                return errResp;
            }
        } catch (UnirestException e) {
            ErrorResponse errResp = new ErrorResponse();
            errResp.setMessage("Network communication has failed.");
            return errResp;
        } catch (JsonParseException e) {
            ErrorResponse errResp = new ErrorResponse();
            errResp.setMessage("JSON parsing has failed.");
            return errResp;
        } catch (JsonMappingException e) {
            ErrorResponse errResp = new ErrorResponse();
            errResp.setMessage("JSON mapping has failed.");
            return errResp;
        } catch (IOException e) {
            ErrorResponse errResp = new ErrorResponse();
            errResp.setMessage("Unknown IO error.");
            return errResp;
        }
    }


}
