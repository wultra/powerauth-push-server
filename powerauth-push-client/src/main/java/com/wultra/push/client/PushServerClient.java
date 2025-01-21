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

package com.wultra.push.client;

import com.fasterxml.jackson.databind.Module;
import com.wultra.core.rest.client.base.DefaultRestClient;
import com.wultra.core.rest.client.base.RestClient;
import com.wultra.core.rest.client.base.RestClientConfiguration;
import com.wultra.core.rest.client.base.RestClientException;
import com.wultra.core.rest.model.base.entity.Error;
import com.wultra.core.rest.model.base.request.ObjectRequest;
import com.wultra.core.rest.model.base.response.ObjectResponse;
import com.wultra.core.rest.model.base.response.Response;
import com.wultra.push.model.base.PagedResponse;
import com.wultra.push.model.entity.*;
import com.wultra.push.model.enumeration.ApnsEnvironment;
import com.wultra.push.model.enumeration.MobilePlatform;
import com.wultra.push.model.enumeration.Mode;
import com.wultra.push.model.request.*;
import com.wultra.push.model.response.*;
import com.wultra.push.model.validator.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

/**
 * Simple class for interacting with the push server RESTful API.
 *
 * @author Petr Dvorak, petr@wultra.com
 * @author Martin Tupy, martin.tupy.work@gmail.com
 */
public class PushServerClient {

    private static final Logger logger = LoggerFactory.getLogger(PushServerClient.class);

    private final RestClient restClient;

    /**
     * Main constructor with the push server base URL.
     * @param serviceBaseUrl Push server instance base URL.
     * @throws PushServerClientException Thrown in case REST client initialization fails.
     */
    public PushServerClient(String serviceBaseUrl) throws PushServerClientException {
        try {
            this.restClient = DefaultRestClient.builder().baseUrl(serviceBaseUrl).build();
        } catch (RestClientException ex) {
            throw new PushServerClientException("Rest client initialization failed, error: " + ex.getMessage(), ex);
        }
    }

    /**
     * Construct the push server client with the given configuration.
     *
     * @param config REST client configuration.
     * @param modules Optional jackson modules.
     * @throws PushServerClientException Thrown in case REST client initialization fails.
     */
    public PushServerClient(final RestClientConfiguration config, final Module... modules) throws PushServerClientException {
        try {
            this.restClient = new DefaultRestClient(config, modules);
        } catch (RestClientException ex) {
            throw new PushServerClientException("Rest client initialization failed, error: " + ex.getMessage(), ex);
        }
    }

    // Client calls

    /**
     * Returns service information
     *
     * @return True if service is running.
     * @throws PushServerClientException In case of network, response / JSON processing, or other IO error.
     */
    public ObjectResponse<ServiceStatusResponse> getServiceStatus() throws PushServerClientException {

        logger.info("call={}, callType={}, action: getServiceStatus, state: initiated", "/push/service/status", "GET");
        final ObjectResponse<ServiceStatusResponse> result = getObjectImpl("/push/service/status", null, ServiceStatusResponse.class);
        logger.info("call={}, callType={}, action: getServiceStatus, state: succeeded", "/push/service/status", "GET");

        return result;
    }

    /**
     * Register anonymous device to the push server.
     *
     * @deprecated use {@link #createDevice(CreateDeviceRequest)}
     *
     * @param appId PowerAuth application app ID.
     * @param token Token received from the push service provider (APNs, FCM).
     * @param platform Mobile platform (APNs, FCM, HMS).
     * @return True if device registration was successful, false otherwise.
     * @throws PushServerClientException In case of network, response / JSON processing, or other IO error.
     */
    @Deprecated
    public boolean createDevice(String appId, String token, MobilePlatform platform) throws PushServerClientException {
        return createDevice(appId, token, platform, null);
    }

    /**
     * Register device associated with activation ID to the push server.
     *
     * @deprecated use {@link #createDevice(CreateDeviceRequest)}
     *
     * @param appId PowerAuth application app ID.
     * @param token Token received from the push service provider (APNs, FCM).
     * @param platform Mobile platform (APNs, FCM, HMS).
     * @param activationId PowerAuth activation ID.
     * @return True if device registration was successful, false otherwise.
     * @throws PushServerClientException In case of network, response / JSON processing, or other IO error.
     */
    @Deprecated
    public boolean createDevice(String appId, String token, MobilePlatform platform, String activationId) throws PushServerClientException {
        CreateDeviceRequest request = new CreateDeviceRequest();
        request.setAppId(appId);
        request.setToken(token);
        request.setPlatform(platform);
        request.setActivationId(activationId);

        // Validate request on the client side.
        String error = CreateDeviceRequestValidator.validate(request);
        if (error != null) {
            throw new PushServerClientException(error);
        }

        logger.info("call={}, callType={}, action: createDevice, state: initiated, appId: {}, token: {}, platform: {}, activationId: {}",
                "/push/device/create", "POST", appId, maskToken(token), platform, activationId);
        Response response = postObjectImpl("/push/device/create", new ObjectRequest<>(request));
        logger.info("call={}, callType={}, action: createDevice, state: succeeded", "/push/device/create", "POST");

        return response.getStatus().equals(Response.Status.OK);
    }

    /**
     * Register a device to the push server.
     *
     * @param request Create device request.
     * @return True if device registration was successful, false otherwise.
     * @throws PushServerClientException In case of network, response / JSON processing, or other IO error.
     */
    public boolean createDevice(final CreateDeviceRequest request) throws PushServerClientException {
        String error = CreateDeviceRequestValidator.validate(request);
        if (error != null) {
            throw new PushServerClientException(error);
        }

        logger.info("call={}, callType={}, action: createDevice, state: initiated, appId: {}, token: {}, platform: {}, environment: {}",
                "/push/device/create", "POST", request.getAppId(), maskToken(request.getToken()), request.getPlatform(), request.getEnvironment());
        Response response = postObjectImpl("/push/device/create", new ObjectRequest<>(request));
        logger.info("call={}, callType={}, action: createDevice, state: succeeded", "/push/device/create", "POST");

        return response.getStatus().equals(Response.Status.OK);
    }

