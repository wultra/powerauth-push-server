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

package io.getlime.push.client;

import com.google.common.io.BaseEncoding;
import com.wultra.core.rest.client.base.DefaultRestClient;
import com.wultra.core.rest.client.base.RestClient;
import com.wultra.core.rest.client.base.RestClientException;
import io.getlime.core.rest.model.base.entity.Error;
import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.core.rest.model.base.response.Response;
import io.getlime.push.model.base.PagedResponse;
import io.getlime.push.model.entity.ListOfUsers;
import io.getlime.push.model.entity.PushMessage;
import io.getlime.push.model.entity.PushMessageBody;
import io.getlime.push.model.entity.PushMessageSendResult;
import io.getlime.push.model.enumeration.MobilePlatform;
import io.getlime.push.model.request.*;
import io.getlime.push.model.response.*;
import io.getlime.push.model.validator.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
            throw new PushServerClientException("Rest client initialization failed, error: " + ex.getMessage());
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

        logger.info("Calling push server status service - start");
        final ObjectResponse<ServiceStatusResponse> result = getObjectImpl("/push/service/status", null, ServiceStatusResponse.class);
        logger.info("Calling push server status service - finish");

        return result;
    }

    /**
     * Register anonymous device to the push server.
     *
     * @param appId PowerAuth application app ID.
     * @param token Token received from the push service provider (APNs, FCM).
     * @param platform Mobile platform (iOS, Android).
     * @return True if device registration was successful, false otherwise.
     * @throws PushServerClientException In case of network, response / JSON processing, or other IO error.
     */
    public boolean createDevice(Long appId, String token, MobilePlatform platform) throws PushServerClientException {
        return createDevice(appId, token, platform, null);
    }

    /**
     * Register device associated with activation ID to the push server.
     *
     * @param appId PowerAuth application app ID.
     * @param token Token received from the push service provider (APNs, FCM).
     * @param platform Mobile platform (iOS, Android).
     * @param activationId PowerAuth activation ID.
     * @return True if device registration was successful, false otherwise.
     * @throws PushServerClientException In case of network, response / JSON processing, or other IO error.
     */
    public boolean createDevice(Long appId, String token, MobilePlatform platform, String activationId) throws PushServerClientException {
        CreateDeviceRequest request = new CreateDeviceRequest();
        request.setAppId(appId);
        request.setToken(token);
        request.setPlatform(platform.value());
        request.setActivationId(activationId);

        // Validate request on the client side.
        String error = CreateDeviceRequestValidator.validate(request);
        if (error != null) {
            throw new PushServerClientException(error);
        }

        logger.info("Calling create device service, appId: {}, token: {}, platform: {} - start", appId, maskToken(token), platform.value());
        Response response = postObjectImpl("/push/device/create", new ObjectRequest<>(request));
        logger.info("Calling create device service, appId: {}, token: {}, platform: {} - finish", appId, maskToken(token), platform.value());

        return response.getStatus().equals(Response.Status.OK);
    }

    /**
     * Register device associated with multiple activation IDs to the push server.
     *
     * @param appId PowerAuth application app ID.
     * @param token Token received from the push service provider (APNs, FCM).
     * @param platform Mobile platform (iOS, Android).
     * @param activationIds PowerAuth activation IDs.
     * @return True if device registration was successful, false otherwise.
     * @throws PushServerClientException In case of network, response / JSON processing, or other IO error.
     */
    public boolean createDeviceForActivations(Long appId, String token, MobilePlatform platform, List<String> activationIds) throws PushServerClientException {
        CreateDeviceForActivationsRequest request = new CreateDeviceForActivationsRequest();
        request.setAppId(appId);
        request.setToken(token);
        request.setPlatform(platform.value());
        request.getActivationIds().addAll(activationIds);

        // Validate request on the client side.
        String error = CreateDeviceRequestValidator.validate(request);
        if (error != null) {
            throw new PushServerClientException(error);
        }

        logger.info("Calling create device service, appId: {}, token: {}, platform: {} - start", appId, maskToken(token), platform.value());
        Response response = postObjectImpl("/push/device/create/multi", new ObjectRequest<>(request));
        logger.info("Calling create device service, appId: {}, token: {}, platform: {} - finish", appId, maskToken(token), platform.value());

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
    public boolean deleteDevice(Long appId, String token) throws PushServerClientException {
        DeleteDeviceRequest request = new DeleteDeviceRequest();
        request.setAppId(appId);
        request.setToken(token);

        // Validate request on the client side.
        String error = DeleteDeviceRequestValidator.validate(request);
        if (error != null) {
            throw new PushServerClientException(error);
        }

        logger.info("Calling push server delete device service, appId: {}, token: {} - start", appId, maskToken(token));
        Response response = postObjectImpl("/push/device/delete", new ObjectRequest<>(request));
        logger.info("Calling push server delete device service, appId: {}, token: {} - finish", appId, maskToken(token));

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

        logger.info("Calling push server update device status, activation ID: {} - start", activationId);
        // Note that there is just plain 'request' in the request, not 'new ObjectRequest<>(request)'.
        // This is due to the fact that standard PowerAuth Server callback format is used here.
        Response response = postImpl("/push/device/status/update", request, new ParameterizedTypeReference<Response>(){});
        logger.info("Calling push server update device status, activation ID: {} - finish", activationId);

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
    public ObjectResponse<PushMessageSendResult> sendPushMessage(Long appId, PushMessage pushMessage) throws PushServerClientException {
        SendPushMessageRequest request = new SendPushMessageRequest();
        request.setAppId(appId);
        request.setMessage(pushMessage);

        // Validate request on the client side.
        String error = SendPushMessageRequestValidator.validate(request);
        if (error != null) {
            throw new PushServerClientException(error);
        }

        logger.info("Calling push server to send a push message, app ID: {}, user ID: {} - start", appId, pushMessage.getUserId());
        final ObjectResponse<PushMessageSendResult> result = postObjectImpl("/push/message/send", new ObjectRequest<>(request), PushMessageSendResult.class);
        logger.info("Calling push server to send a push message, app ID: {}, user ID: {} - finish", appId, pushMessage.getUserId());

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
    public ObjectResponse<PushMessageSendResult> sendPushMessageBatch(Long appId, List<PushMessage> batch) throws PushServerClientException {
        SendPushMessageBatchRequest request = new SendPushMessageBatchRequest();
        request.setAppId(appId);
        request.setBatch(batch);

        // Validate request on the client side.
        String error = SendPushMessageBatchRequestValidator.validate(request);
        if (error != null) {
            throw new PushServerClientException(error);
        }

        logger.info("Calling push server to send a push message batch, app ID: {} - start", appId);
        final ObjectResponse<PushMessageSendResult> result = postObjectImpl("/push/message/batch/send", new ObjectRequest<>(request), PushMessageSendResult.class);
        logger.info("Calling push server to send a push message batch, app ID: {} - finish", appId);

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
    public ObjectResponse<CreateCampaignResponse> createCampaign(Long appId, PushMessageBody message) throws PushServerClientException {
        CreateCampaignRequest request = new CreateCampaignRequest();
        request.setAppId(appId);
        request.setMessage(message);

        // Validate request on the client side.
        String error = CreateCampaignRequestValidator.validate(request);
        if (error != null) {
            throw new PushServerClientException(error);
        }

        logger.info("Calling push server to create a push campaign, app ID: {} - start", appId);
        final ObjectResponse<CreateCampaignResponse> result = postObjectImpl("/push/campaign/create", new ObjectRequest<>(request), CreateCampaignResponse.class);
        logger.info("Calling push server to create a push campaign, app ID: {} - finish", appId);

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
        try {
            String campaignIdSanitized = URLEncoder.encode(String.valueOf(campaignId), "UTF-8");

            logger.info("Calling push server to delete a push campaign, campaign ID: {} - start", campaignId);
            ObjectResponse<DeleteCampaignResponse> response = postObjectImpl("/push/campaign/" + campaignIdSanitized + "/delete", null, DeleteCampaignResponse.class);
            logger.info("Calling push server to delete a push campaign, campaign ID: {} - finish", campaignId);

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
     * @throws PushServerClientException In case of network, response / JSON processing, or other IO error.
     */
    public ObjectResponse<ListOfCampaignsResponse> getListOfCampaigns(boolean all) throws PushServerClientException {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("all", Collections.singletonList(Boolean.valueOf(all).toString()));

        logger.info("Calling push server to obtain a push campaign list - start");
        final ObjectResponse<ListOfCampaignsResponse> result = getObjectImpl("/push/campaign/list", params, ListOfCampaignsResponse.class);
        logger.info("Calling push server to obtain a push campaign list - finish");

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
        try {
            String campaignIdSanitized = URLEncoder.encode(String.valueOf(campaignId), "UTF-8");

            logger.info("Calling push server to obtain a push campaign detail, campaign ID: {} - start", campaignId);
            final ObjectResponse<CampaignResponse> result = getObjectImpl("/push/campaign/" + campaignIdSanitized + "/detail", null, CampaignResponse.class);
            logger.info("Calling push server to obtain a push campaign detail, campaign ID: {} - finish", campaignId);

            return result;
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
     * @throws PushServerClientException In case of network, response / JSON processing, or other IO error.
     */
    public boolean addUsersToCampaign(Long campaignId, List<String> users) throws PushServerClientException {
        try {
            ListOfUsers listOfUsers = new ListOfUsers(users);
            String campaignIdSanitized = URLEncoder.encode(String.valueOf(campaignId), "UTF-8");

            logger.info("Calling push server to add users to campaign, campaign ID: {} - start", campaignId);
            Response response = putObjectImpl("/push/campaign/" + campaignIdSanitized + "/user/add", new ObjectRequest<>(listOfUsers));
            logger.info("Calling push server to add users to campaign, campaign ID: {} - finish", campaignId);

            if (response == null) {
                throw new PushServerClientException(new Error("PUSH_SERVER_CLIENT_ERROR", "Network communication has failed."));
            }

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
     * @throws PushServerClientException In case of network, response / JSON processing, or other IO error.
     */
    public PagedResponse<ListOfUsersFromCampaignResponse> getListOfUsersFromCampaign(Long campaignId, int page, int size) throws PushServerClientException {
        try {
            String campaignIdSanitized = URLEncoder.encode(String.valueOf(campaignId), "UTF-8");
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.put("page", Collections.singletonList(Integer.valueOf(page).toString()));
            params.put("size", Collections.singletonList(Integer.valueOf(size).toString()));

            ParameterizedTypeReference<PagedResponse<ListOfUsersFromCampaignResponse>> typeReference = new ParameterizedTypeReference<PagedResponse<ListOfUsersFromCampaignResponse>>() {};
            logger.info("Calling push server to get users from the campaign, campaign ID: {} - start", campaignId);
            final PagedResponse<ListOfUsersFromCampaignResponse> result = getImpl("/push/campaign/" + campaignIdSanitized + "/user/list", params, typeReference);
            logger.info("Calling push server to get users from the campaign, campaign ID: {} - finish", campaignId);

            return result;
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
     * @throws PushServerClientException In case of network, response / JSON processing, or other IO error.
     */
    public boolean deleteUsersFromCampaign(Long campaignId, List<String> users) throws PushServerClientException {
        try {
            ListOfUsers listOfUsers = new ListOfUsers(users);
            String campaignIdSanitized = URLEncoder.encode(String.valueOf(campaignId), "UTF-8");

            logger.info("Calling push server to remove users from the campaign, campaign ID: {} - start", campaignId);
            Response response = postObjectImpl("/push/campaign/" + campaignIdSanitized + "/user/delete", new ObjectRequest<>(listOfUsers));
            logger.info("Calling push server to remove users from the campaign, campaign ID: {} - finish", campaignId);

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
     * @throws PushServerClientException In case of network, response / JSON processing, or other IO error.
     */
    public boolean sendTestCampaign(Long campaignId, String userId) throws PushServerClientException {
        try {
            String campaignIdSanitized = URLEncoder.encode(String.valueOf(campaignId), "UTF-8");
            TestCampaignRequest request = new TestCampaignRequest();
            request.setUserId(userId);

            // Validate request on the client side.
            String error = TestCampaignRequestValidator.validate(request);
            if (error != null) {
                throw new PushServerClientException(error);
            }

            logger.info("Calling push server to send test campaign, campaign ID: {}, user ID: {} - start", campaignId, userId);
            Response response = postObjectImpl("/push/campaign/send/test/" + campaignIdSanitized, new ObjectRequest<>(request));
            logger.info("Calling push server to send test campaign, campaign ID: {}, user ID: {} - finish", campaignId, userId);

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
     * @throws PushServerClientException In case of network, response / JSON processing, or other IO error.
     */
    public boolean sendCampaign(Long campaignId) throws PushServerClientException {
        try {
            String campaignIdSanitized = URLEncoder.encode(String.valueOf(campaignId), "UTF-8");

            logger.info("Calling push server to send a production campaign, campaign ID: {} - start", campaignId);
            Response response = postObjectImpl("/push/campaign/send/live/" + campaignIdSanitized, null);
            logger.info("Calling push server to send a production campaign, campaign ID: {} - finish", campaignId);

            return response.getStatus().equals(Response.Status.OK);
        } catch (UnsupportedEncodingException e) {
            throw new PushServerClientException(new Error("PUSH_SERVER_CLIENT_ERROR", e.getMessage()));
        }
    }

    /**
     * Get list of application credentials entities.
     * @return Application credentials entity list.
     * @throws PushServerClientException Thrown when communication with Push Server fails.
     */
    public ObjectResponse<GetApplicationListResponse> getApplicationList() throws PushServerClientException {
        logger.info("Calling push server to retrieve list of applications - start");
        final ObjectResponse<GetApplicationListResponse> response = getObjectImpl("/admin/app/list", null, GetApplicationListResponse.class);
        logger.info("Calling push server to retrieve list of applications - finish");
        return response;
    }

    /**
     * Get list of applications which are not yet configured in Push Server but exist in PowerAuth server.
     * @return List of applications which are not configured.
     * @throws PushServerClientException Thrown when communication with Push Server fails.
     */
    public ObjectResponse<GetApplicationListResponse> getUnconfiguredApplicationList() throws PushServerClientException {
        logger.info("Calling push server to retrieve list of unconfigured applications - start");
        final ObjectResponse<GetApplicationListResponse> response = getObjectImpl("/admin/app/unconfigured/list", null, GetApplicationListResponse.class);
        logger.info("Calling push server to retrieve list of unconfigured applications - finish");
        return response;
    }

    /**
     * Get detail for an application credentials entity.
     * @param id Application credentials entity ID.
     * @param includeIos Whether to include iOS details.
     * @param includeAndroid Whether to include Android details.
     * @return Application credentials entity detail.
     * @throws PushServerClientException Thrown when communication with Push Server fails.
     */
    public ObjectResponse<GetApplicationDetailResponse> getApplicationDetail(Long id, boolean includeIos, boolean includeAndroid) throws PushServerClientException {
        GetApplicationDetailRequest request = new GetApplicationDetailRequest(id, includeIos, includeAndroid);
        logger.info("Calling push server to retrieve application detail, ID: {} - start", id);
        final ObjectResponse<GetApplicationDetailResponse> response = postObjectImpl("/admin/app/detail", new ObjectRequest<>(request), GetApplicationDetailResponse.class);
        logger.info("Calling push server to retrieve application detail, ID: {} - finish", id);
        return response;
    }

    /**
     * Create application credentials entity.
     * @param appId PowerAuth application ID.
     * @return Response with ID of created application credentials entity.
     * @throws PushServerClientException Thrown when communication with Push Server fails.
     */
    public ObjectResponse<CreateApplicationResponse> createApplication(Long appId) throws PushServerClientException {
        final CreateApplicationRequest request = new CreateApplicationRequest(appId);
        logger.info("Calling push server to create application, app ID: {} - start", appId);
        final ObjectResponse<CreateApplicationResponse> response = postObjectImpl("/admin/app/create", new ObjectRequest<>(request), CreateApplicationResponse.class);
        logger.info("Calling push server to create application, app ID: {} - finish", appId);
        return response;
    }

    /**
     * Update iOS details for an application credentials entity.
     * @param id ID of application credentials entity.
     * @param bundle The iOS bundle record.
     * @param keyId The iOS key record.
     * @param teamId The iOS team ID record.
     * @param privateKey The iOS private key bytes.
     * @return Response from server.
     * @throws PushServerClientException Thrown when communication with Push Server fails.
     */
    public Response updateIos(Long id, String bundle, String keyId, String teamId, byte[] privateKey) throws PushServerClientException {
        final String privateKeyBase64 = BaseEncoding.base64().encode(privateKey);
        final UpdateIosRequest request = new UpdateIosRequest(id, bundle, keyId, teamId, privateKeyBase64);
        logger.info("Calling push server to update iOS, ID: {} - start", id);
        final Response response = putObjectImpl("/admin/app/ios/update", new ObjectRequest<>(request));
        logger.info("Calling push server to update iOS, ID: {} - finish", id);
        return response;
    }

    /**
     * Remove iOS record from an application credentials entity.
     * @param id Application credentials entity ID.
     * @return Response from server.
     * @throws PushServerClientException Thrown when communication with Push Server fails.
     */
    public Response removeIos(Long id) throws PushServerClientException {
        final RemoveIosRequest request = new RemoveIosRequest(id);
        logger.info("Calling push server to remove iOS, ID: {} - start", id);
        final Response response = postObjectImpl("/admin/app/ios/remove", new ObjectRequest<>(request));
        logger.info("Calling push server to remove iOS, ID: {} - finish", id);
        return response;
    }

    /**
     * Update Android details for an application credentials entity.
     * @param id Application credentials entity ID.
     * @param projectId The Android project ID record.
     * @param privateKey The Android private key bytes.
     * @return Response from server.
     * @throws PushServerClientException Thrown when communication with Push Server fails.
     */
    public Response updateAndroid(Long id, String projectId, byte[] privateKey) throws PushServerClientException {
        final String privateKeyBase64 = BaseEncoding.base64().encode(privateKey);
        final UpdateAndroidRequest request = new UpdateAndroidRequest(id, projectId, privateKeyBase64);
        logger.info("Calling push server to update android, ID: {} - start", id);
        final Response response = putObjectImpl("/admin/app/android/update", new ObjectRequest<>(request));
        logger.info("Calling push server to update android, ID: {} - finish", id);
        return response;
    }

    /**
     * Remove Android record from an application credentials entity.
     * @param id Application credentials entity ID.
     * @return Response from server.
     * @throws PushServerClientException Thrown when communication with Push Server fails.
     */
    public Response removeAndroid(Long id) throws PushServerClientException {
        final RemoveAndroidRequest request = new RemoveAndroidRequest(id);
        logger.info("Calling push server to remove android, ID: {} - start", id);
        final Response response = postObjectImpl("/admin/app/android/remove", new ObjectRequest<>(request));
        logger.info("Calling push server to remove android, ID: {} - finish", id);
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
            logger.warn(ex.getMessage(), ex);
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
            logger.warn(ex.getMessage(), ex);
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
            logger.warn(ex.getMessage(), ex);
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
            logger.warn(ex.getMessage(), ex);
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
            logger.warn(ex.getMessage(), ex);
            throw new PushServerClientException(ex, new Error("PUSH_SERVER_CLIENT_ERROR", "HTTP POST request failed."));
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
            logger.warn(ex.getMessage(), ex);
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

}
