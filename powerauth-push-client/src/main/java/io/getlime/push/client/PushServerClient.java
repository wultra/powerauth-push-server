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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.getlime.core.rest.model.base.entity.Error;
import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ErrorResponse;;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.core.rest.model.base.response.Response;
import io.getlime.push.model.*;
import io.getlime.push.model.entity.PushMessage;
import io.getlime.push.model.entity.PushSendResult;

import java.io.IOException;
import java.util.List;

/**
 * Simple class for interacting with the push server.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 */
public class PushServerClient {

    private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper = null;

    /**
     * Default constructor.
     */
    public PushServerClient() {

        jacksonObjectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

        Unirest.setObjectMapper(new ObjectMapper() {

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
    public boolean registerDevice(Long appId, String token, MobilePlatform platform) throws PushServerClientException {
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
    public boolean registerDevice(Long appId, String token, MobilePlatform platform, String activationId) throws PushServerClientException {
        CreateDeviceRegistrationRequest request = new CreateDeviceRegistrationRequest();
        request.setAppId(appId);
        request.setToken(token);
        request.setPlatform(platform.value());
        request.setActivationId(activationId);
        TypeReference<ObjectResponse> typeReference = new TypeReference<ObjectResponse>() {};
        ObjectResponse<?> response = sendObjectImpl("/push/device/create", new ObjectRequest<>(request), typeReference);
        return response.getStatus().equals(Response.Status.OK);
    }

    /**
     * Send a single push message to application with given ID.
     * @param appId PowerAuth 2.0 application app ID.
     * @param pushMessage Push message to be sent.
     * @return SendMessageResponse in case everything went OK, ErrorResponse in case of an error.
     */
    public ObjectResponse<PushSendResult> sendNotification(Long appId, PushMessage pushMessage) throws PushServerClientException {
        SendPushMessageRequest request = new SendPushMessageRequest();
        request.setAppId(appId);
        request.setPush(pushMessage);
        TypeReference<ObjectResponse<PushSendResult>> typeReference = new TypeReference<ObjectResponse<PushSendResult>>() {};
        return sendObjectImpl("/push/message/send", new ObjectRequest<>(request), typeReference);
    }

    /**
     * Send a push message batch to application with given ID.
     * @param appId PowerAuth 2.0 application app ID.
     * @param batch Push message batch to be sent.
     * @return SendMessageResponse in case everything went OK, ErrorResponse in case of an error.
     */
    public ObjectResponse<PushSendResult> sendNotificationBatch(Long appId, List<PushMessage> batch) throws PushServerClientException {
        SendBatchMessageRequest request = new SendBatchMessageRequest();
        request.setAppId(appId);
        request.setBatch(batch);
        TypeReference<ObjectResponse<PushSendResult>> typeReference = new TypeReference<ObjectResponse<PushSendResult>>() {};
        return sendObjectImpl("/push/message/batch/send", new ObjectRequest<>(request), typeReference);
    }

    private <T> ObjectResponse<T> sendObjectImpl(String url, Object request, TypeReference typeReference) throws PushServerClientException {
        try {
            // Fetch post response from given URL and for provided request object
            HttpResponse response = Unirest.post(serviceBaseUrl + url)
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .body(request)
                    .asString();
            if (response.getStatus() == 200) {
                return this.jacksonObjectMapper.readValue(response.getRawBody(), typeReference);
            } else {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                ErrorResponse errResp = mapper.readValue(response.getRawBody(), ErrorResponse.class);
                throw new PushServerClientException(response.getStatusText(), errResp.getResponseObject());
            }
        } catch (UnirestException e) {
            throw new PushServerClientException(new Error("PUSH_SERVER_CLIENT_ERROR", "Network communication has failed."));
        } catch (JsonParseException e) {
            throw new PushServerClientException(new Error("PUSH_SERVER_CLIENT_ERROR", "JSON parsing has failed."));
        } catch (JsonMappingException e) {
            throw new PushServerClientException(new Error("PUSH_SERVER_CLIENT_ERROR", "JSON mapping has failed."));
        } catch (IOException e) {
            throw new PushServerClientException(new Error("PUSH_SERVER_CLIENT_ERROR", "Unknown IO error."));
        }
    }


}
