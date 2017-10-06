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
import io.getlime.core.rest.model.base.response.ErrorResponse;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.core.rest.model.base.response.Response;
import io.getlime.push.model.base.PagedResponse;
import io.getlime.push.model.entity.ListOfUsers;
import io.getlime.push.model.entity.PushMessage;
import io.getlime.push.model.entity.PushMessageBody;
import io.getlime.push.model.entity.PushMessageSendResult;
import io.getlime.push.model.request.*;
import io.getlime.push.model.response.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple class for interacting with the push server.
 *
 * @author Petr Dvorak, petr@lime-company.eu
 * @author Martin Tupy, martin.tupy.work@gmail.com
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
     *
     * @param serviceBaseUrl Base URL.
     */
    public void setServiceBaseUrl(String serviceBaseUrl) {
        this.serviceBaseUrl = serviceBaseUrl;
    }

    /**
     * Returns service information
     *
     * @return True if service is running.
     */
    public ObjectResponse<ServiceStatusResponse> getServiceStatus() throws PushServerClientException {
        TypeReference<ObjectResponse<ServiceStatusResponse>> typeReference = new TypeReference<ObjectResponse<ServiceStatusResponse>>() {
        };
        return getObjectImpl("/push/service/status", null, typeReference);
    }

    /**
     * Register anonymous device to the push server.
     *
     * @param appId PowerAuth 2.0 application app ID.
     * @param token Token received from the push service provider (APNs, FCM).
     * @param platform Mobile platform (iOS, Android).
     * @return True if device registration was successful, false otherwise.
     */
    public boolean createDevice(Long appId, String token, MobilePlatform platform) throws PushServerClientException {
        return createDevice(appId, token, platform, null);
    }

    /**
     * Register device associated with activation ID to the push server.
     *
     * @param appId PowerAuth 2.0 application app ID.
     * @param token Token received from the push service provider (APNs, FCM).
     * @param platform Mobile platform (iOS, Android).
     * @param activationId PowerAuth 2.0 activation ID.
     * @return True if device registration was successful, false otherwise.
     */
    public boolean createDevice(Long appId, String token, MobilePlatform platform, String activationId) throws PushServerClientException {
        CreateDeviceRequest request = new CreateDeviceRequest();
        request.setAppId(appId);
        request.setToken(token);
        request.setPlatform(platform.value());
        request.setActivationId(activationId);
        TypeReference<Response> typeReference = new TypeReference<Response>() {
        };
        ObjectResponse<?> response = postObjectImpl("/push/device/create", request, typeReference);
        return response.getStatus().equals(Response.Status.OK);
    }

    /**
     * Remove device from the push server
     *
     * @param appId PowerAuth 2.0 application app ID.
     * @param token Token received from the push service provider.
     * @return True if device removal was successful, false otherwise.
     */
    public boolean deleteDevice(Long appId, String token) throws PushServerClientException {
        DeleteDeviceRequest request = new DeleteDeviceRequest();
        request.setAppId(appId);
        request.setToken(token);
        TypeReference<DeleteCampaignResponse> typeReference = new TypeReference<DeleteCampaignResponse>() {
        };
        ObjectResponse<DeleteCampaignResponse> response = postObjectImpl("/push/device/delete", request, typeReference);
        return response.getResponseObject().isDeleted();
    }

    /**
     * Update activation status for given device registration.
     *
     * @param activationId Identifier of activation
     * @return True if updating went successful, false otherwise.
     */
    public boolean updateDeviceStatus(String activationId) throws PushServerClientException {
        UpdateDeviceStatusRequest request = new UpdateDeviceStatusRequest();
        request.setActivationId(activationId);
        TypeReference<Response> typeReference = new TypeReference<Response>() {
        };
        ObjectResponse<?> response = postObjectImpl("/push/device/status/update", request, typeReference);
        return response.getStatus().equals(Response.Status.OK);
    }

    /**
     * Send a single push message to application with given ID.
     *
     * @param appId PowerAuth 2.0 application app ID.
     * @param pushMessage Push message to be sent.
     * @return SendMessageResponse in case everything went OK, ErrorResponse in case of an error.
     */
    public ObjectResponse<PushMessageSendResult> sendPushMessage(Long appId, PushMessage pushMessage) throws PushServerClientException {
        SendPushMessageRequest request = new SendPushMessageRequest();
        request.setAppId(appId);
        request.setMessage(pushMessage);
        TypeReference<ObjectResponse<PushMessageSendResult>> typeReference = new TypeReference<ObjectResponse<PushMessageSendResult>>() {
        };
        return postObjectImpl("/push/message/send", request, typeReference);
    }

    /**
     * Send a push message batch to application with given ID.
     *
     * @param appId PowerAuth 2.0 application app ID.
     * @param batch Push message batch to be sent.
     * @return SendMessageResponse in case everything went OK, ErrorResponse in case of an error.
     */
    public ObjectResponse<PushMessageSendResult> sendPushMessageBatch(Long appId, List<PushMessage> batch) throws PushServerClientException {
        SendPushMessageBatchRequest request = new SendPushMessageBatchRequest();
        request.setAppId(appId);
        request.setBatch(batch);
        TypeReference<ObjectResponse<PushMessageSendResult>> typeReference = new TypeReference<ObjectResponse<PushMessageSendResult>>() {
        };
        return postObjectImpl("/push/message/batch/send", request, typeReference);
    }

    /**
     * Create a campaign.
     *
     * @param message Message which attributes are defined in PushMessageBody.
     * @return ID of new created campaign.
     */
    public ObjectResponse<CreateCampaignResponse> createCampaign(Long appId, PushMessageBody message) throws PushServerClientException {
        CreateCampaignRequest request = new CreateCampaignRequest();
        request.setAppId(appId);
        request.setMessage(message);
        TypeReference<ObjectResponse<CreateCampaignRequest>> typeReference = new TypeReference<ObjectResponse<CreateCampaignRequest>>() {
        };
        return postObjectImpl("/push/campaign/create", request, typeReference);
    }

    /**
     * Delete a campaign specified with campaignId.
     *
     * @return True if campaign is removed, false otherwise.
     */
    public boolean deleteCampaign(Long campaignId) throws PushServerClientException {
        try {
            String campaignIdSanitized = URLEncoder.encode(String.valueOf(campaignId), "utf-8");
            TypeReference<ObjectResponse<DeleteCampaignResponse>> typeReference = new TypeReference<ObjectResponse<DeleteCampaignResponse>>() {
            };
            ObjectResponse<?> response = postObjectImpl("/push/campaign/" + campaignIdSanitized + "/delete", null, typeReference);
            return response.getStatus().equals(Response.Status.OK);
        } catch (UnsupportedEncodingException e) {
            throw new PushServerClientException(new Error("PUSH_SERVER_CLIENT_ERROR", e.getMessage()));
        }
    }

    /**
     * Get list of campaigns, dependent on all param
     *
     * @param all true to get whole list, false to get campaigns that are only sent
     * @return List of campaigns.
     */
    public ObjectResponse<ListOfCampaignsResponse> getListOfCampaigns(boolean all) throws PushServerClientException {
        TypeReference<ObjectResponse<ListOfCampaignsResponse>> typeReference = new TypeReference<ObjectResponse<ListOfCampaignsResponse>>() {
        };
        Map<String, Object> params = new HashMap<>();
        params.put("all", all);
        return getObjectImpl("/push/campaign/list", params, typeReference);
    }

    /**
     * Get a campaign specified with campaignID.
     *
     * @param campaignId ID of campaign to get.
     * @return Details of campaign, defined in CampaignResponse
     */
    public ObjectResponse<CampaignResponse> getCampaign(Long campaignId) throws PushServerClientException {
        try {
            String campaignIdSanitized = URLEncoder.encode(String.valueOf(campaignId), "utf-8");
            TypeReference<ObjectResponse<CampaignResponse>> typeReference = new TypeReference<ObjectResponse<CampaignResponse>>() {
            };
            return getObjectImpl("/push/campaign/" + campaignIdSanitized + "/detail", null, typeReference);
        } catch (UnsupportedEncodingException e) {
            throw new PushServerClientException(new Error("PUSH_SERVER_CLIENT_ERROR", e.getMessage()));
        }
    }

    /**
     * Add a list of users to a specific campaign
     *
     * @param campaignId Identifier of campaign.
     * @param users List of users to add.
     * @return True if adding was successful, false otherwise.
     */
    public boolean addUsersToCampaign(Long campaignId, List<String> users) throws PushServerClientException {
        try {
            ListOfUsers listOfUsers = new ListOfUsers();
            listOfUsers.addAll(users);
            String campaignIdSanitized = URLEncoder.encode(String.valueOf(campaignId), "utf-8");
            TypeReference<Response> typeReference = new TypeReference<Response>() {
            };
            ObjectResponse<?> response = putObjectImpl("/push/campaign/" + campaignIdSanitized + "/user/add", listOfUsers, typeReference);
            return response.getStatus().equals(Response.Status.OK);
        } catch (UnsupportedEncodingException e) {
            throw new PushServerClientException(new Error("PUSH_SERVER_CLIENT_ERROR", e.getMessage()));
        }
    }

    /**
     * Get a list of users in paged format from specific campaign
     *
     * @param campaignId Identifier of campaign.
     * @param page Page number.
     * @param size Size of elements per page.
     * @return Page of users specified with params.
     */
    public ObjectResponse<PagedResponse<ListOfUsersFromCampaignResponse>> getListOfUsersFromCampaign(Long campaignId, int page, int size) throws PushServerClientException {
        try {
            String campaignIdSanitized = URLEncoder.encode(String.valueOf(campaignId), "utf-8");
            TypeReference<ObjectResponse<PagedResponse<ListOfUsersFromCampaignResponse>>> typeReference = new TypeReference<ObjectResponse<PagedResponse<ListOfUsersFromCampaignResponse>>>() {
            };
            Map<String, Object> params = new HashMap<>();
            params.put("page", page);
            params.put("size", size);
            return getObjectImpl("/push/campaign/" + campaignIdSanitized + "/user/list", params, typeReference);
        } catch (UnsupportedEncodingException e) {
            throw new PushServerClientException(new Error("PUSH_SERVER_CLIENT_ERROR", e.getMessage()));
        }
    }

    /**
     * Delete a list of users from specific campaign.
     *
     * @param campaignId Identifier of campaign.
     * @param users List of users' Identifiers to delete.
     * @return True if deletion was successful, false otherwise.
     */
    public boolean deleteUsersFromCampaign(Long campaignId, List<String> users) throws PushServerClientException {
        try {
            ListOfUsers listOfUsers = new ListOfUsers();
            listOfUsers.addAll(users);
            String campaignIdSanitized = URLEncoder.encode(String.valueOf(campaignId), "utf-8");
            TypeReference<Response> typeReference = new TypeReference<Response>() {
            };
            ObjectResponse<?> response = postObjectImpl("/push/campaign/" + campaignIdSanitized + "/user/delete", listOfUsers, typeReference);
            return response.getStatus().equals(Response.Status.OK);
        } catch (UnsupportedEncodingException e) {
            throw new PushServerClientException(new Error("PUSH_SERVER_CLIENT_ERROR", e.getMessage()));
        }
    }

    /**
     * Send a campaign on test user for trying its correctness.
     *
     * @param campaignId Identifier of campaign.
     * @param userId Identifier of test user.
     * @return True if sent, else otherwise.
     */
    public boolean sendTestCampaign(Long campaignId, String userId) throws PushServerClientException {
        try {
            String campaignIdSanitized = URLEncoder.encode(String.valueOf(campaignId), "utf-8");
            TestCampaignRequest testCampaignRequest = new TestCampaignRequest();
            testCampaignRequest.setUserId(userId);
            TypeReference<Response> typeReference = new TypeReference<Response>() {
            };
            ObjectResponse<?> response = postObjectImpl("/push/campaign/send/test/" + campaignIdSanitized, testCampaignRequest, typeReference);
            return response.getStatus().equals(Response.Status.OK);
        } catch (UnsupportedEncodingException e) {
            throw new PushServerClientException(new Error("PUSH_SERVER_CLIENT_ERROR", e.getMessage()));
        }
    }

    /**
     * Send a specific campaign to users carrying this campaignID in PushCampaignUser model, but only once per device identified by token.
     *
     * @param campaignId Identifier of campaign.
     * @return True if sent, else otherwise.
     */
    public boolean sendCampaign(Long campaignId) throws PushServerClientException {
        try {
            String campaignIdSanitized = URLEncoder.encode(String.valueOf(campaignId), "utf-8");
            TypeReference<Response> typeReference = new TypeReference<Response>() {
            };
            ObjectResponse<?> response = postObjectImpl("/push/campaign/send/live/" + campaignIdSanitized, null, typeReference);
            return response.getStatus().equals(Response.Status.OK);
        } catch (UnsupportedEncodingException e) {
            throw new PushServerClientException(new Error("PUSH_SERVER_CLIENT_ERROR", e.getMessage()));
        }
    }

    /**
     * Prepare GET object response.
     *
     * @param url specific url of method
     * @param params params to pass to url path, optional
     * @param typeReference reference on type for parsing into JSON
     */
    private <T> ObjectResponse<T> getObjectImpl(String url, Map<String, Object> params, TypeReference typeReference) throws PushServerClientException {
        try {
            HttpResponse response = Unirest.get(serviceBaseUrl + url)
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .queryString(params)
                    .asString();
            return checkHttpResponseFormat(typeReference, response);
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

    /**
     * Prepare POST object response.
     *
     * @param url specific url of method
     * @param request request body
     * @param typeReference reference on type for parsing into JSON
     */
    private <T> ObjectResponse<T> postObjectImpl(String url, Object request, TypeReference typeReference) throws PushServerClientException {
        try {
            // Fetch post response from given URL and for provided request object
            HttpResponse response = Unirest.post(serviceBaseUrl + url)
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .body(request)
                    .asString();
            return checkHttpResponseFormat(typeReference, response);
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

    /**
     * Prepare PUT object response.
     *
     * @param url specific url of method
     * @param request request body
     * @param typeReference reference on type for parsing into JSON
     */
    private <T> ObjectResponse<T> putObjectImpl(String url, Object request, TypeReference typeReference) throws PushServerClientException {
        try {
            HttpResponse response = Unirest.put(serviceBaseUrl + url)
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .body(request)
                    .asString();
            return checkHttpResponseFormat(typeReference, response);
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

    /**
     * Checks if format of http response is valid
     *
     * @param typeReference reference on type of response body from which map into JSON
     * @param response prepared http response
     */
    private <T> ObjectResponse<T> checkHttpResponseFormat(TypeReference typeReference, HttpResponse response) throws IOException, PushServerClientException {
        if (response.getStatus() == 200) {
            return jacksonObjectMapper.readValue(response.getRawBody(), typeReference);
        } else {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            ErrorResponse errorResponse = mapper.readValue(response.getRawBody(), ErrorResponse.class);
            throw new PushServerClientException(response.getStatusText(), errorResponse.getResponseObject());
        }
    }
}