    /**
     * Register device associated with multiple activation IDs to the push server.
     *
     * @deprecated use {@link #createDeviceForActivations(CreateDeviceForActivationsRequest)}
     *
     * @param appId PowerAuth application app ID.
     * @param token Token received from the push service provider (APNs, FCM).
     * @param platform Mobile platform (APNs, FCM, HMS).
     * @param activationIds PowerAuth activation IDs.
     * @return True if device registration was successful, false otherwise.
     * @throws PushServerClientException In case of network, response / JSON processing, or other IO error.
     */
    @Deprecated
    public boolean createDeviceForActivations(String appId, String token, MobilePlatform platform, List<String> activationIds) throws PushServerClientException {
        CreateDeviceForActivationsRequest request = new CreateDeviceForActivationsRequest();
        request.setAppId(appId);
        request.setToken(token);
        request.setPlatform(platform);
        request.getActivationIds().addAll(activationIds);

        // Validate request on the client side.
        String error = CreateDeviceRequestValidator.validate(request);
        if (error != null) {
            throw new PushServerClientException(error);
        }

        logger.info("call={}, callType={}, action: createDeviceForActivations, state: initiated, appId: {}, token: {}, platform: {}, activationIds: {}",
                "/push/device/create/multi", "POST", request.getAppId(), maskToken(request.getToken()), request.getPlatform(), request.getActivationIds());
        Response response = postObjectImpl("/push/device/create/multi", new ObjectRequest<>(request));
        logger.info("call={}, callType={}, action: createDeviceForActivations, state: succeeded", "/push/device/create/multi", "POST");

        return response.getStatus().equals(Response.Status.OK);
    }

    /**
     * Register device associated with multiple activation IDs to the push server.
     *
     * @param request Create device for activations request.
     * @return True if device registration was successful, false otherwise.
     * @throws PushServerClientException In case of network, response / JSON processing, or other IO error.
     */
    public boolean createDeviceForActivations(final CreateDeviceForActivationsRequest request) throws PushServerClientException {
        // Validate request on the client side.
        String error = CreateDeviceRequestValidator.validate(request);
        if (error != null) {
            throw new PushServerClientException(error);
        }

        logger.info("call={}, callType={}, action: createDeviceForActivations, state: initiated, appId: {}, token: {}, platform: {}, environment: {}, activationIds: {}",
                "/push/device/create/multi", "POST", request.getAppId(), maskToken(request.getToken()), request.getPlatform(), request.getEnvironment(), request.getActivationIds());
        Response response = postObjectImpl("/push/device/create/multi", new ObjectRequest<>(request));
        logger.info("call={}, callType={}, action: createDeviceForActivations, state: succeeded", "/push/device/create/multi", "POST");

        return response.getStatus().equals(Response.Status.OK);
    }

    /**
     * Remove device from the push server
     *
     * @param appId PowerAuth application app ID.
     * @param token Token received from the push service provider.
     * @return True if device removal was successful, false otherwise.
     * @throws PushServerClientException In case of network, response / JSON processing, or other IO error.
     */
    public boolean deleteDevice(String appId, String token) throws PushServerClientException {
        DeleteDeviceRequest request = new DeleteDeviceRequest();
        request.setAppId(appId);
        request.setToken(token);

        // Validate request on the client side.
        String error = DeleteDeviceRequestValidator.validate(request);
        if (error != null) {
            throw new PushServerClientException(error);
        }

        logger.info("call={}, callType={}, action: deleteDevice, state: initiated, appId: {}, token: {}",
                "/push/device/delete", "POST", appId, maskToken(token));
        Response response = postObjectImpl("/push/device/delete", new ObjectRequest<>(request));
        logger.info("call={}, callType={}, action: deleteDevice, state: succeeded", "/push/device/delete", "POST");

        return response.getStatus().equals(Response.Status.OK);
    }

    /**
     * Update activation status for given device registration.
     *
     * @param activationId Identifier of activation
     * @return True if updating went successful, false otherwise.
     * @throws PushServerClientException In case of network, response / JSON processing, or other IO error.
     */
    public boolean updateDeviceStatus(String activationId) throws PushServerClientException {
        UpdateDeviceStatusRequest request = new UpdateDeviceStatusRequest();
        request.setActivationId(activationId);

        // Validate request on the client side.
        String error = UpdateDeviceStatusRequestValidator.validate(request);
        if (error != null) {
            throw new PushServerClientException(error);
        }

        logger.info("call={}, callType={}, action: updateDeviceStatus, state: initiated, activationId: {}",
                "/push/device/status/update", "POST", activationId);
        // Note that there is just plain 'request' in the request, not 'new ObjectRequest<>(request)'.
        // This is due to the fact that standard PowerAuth Server callback format is used here.
        final Response response = postImpl("/push/device/status/update", request, new ParameterizedTypeReference<>(){});
        logger.info("call={}, callType={}, action: updateDeviceStatus, state: succeeded", "/push/device/status/update", "POST");

        return response.getStatus().equals(Response.Status.OK);
    }

    /**
     * Send a single push message to application with given ID.
     *
     * @param appId PowerAuth application app ID.
     * @param pushMessage Push message to be sent.
     * @return SendMessageResponse in case everything went OK, ErrorResponse in case of an error.
     * @throws PushServerClientException In case of network, response / JSON processing, or other IO error.
     */
    public ObjectResponse<PushMessageSendResult> sendPushMessage(String appId, PushMessage pushMessage) throws PushServerClientException {
        return sendPushMessage(appId, Mode.SYNCHRONOUS, pushMessage);
    }

    /**
     * Send a single push message to application with given ID.
     *
     * @param appId PowerAuth application app ID.
     * @param mode Mode of push notification sending.
     * @param pushMessage Push message to be sent.
     * @return SendMessageResponse in case everything went OK, ErrorResponse in case of an error.
     * @throws PushServerClientException In case of network, response / JSON processing, or other IO error.
     */
    public ObjectResponse<PushMessageSendResult> sendPushMessage(String appId, Mode mode, PushMessage pushMessage) throws PushServerClientException {
        final SendPushMessageRequest request = new SendPushMessageRequest();
        request.setMode(mode);
        request.setAppId(appId);
        request.setMessage(pushMessage);

        // Validate request on the client side.
        final String error = SendPushMessageRequestValidator.validate(request);
        if (error != null) {
            throw new PushServerClientException(error);
        }

        logger.info("call={}, callType={}, action: sendPushMessage, state: initiated, appId: {}, userId: {}",
                "/push/message/send", "POST", appId, pushMessage.getUserId());
        final ObjectResponse<PushMessageSendResult> result = postObjectImpl("/push/message/send", new ObjectRequest<>(request), PushMessageSendResult.class);
        logger.info("call={}, callType={}, action: sendPushMessage, state: succeeded", "/push/message/send", "POST");

        return result;
    }

    /**
     * Send a push message batch to application with given ID.
     *
     * @param appId PowerAuth application app ID.
     * @param batch Push message batch to be sent.
     * @return SendMessageResponse in case everything went OK, ErrorResponse in case of an error.
     * @throws PushServerClientException In case of network, response / JSON processing, or other IO error.
     */
    public ObjectResponse<PushMessageSendResult> sendPushMessageBatch(String appId, List<PushMessage> batch) throws PushServerClientException {
        return sendPushMessageBatch(appId, Mode.SYNCHRONOUS, batch);
    }

    /**
     * Send a push message batch to application with given ID.
     *
     * @param appId PowerAuth application app ID.
     * @param mode Mode of push notification sending.
     * @param batch Push message batch to be sent.
     * @return SendMessageResponse in case everything went OK, ErrorResponse in case of an error.
     * @throws PushServerClientException In case of network, response / JSON processing, or other IO error.
     */
    public ObjectResponse<PushMessageSendResult> sendPushMessageBatch(String appId, Mode mode, List<PushMessage> batch) throws PushServerClientException {
        final SendPushMessageBatchRequest request = new SendPushMessageBatchRequest();
        request.setAppId(appId);
        request.setMode(mode);
        request.setBatch(batch);

        // Validate request on the client side.
        final String error = SendPushMessageBatchRequestValidator.validate(request);
        if (error != null) {
            throw new PushServerClientException(error);
        }

        logger.info("call={}, callType={}, action: sendPushMessageBatch, state: initiated, appId: {}, batchSize: {}",
                "/push/message/batch/send", "POST", appId, batch.size());
        final ObjectResponse<PushMessageSendResult> result = postObjectImpl("/push/message/batch/send", new ObjectRequest<>(request), PushMessageSendResult.class);
        logger.info("call={}, callType={}, action: sendPushMessageBatch, state: succeeded", "/push/message/batch/send", "POST");

        return result;
    }

    /**
     * Create a campaign.
     *
     * @param appId Application ID.
     * @param message Message which attributes are defined in PushMessageBody.
     * @return ID of new created campaign.
     * @throws PushServerClientException In case of network, response / JSON processing, or other IO error.
     */
    public ObjectResponse<CreateCampaignResponse> createCampaign(String appId, PushMessageBody message) throws PushServerClientException {
        CreateCampaignRequest request = new CreateCampaignRequest();
        request.setAppId(appId);
        request.setMessage(message);

        // Validate request on the client side.
        String error = CreateCampaignRequestValidator.validate(request);
        if (error != null) {
            throw new PushServerClientException(error);
        }

        logger.info("call={}, callType={}, action: createCampaign, state: initiated, appId: {}",
                "/push/campaign/create", "POST", appId);
        final ObjectResponse<CreateCampaignResponse> result = postObjectImpl("/push/campaign/create", new ObjectRequest<>(request), CreateCampaignResponse.class);
        logger.info("call={}, callType={}, action: createCampaign, state: succeeded", "/push/campaign/create", "POST");


        return result;
    }

    /**
     * Delete a campaign specified with campaignId.
     *
     * @param campaignId Campaign ID.
     * @return True if campaign is removed, false otherwise.
     * @throws PushServerClientException In case of network, response / JSON processing, or other IO error.
     */
    public boolean deleteCampaign(Long campaignId) throws PushServerClientException {
        final String campaignIdSanitized = URLEncoder.encode(String.valueOf(campaignId), StandardCharsets.UTF_8);

        logger.info("call={}, callType={}, action: deleteCampaign, state: initiated, campaignId: {}",
                "/push/campaign/{campaignId}/delete", "POST", campaignId);
        final ObjectResponse<DeleteCampaignResponse> response = postObjectImpl("/push/campaign/" + campaignIdSanitized + "/delete", null, DeleteCampaignResponse.class);
        logger.info("call={}, callType={}, action: deleteCampaign, state: succeeded", "/push/campaign/{campaignId}/delete", "POST");

        return response.getStatus().equals(Response.Status.OK);
    }

    /**
     * Get list of campaigns, dependent on all param
     *
     * @param all true to get whole list, false to get campaigns that are only sent
     * @return List of campaigns.
     * @throws PushServerClientException In case of network, response / JSON processing, or other IO error.
     */
    public ObjectResponse<ListOfCampaignsResponse> getListOfCampaigns(boolean all) throws PushServerClientException {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("all", Collections.singletonList(Boolean.valueOf(all).toString()));

        logger.info("call={}, callType={}, action: getListOfCampaigns, state: initiated",
                "/push/campaign/list", "GET");
        final ObjectResponse<ListOfCampaignsResponse> result = getObjectImpl("/push/campaign/list", params, ListOfCampaignsResponse.class);
        logger.info("call={}, callType={}, action: getListOfCampaigns, state: succeeded", "/push/campaign/list", "GET");

        return result;
    }

    /**
     * Get a campaign specified with campaignID.
     *
     * @param campaignId ID of campaign to get.
     * @return Details of campaign, defined in CampaignResponse
     * @throws PushServerClientException In case of network, response / JSON processing, or other IO error.
     */
    public ObjectResponse<CampaignResponse> getCampaign(Long campaignId) throws PushServerClientException {
        final String campaignIdSanitized = URLEncoder.encode(String.valueOf(campaignId), StandardCharsets.UTF_8);

        logger.info("call={}, callType={}, action: getCampaign, state: initiated, campaignId: {}", "/push/campaign/{campaignId}/detail", "GET", campaignId);
        final ObjectResponse<CampaignResponse> result = getObjectImpl("/push/campaign/" + campaignIdSanitized + "/detail", null, CampaignResponse.class);
        logger.info("call={}, callType={}, action: getCampaign, state: succeeded", "/push/campaign/{campaignId}/detail", "GET");

        return result;
    }

    /**
     * Add a list of users to a specific campaign
     *
     * @param campaignId Identifier of campaign.
     * @param users List of users to add.
     * @return True if adding was successful, false otherwise.
     * @throws PushServerClientException In case of network, response / JSON processing, or other IO error.
     */
    public boolean addUsersToCampaign(Long campaignId, List<String> users) throws PushServerClientException {
        final ListOfUsers listOfUsers = new ListOfUsers(users);
        final String campaignIdSanitized = URLEncoder.encode(String.valueOf(campaignId), StandardCharsets.UTF_8);

        logger.info("call={}, callType={}, action: addUsersToCampaign, state: initiated, campaignId: {}, userCount: {}", "/push/campaign/{campaignId}/user/add", "PUT", campaignId, users.size());
        final Response response = putObjectImpl("/push/campaign/" + campaignIdSanitized + "/user/add", new ObjectRequest<>(listOfUsers));
        logger.info("call={}, callType={}, action: addUsersToCampaign, state: succeeded", "/push/campaign/{campaignId}/user/add", "PUT");

        if (response == null) {
            throw new PushServerClientException(new Error("PUSH_SERVER_CLIENT_ERROR", "Network communication has failed."));
        }

        return response.getStatus().equals(Response.Status.OK);
    }

    /**
     * Get a list of users in paged format from specific campaign
     *
     * @param campaignId Identifier of campaign.
     * @param page Page number.
     * @param size Size of elements per page.
     * @return Page of users specified with params.
     * @throws PushServerClientException In case of network, response / JSON processing, or other IO error.
     */
    public PagedResponse<ListOfUsersFromCampaignResponse> getListOfUsersFromCampaign(Long campaignId, int page, int size) throws PushServerClientException {
        final String campaignIdSanitized = URLEncoder.encode(String.valueOf(campaignId), StandardCharsets.UTF_8);
        final MultiValueMap<String, String> params = buildPages(page, size);

        final ParameterizedTypeReference<PagedResponse<ListOfUsersFromCampaignResponse>> typeReference = new ParameterizedTypeReference<>() {};
        logger.info("call={}, callType={}, action: getListOfUsersFromCampaign, state: initiated, campaignId: {}, page: {}, size: {}", "/push/campaign/{campaignId}/user/list", "GET", campaignId, page, size);
        final PagedResponse<ListOfUsersFromCampaignResponse> result = getImpl("/push/campaign/" + campaignIdSanitized + "/user/list", params, typeReference);
        logger.info("call={}, callType={}, action: getListOfUsersFromCampaign, state: succeeded", "/push/campaign/{campaignId}/user/list", "GET");

        return result;
    }

    /**
     * Delete a list of users from specific campaign.
     *
     * @param campaignId Identifier of campaign.
     * @param users List of users' Identifiers to delete.
     * @return True if deletion was successful, false otherwise.
     * @throws PushServerClientException In case of network, response / JSON processing, or other IO error.
     */
    public boolean deleteUsersFromCampaign(Long campaignId, List<String> users) throws PushServerClientException {
        final ListOfUsers listOfUsers = new ListOfUsers(users);
        final String campaignIdSanitized = URLEncoder.encode(String.valueOf(campaignId), StandardCharsets.UTF_8);

        logger.info("call={}, callType={}, action: deleteUsersFromCampaign, state: initiated, campaignId: {}, userCount: {}", "/push/campaign/{campaignId}/user/delete", "POST", campaignId, users.size());
        final Response response = postObjectImpl("/push/campaign/" + campaignIdSanitized + "/user/delete", new ObjectRequest<>(listOfUsers));
        logger.info("call={}, callType={}, action: deleteUsersFromCampaign, state: succeeded", "/push/campaign/{campaignId}/user/delete", "POST");

        return response.getStatus().equals(Response.Status.OK);
    }

    /**
     * Send a campaign on test user for trying its correctness.
     *
     * @param campaignId Identifier of campaign.
     * @param userId Identifier of test user.
     * @return True if sent, else otherwise.
     * @throws PushServerClientException In case of network, response / JSON processing, or other IO error.
     */
    public boolean sendTestCampaign(Long campaignId, String userId) throws PushServerClientException {
        final String campaignIdSanitized = URLEncoder.encode(String.valueOf(campaignId), StandardCharsets.UTF_8);
        final TestCampaignRequest request = new TestCampaignRequest();
        request.setUserId(userId);

        // Validate request on the client side.
        final String error = TestCampaignRequestValidator.validate(request);
        if (error != null) {
            throw new PushServerClientException(error);
        }

        logger.info("call={}, callType={}, action: sendTestCampaign, state: initiated, campaignId: {}, userId: {}", "/push/campaign/send/test/{campaignId}", "POST", campaignId, userId);
        final Response response = postObjectImpl("/push/campaign/send/test/" + campaignIdSanitized, new ObjectRequest<>(request));
        logger.info("call={}, callType={}, action: sendTestCampaign, state: succeeded", "/push/campaign/send/test/{campaignId}", "POST");

        return response.getStatus().equals(Response.Status.OK);
    }

    /**
     * Send a specific campaign to users carrying this campaignID in PushCampaignUser model, but only once per device identified by token.
     *
     * @param campaignId Identifier of campaign.
     * @return True if sent, else otherwise.
     * @throws PushServerClientException In case of network, response / JSON processing, or other IO error.
     */
    public boolean sendCampaign(Long campaignId) throws PushServerClientException {
        final String campaignIdSanitized = URLEncoder.encode(String.valueOf(campaignId), StandardCharsets.UTF_8);

        logger.info("call={}, callType={}, action: sendCampaign, state: initiated, campaignId: {}", "/push/campaign/send/live/{campaignId}", "POST", campaignId);
        final Response response = postObjectImpl("/push/campaign/send/live/" + campaignIdSanitized, null);
        logger.info("call={}, callType={}, action: sendCampaign, state: succeeded", "/push/campaign/send/live/{campaignId}", "POST");

        return response.getStatus().equals(Response.Status.OK);
    }

    /**
     * Get list of application credentials entities.
     * @return Application credentials entity list.
     * @throws PushServerClientException Thrown when communication with Push Server fails.
     */
    public ObjectResponse<GetApplicationListResponse> getApplicationList() throws PushServerClientException {
        logger.info("call={}, callType={}, action: getApplicationList, state: initiated", "/admin/app/list", "GET");
        final ObjectResponse<GetApplicationListResponse> response = getObjectImpl("/admin/app/list", null, GetApplicationListResponse.class);
        logger.info("call={}, callType={}, action: getApplicationList, state: succeeded", "/admin/app/list", "GET");
        return response;
    }

    /**
     * Get list of applications which are not yet configured in Push Server but exist in PowerAuth server.
     * @return List of applications which are not configured.
     * @throws PushServerClientException Thrown when communication with Push Server fails.
     */
    public ObjectResponse<GetApplicationListResponse> getUnconfiguredApplicationList() throws PushServerClientException {
        logger.info("call={}, callType={}, action: getUnconfiguredApplicationList, state: initiated", "/admin/app/unconfigured/list", "GET");
        final ObjectResponse<GetApplicationListResponse> response = getObjectImpl("/admin/app/unconfigured/list", null, GetApplicationListResponse.class);
        logger.info("call={}, callType={}, action: getUnconfiguredApplicationList, state: succeeded", "/admin/app/unconfigured/list", "GET");
        return response;
    }

    /**
     * Get detail for an application credentials entity.
     * @param request Application detail request.
     * @return Application credentials entity detail.
     * @throws PushServerClientException Thrown when communication with Push Server fails.
     */
    public ObjectResponse<GetApplicationDetailResponse> getApplicationDetail(final GetApplicationDetailRequest request) throws PushServerClientException {
        logger.info("call={}, callType={}, action: getApplicationDetail, state: initiated, appId: {}", "/admin/app/detail", "POST", request.getAppId());
        final ObjectResponse<GetApplicationDetailResponse> response = postObjectImpl("/admin/app/detail", new ObjectRequest<>(request), GetApplicationDetailResponse.class);
        logger.info("call={}, callType={}, action: getApplicationDetail, state: succeeded", "/admin/app/detail", "POST");
        return response;
    }

    /**
     * Create application credentials entity.
     * @param appId PowerAuth application ID.
     * @return Response with ID of created application credentials entity.
     * @throws PushServerClientException Thrown when communication with Push Server fails.
     */
    public ObjectResponse<CreateApplicationResponse> createApplication(String appId) throws PushServerClientException {
        final CreateApplicationRequest request = new CreateApplicationRequest(appId);
        logger.info("call={}, callType={}, action: createApplication, state: initiated, appId: {}", "/admin/app/create", "POST", appId);
        final ObjectResponse<CreateApplicationResponse> response = postObjectImpl("/admin/app/create", new ObjectRequest<>(request), CreateApplicationResponse.class);
        logger.info("call={}, callType={}, action: createApplication, state: succeeded", "/admin/app/create", "POST");
        return response;
    }

    /**
     * Update iOS details for an application credentials entity.
     *
     * @deprecated use {@link #updateApns(UpdateApnsRequest)}
     *
     * @param appId ID of application credentials entity.
     * @param bundle The iOS bundle record.
     * @param keyId The APNs key ID record.
     * @param teamId The Team ID record.
     * @param environment The APNs environment.
     * @param privateKey The APNs private key bytes.
     * @return Response from server.
     * @throws PushServerClientException Thrown when communication with Push Server fails.
     */
    @Deprecated
    public Response updateIos(String appId, String bundle, String keyId, String teamId, ApnsEnvironment environment, byte[] privateKey) throws PushServerClientException {
        final String privateKeyBase64 = Base64.getEncoder().encodeToString(privateKey);
        final UpdateIosRequest request = new UpdateIosRequest(appId, bundle, keyId, teamId, environment, privateKeyBase64);
        logger.info("call={}, callType={}, action: updateIos, state: initiated, appId: {}", "/admin/app/ios/update", "PUT", appId);
        final Response response = putObjectImpl("/admin/app/ios/update", new ObjectRequest<>(request));
        logger.info("call={}, callType={}, action: updateIos, state: succeeded", "/admin/app/ios/update", "PUT");
        return response;
    }

    /**
     * Update APNs details for an application credentials entity.
     *
     * @param updateRequest Update APNS request.
     * @return Response from server.
     * @throws PushServerClientException Thrown when communication with Push Server fails.
     */
    public Response updateApns(final UpdateApnsRequest updateRequest) throws PushServerClientException {
        logger.info("call={}, callType={}, action: updateApns, state: initiated, appId: {}", "/admin/app/apns", "PUT", updateRequest.getAppId());
        final Response response = putObjectImpl("/admin/app/apns", new ObjectRequest<>(updateRequest));
        logger.info("call={}, callType={}, action: updateApns, state: succeeded", "/admin/app/apns", "PUT");
        return response;
    }

    /**
     * Remove iOS record from an application credentials entity.
     *
     * @deprecated use {@link #removeApns(String)}
     *
     * @param appId Application credentials entity ID.
     * @return Response from server.
     * @throws PushServerClientException Thrown when communication with Push Server fails.
     */
    @Deprecated
    public Response removeIos(String appId) throws PushServerClientException {
        final RemoveIosRequest request = new RemoveIosRequest(appId);
        logger.info("call={}, callType={}, action: removeIos, state: initiated, appId: {}", "/admin/app/ios/remove", "POST", appId);
        final Response response = postObjectImpl("/admin/app/ios/remove", new ObjectRequest<>(request));
        logger.info("call={}, callType={}, action: removeIos, state: succeeded", "/admin/app/ios/remove", "POST");
        return response;
    }

    /**
     * Remove APNs record from an application credentials entity.
     *
     * @param appId Application credentials entity ID.
     * @return Response from server.
     * @throws PushServerClientException Thrown when communication with Push Server fails.
     */
    public Response removeApns(String appId) throws PushServerClientException {
        logger.info("call={}, callType={}, action: removeApns, state: initiated, appId: {}", "/admin/app/apns", "DELETE", appId);
        final Response response = deleteImpl("/admin/app/apns?appId=" + appId);
        logger.info("call={}, callType={}, action: removeApns, state: succeeded", "/admin/app/apns", "DELETE");
        return response;
    }

    /**
     * Update Android details for an application credentials entity.
     *
     * @deprecated use {@link #updateFcm(UpdateFcmRequest)}
     *
     * @param appId Application credentials entity ID.
     * @param projectId The Android project ID record.
     * @param privateKey The Android private key bytes.
     * @return Response from server.
     * @throws PushServerClientException Thrown when communication with Push Server fails.
     */
    @Deprecated
    public Response updateAndroid(String appId, String projectId, byte[] privateKey) throws PushServerClientException {
        final String privateKeyBase64 = Base64.getEncoder().encodeToString(privateKey);
        final UpdateAndroidRequest request = new UpdateAndroidRequest(appId, projectId, privateKeyBase64);
        logger.info("call={}, callType={}, action: updateAndroid, state: initiated, appId: {}", "/admin/app/android/update", "PUT", appId);
        final Response response = putObjectImpl("/admin/app/android/update", new ObjectRequest<>(request));
        logger.info("call={}, callType={}, action: updateAndroid, state: succeeded", "/admin/app/android/update", "PUT");
        return response;
    }

    /**
     * Update FCM details for an application credentials entity.
     *
     * @param updateRequest Update FCM request.
     * @return Response from server.
     * @throws PushServerClientException Thrown when communication with Push Server fails.
     */
    public Response updateFcm(final UpdateFcmRequest updateRequest) throws PushServerClientException {
        logger.info("call={}, callType={}, action: updateFcm, state: initiated, appId: {}", "/admin/app/fcm", "PUT", updateRequest.getAppId());
        final Response response = putObjectImpl("/admin/app/fcm", new ObjectRequest<>(updateRequest));
        logger.info("call={}, callType={}, action: updateFcm, state: succeeded", "/admin/app/fcm", "PUT");
        return response;
    }

    /**
     * Remove Android record from an application credentials entity.
     *
     * @deprecated use {@link #removeFcm(String)}
     *
     * @param appId Application credentials entity ID.
     * @return Response from server.
     * @throws PushServerClientException Thrown when communication with Push Server fails.
     */
    @Deprecated
    public Response removeAndroid(String appId) throws PushServerClientException {
        final RemoveAndroidRequest request = new RemoveAndroidRequest(appId);
        logger.info("call={}, callType={}, action: removeAndroid, state: initiated, appId: {}", "/admin/app/android/remove", "POST", appId);
        final Response response = postObjectImpl("/admin/app/android/remove", new ObjectRequest<>(request));
        logger.info("call={}, callType={}, action: removeAndroid, state: succeeded", "/admin/app/android/remove", "POST");
        return response;
    }

    /**
     * Remove FCM record from an application credentials entity.
     *
     * @param appId Application credentials entity ID.
     * @return Response from server.
     * @throws PushServerClientException Thrown when communication with Push Server fails.
     */
    public Response removeFcm(String appId) throws PushServerClientException {
        logger.info("call={}, callType={}, action: removeFcm, state: initiated, appId: {}", "/admin/app/fcm", "DELETE", appId);
        final Response response = deleteImpl("/admin/app/fcm?appId=" + appId);
        logger.info("call={}, callType={}, action: removeFcm, state: succeeded", "/admin/app/fcm", "DELETE");
        return response;
    }

    /**
     * Update Huawei details for an application credentials entity.
     *
     * @deprecated use {@link #updateHms(UpdateHmsRequest)}
     *
     * @param request Update Huawei request.
     * @return Response from server.
     * @throws PushServerClientException Thrown when communication with Push Server fails.
     */
    @Deprecated
    public Response updateHuawei(final UpdateHuaweiRequest request) throws PushServerClientException {
        logger.info("call={}, callType={}, action: updateHuawei, state: initiated, appId: {}", "/admin/app/huawei/update", "PUT", request.getAppId());
        final Response response = putObjectImpl("/admin/app/huawei/update", new ObjectRequest<>(request));
        logger.info("call={}, callType={}, action: updateHuawei, state: succeeded", "/admin/app/huawei/update", "PUT");
        return response;
    }

    /**
     * Update HMS details for an application credentials entity.
     *
     * @param request Update HMS request.
     * @return Response from server.
     * @throws PushServerClientException Thrown when communication with Push Server fails.
     */
    public Response updateHms(final UpdateHmsRequest request) throws PushServerClientException {
        logger.info("call={}, callType={}, action: updateHms, state: initiated, appId: {}", "/admin/app/hms", "PUT", request.getAppId());
        final Response response = putObjectImpl("/admin/app/hms", new ObjectRequest<>(request));
        logger.info("call={}, callType={}, action: updateHms, state: succeeded", "/admin/app/hms", "PUT");
        return response;
    }

    /**
     * Remove Huawei record from an application credentials entity.
     *
     * @deprecated use {@link #removeHms(String)}
     *
     * @param appId Application credentials entity ID.
     * @return Response from server.
     * @throws PushServerClientException Thrown when communication with Push Server fails.
     */
    @Deprecated
    public Response removeHuawei(String appId) throws PushServerClientException {
        final RemoveHuaweiRequest request = new RemoveHuaweiRequest(appId);
        logger.info("call={}, callType={}, action: removeHuawei, state: initiated, appId: {}", "/admin/app/huawei/remove", "POST", appId);
        final Response response = postObjectImpl("/admin/app/huawei/remove", new ObjectRequest<>(request));
        logger.info("call={}, callType={}, action: removeHuawei, state: succeeded", "/admin/app/huawei/remove", "POST");
        return response;
    }

    /**
     * Remove HMS record from an application credentials entity.
     *
     * @param appId Application credentials entity ID.
     * @return Response from server.
     * @throws PushServerClientException Thrown when communication with Push Server fails.
     */
    public Response removeHms(String appId) throws PushServerClientException {
        logger.info("call={}, callType={}, action: removeHms, state: initiated, appId: {}", "/admin/app/hms", "DELETE", appId);
        final Response response = deleteImpl("/admin/app/hms?appId=" + appId);
        logger.info("call={}, callType={}, action: removeHms, state: succeeded", "/admin/app/hms", "DELETE");
        return response;
    }

    /**
     * Post a message to an inbox of provided user.
     * @param request Request with the message detail.
     * @return Response with a newly created message.
     * @throws PushServerClientException Thrown when communication with Push Server fails.
     */
    public ObjectResponse<GetInboxMessageDetailResponse> postMessage(CreateInboxMessageRequest request) throws PushServerClientException {
        logger.info("call={}, callType={}, action: postMessage, state: initiated, userId: {}, subject: {}", "/inbox/messages", "POST", request.getUserId(), request.getSubject());
        final ObjectResponse<GetInboxMessageDetailResponse> response = postObjectImpl("/inbox/messages", new ObjectRequest<>(request), GetInboxMessageDetailResponse.class);
        logger.info("call={}, callType={}, action: postMessage, state: succeeded", "/inbox/messages", "POST");
        return response;
    }

    /**
     * Fetch the list of messages for a given user.
     * @param userId User ID.
     * @param applications List of application IDs.
     * @param onlyUnread Indication if only unread messages should be returneed.
     * @param page Page index.
     * @param size Page size.
     * @return List of inbox messages.
     * @throws PushServerClientException Thrown when communication with Push Server fails.
     */
    public PagedResponse<ListOfInboxMessages> fetchMessageListForUser(String userId, List<String> applications, boolean onlyUnread, Integer page, Integer size) throws PushServerClientException {
        final MultiValueMap<String, String> params = buildPages(page, size);
        params.add("userId", userId);
        params.add("applications", String.join(",", applications));
        params.add("onlyUnread", Boolean.toString(onlyUnread));

        final ParameterizedTypeReference<PagedResponse<ListOfInboxMessages>> typeReference = new ParameterizedTypeReference<>() {};
        logger.info("call={}, callType={}, action: fetchMessageListForUser, state: initiated, userId: {}, onlyUnread: {}", "/inbox/messages/list", "GET", userId, onlyUnread);
        final PagedResponse<ListOfInboxMessages> result = getImpl("/inbox/messages/list", params, typeReference);
        logger.info("call={}, callType={}, action: fetchMessageListForUser, state: succeeded", "/inbox/messages/list", "GET");

        return result;
    }

    /**
     * Fetch unread message count for a user with given ID.
     * @param userId User ID.
     * @param appId Application ID.
     * @return Count of unread messages.
     * @throws PushServerClientException Thrown when communication with Push Server fails.
     */
    public ObjectResponse<GetInboxMessageCountResponse> fetchMessageCountForUser(String userId, String appId) throws PushServerClientException {
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("userId", userId);
        params.add("appId", appId);

        final ParameterizedTypeReference<ObjectResponse<GetInboxMessageCountResponse>> typeReference = new ParameterizedTypeReference<>() {};
        logger.info("call={}, callType={}, action: fetchMessageCountForUser, state: initiated, userId: {}, appId: {}", "/inbox/messages/count", "GET", userId, appId);
        final ObjectResponse<GetInboxMessageCountResponse> result = getImpl("/inbox/messages/count", params, typeReference);
        logger.info("call={}, callType={}, action: fetchMessageCountForUser, state: succeeded", "/inbox/messages/count", "GET");

        return result;
    }

    /**
     * Read all unread messages in inbox of provided user.
     * @param userId User ID.
     * @param appId Application ID.
     * @return Response.
     * @throws PushServerClientException Thrown when communication with Push Server fails.
     */
    public Response readAllMessages(String userId, String appId) throws PushServerClientException {
        final ReadAllInboxMessagesRequest request = new ReadAllInboxMessagesRequest();
        request.setUserId(userId);
        request.setAppId(appId);
        logger.info("call={}, callType={}, action: readAllMessages, state: initiated, userId: {}, appId: {}", "/inbox/messages/read-all", "POST", userId, appId);
        final Response response = postObjectImpl("/inbox/messages/read-all", new ObjectRequest<>(request));
        logger.info("call={}, callType={}, action: readAllMessages, state: succeeded", "/inbox/messages/read-all", "POST");
        return response;
    }

    /**
     * Fetch detail of the message for given message ID.
     * @param inboxId Inbox message ID.
     * @return Detail of a message with given ID.
     * @throws PushServerClientException Thrown when communication with Push Server fails.
     */
    public ObjectResponse<GetInboxMessageDetailResponse> fetchMessageDetail(String inboxId) throws PushServerClientException {
        final MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("id", inboxId);

        logger.info("call={}, callType={}, action: fetchMessageDetail, state: initiated, inboxId: {}", "/inbox/messages/detail", "GET", inboxId);
        final ObjectResponse<GetInboxMessageDetailResponse> result = getObjectImpl("/inbox/messages/detail", params, GetInboxMessageDetailResponse.class);
        logger.info("call={}, callType={}, action: fetchMessageDetail, state: succeeded", "/inbox/messages/detail", "GET");

        return result;
    }

    /**
     * Read message with given ID in inbox of provided user.
     * @param inboxId Inbox message ID.
     * @return Detail of the read message.
     * @throws PushServerClientException Thrown when communication with Push Server fails.
     */
    public ObjectResponse<GetInboxMessageDetailResponse> readMessage(String inboxId) throws PushServerClientException {
        final ReadInboxMessageRequest request = new ReadInboxMessageRequest();
        request.setInboxId(inboxId);
        logger.info("call={}, callType={}, action: readMessage, state: initiated, inboxId: {}", "/inbox/messages/read", "POST", inboxId);
        final ObjectResponse<GetInboxMessageDetailResponse> response = postObjectImpl("/inbox/messages/read", new ObjectRequest<>(request), GetInboxMessageDetailResponse.class);
        logger.info("call={}, callType={}, action: readMessage, state: succeeded", "/inbox/messages/read", "POST");
        return response;
    }

    // Generic HTTP client methods

    /**
     * Prepare GET response.
     *
     * @param url specific url of method.
     * @param params params to pass to url path, optional.
     * @param typeReference response type reference.
     * @return Object obtained after processing the response JSON.
     * @throws PushServerClientException In case of network, response / JSON processing, or other IO error.
     *
     */
    private <T> T getImpl(String url, MultiValueMap<String, String> params, ParameterizedTypeReference<T> typeReference) throws PushServerClientException {
        try {
            return restClient.get(url, params, null, typeReference).getBody();
        } catch (RestClientException ex) {
            logger.debug(ex.getMessage(), ex);
            throw new PushServerClientException(ex, new Error("PUSH_SERVER_CLIENT_ERROR", "HTTP GET request failed."));
        }
    }

    /**
     * Prepare GET object response.
     *
     * @param url specific url of method.
     * @param params params to pass to url path, optional.
     * @param responseType response type.
     * @return Object obtained after processing the response JSON.
     * @throws PushServerClientException In case of network, response / JSON processing, or other IO error.
     *
     */
    private <T> ObjectResponse<T> getObjectImpl(String url, MultiValueMap<String, String> params, Class<T> responseType) throws PushServerClientException {
        try {
            return restClient.getObject(url, params, null, responseType);
        } catch (RestClientException ex) {
            logger.debug(ex.getMessage(), ex);
            throw new PushServerClientException(ex, new Error("PUSH_SERVER_CLIENT_ERROR", "HTTP GET request failed."));
        }
    }

    /**
     * Prepare a generic POST response.
     *
     * @param url specific url of method
     * @param request request body
     * @param typeReference type reference
     * @return Object obtained after processing the response JSON.
     * @throws PushServerClientException In case of network, response / JSON processing, or other IO error.
     */
    private <T> T postImpl(String url, Object request, ParameterizedTypeReference<T> typeReference) throws PushServerClientException {
        try {
            return restClient.post(url, request, typeReference).getBody();
        } catch (RestClientException ex) {
            logger.debug(ex.getMessage(), ex);
            throw new PushServerClientException(ex, new Error("PUSH_SERVER_CLIENT_ERROR", "HTTP POST request failed."));
        }
    }

    /**
     * Prepare POST object response. Uses default {@link Response} type reference for response.
     *
     * @param url specific url of method
     * @param request request body
     * @return Object obtained after processing the response JSON.
     * @throws PushServerClientException In case of network, response / JSON processing, or other IO error.
     */
    private Response postObjectImpl(String url, ObjectRequest<?> request) throws PushServerClientException {
        try {
            return restClient.postObject(url, request);
        } catch (RestClientException ex) {
            logger.debug(ex.getMessage(), ex);
            throw new PushServerClientException(ex, new Error("PUSH_SERVER_CLIENT_ERROR", "HTTP POST request failed."));
        }
    }

    /**
     * Prepare POST object response.
     *
     * @param url specific url of method
     * @param request request body
     * @param responseType response type
     * @return Object obtained after processing the response JSON.
     * @throws PushServerClientException In case of network, response / JSON processing, or other IO error.
     */
    private <T> ObjectResponse<T> postObjectImpl(String url, ObjectRequest<?> request, Class<T> responseType) throws PushServerClientException {
        try {
            return restClient.postObject(url, request, responseType);
        } catch (RestClientException ex) {
            logger.debug(ex.getMessage(), ex);
            throw new PushServerClientException(ex, new Error("PUSH_SERVER_CLIENT_ERROR", "HTTP POST request failed."));
        }
    }

    /**
     * Prepare a generic PUT response.
     *
     * @param url specific url of method
     * @param request request body
     * @param typeReference type reference
     * @return Object obtained after processing the response JSON.
     * @throws PushServerClientException In case of network, response / JSON processing, or other IO error.
     */
    private <T> T putImpl(String url, Object request, ParameterizedTypeReference<T> typeReference) throws PushServerClientException {
        try {
            return restClient.put(url, request, typeReference).getBody();
        } catch (RestClientException ex) {
            logger.debug(ex.getMessage(), ex);
            throw new PushServerClientException(ex, new Error("PUSH_SERVER_CLIENT_ERROR", "HTTP POST request failed."));
        }
    }

    /**
     * Prepare a generic PUT response.
     *
     * @param url specific url of method
     * @param request request body
     * @param queryParams query parameters
     * @param headers HTTP headers
     * @param typeReference type reference
     * @return Object obtained after processing the response JSON.
     * @throws PushServerClientException In case of network, response / JSON processing, or other IO error.
     */
    private <T> T putImpl(String url, Object request, MultiValueMap<String, String> queryParams, MultiValueMap<String, String> headers, ParameterizedTypeReference<T> typeReference) throws PushServerClientException {
        try {
            return restClient.put(url, request, queryParams, headers, typeReference).getBody();
        } catch (RestClientException ex) {
            logger.debug(ex.getMessage(), ex);
            throw new PushServerClientException(ex, new Error("PUSH_SERVER_CLIENT_ERROR", "HTTP POST request failed."));
        }
    }

    /**
     * Prepare DELETE response. Uses default {@link Response} type reference for response.
     *
     * @param url specific url of method
     * @return Object obtained after processing the response JSON.
     * @throws PushServerClientException In case of network, response / JSON processing, or other IO error.
     */
    private Response deleteImpl(String url) throws PushServerClientException {
        try {
            return restClient.delete(url, new ParameterizedTypeReference<Response>(){}).getBody();
        } catch (RestClientException ex) {
            logger.debug(ex.getMessage(), ex);
            throw new PushServerClientException(ex, new Error("PUSH_SERVER_CLIENT_ERROR", "HTTP DELETE request failed."));
        }
    }

    /**
     * Prepare PUT object response. Uses default {@link Response} type reference for response.
     *
     * @param url specific url of method
     * @param request request body
     * @return Object obtained after processing the response JSON.
     * @throws PushServerClientException In case of network, response / JSON processing, or other IO error.
     */
    private Response putObjectImpl(String url, ObjectRequest<?> request) throws PushServerClientException {
        try {
            return restClient.putObject(url, request);
        } catch (RestClientException ex) {
            logger.debug(ex.getMessage(), ex);
            throw new PushServerClientException(ex, new Error("PUSH_SERVER_CLIENT_ERROR", "HTTP POST request failed."));
        }
    }

    /**
     * Mask push service token to avoid leaking tokens in log files.
     * @param token Push service token.
     * @return Masked push service token.
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 10) {
            return token;
        }
        return token.substring(0, 10) + "...";
    }

    /**
     * Convert input page and size to query parameter map.
     * @param page Page index. Zero indexed. Default: 0.
     * @param size Page size. Default: 100.
     * @return Query parameter map.
     */
    private MultiValueMap<String, String> buildPages(Integer page, Integer size) {
        final MultiValueMap<String, String> result = new LinkedMultiValueMap<>();
        if (page != null) {
            result.put("page", Collections.singletonList(page.toString()));
        } else {
            result.put("page", Collections.singletonList("0"));
        }
        if (size != null) {
            result.put("size", Collections.singletonList(size.toString()));
        } else {
            result.put("size", Collections.singletonList("100"));
        }
        return result;
    }

}
