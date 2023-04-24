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

package io.getlime.push.controller.rest;

import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.core.rest.model.base.response.Response;
import io.getlime.push.errorhandling.exceptions.PushServerException;
import io.getlime.push.model.base.PagedResponse;
import io.getlime.push.model.entity.ListOfUsers;
import io.getlime.push.model.entity.PushMessageBody;
import io.getlime.push.model.request.CreateCampaignRequest;
import io.getlime.push.model.response.*;
import io.getlime.push.model.validator.CreateCampaignRequestValidator;
import io.getlime.push.repository.AppCredentialsRepository;
import io.getlime.push.repository.PushCampaignRepository;
import io.getlime.push.repository.PushCampaignUserRepository;
import io.getlime.push.repository.model.AppCredentialsEntity;
import io.getlime.push.repository.model.PushCampaignEntity;
import io.getlime.push.repository.model.PushCampaignUserEntity;
import io.getlime.push.repository.serialization.JsonSerialization;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Controller for push campaign related methods
 *
 * @author Martin Tupy, martin.tupy.work@gmail.com
 */
@RestController
@RequestMapping(value = "push/campaign")
public class PushCampaignController {

    private static final Logger logger = LoggerFactory.getLogger(PushCampaignController.class);

    private final AppCredentialsRepository appCredentialsRepository;
    private final PushCampaignRepository pushCampaignRepository;
    private final PushCampaignUserRepository pushCampaignUserRepository;
    private final JsonSerialization jsonSerialization;

    /**
     * Constructor with autowired dependencies.
     *
     * @param appCredentialsRepository App credentials repository.
     * @param pushCampaignRepository Push campaign repository.
     * @param pushCampaignUserRepository Push campaign user repository.
     * @param jsonSerialization Helper for JSON serialization.
     */
    @Autowired
    public PushCampaignController(AppCredentialsRepository appCredentialsRepository, PushCampaignRepository pushCampaignRepository,
                                  PushCampaignUserRepository pushCampaignUserRepository, JsonSerialization jsonSerialization) {
        this.appCredentialsRepository = appCredentialsRepository;
        this.pushCampaignRepository = pushCampaignRepository;
        this.pushCampaignUserRepository = pushCampaignUserRepository;
        this.jsonSerialization = jsonSerialization;
    }

    /**
     * Create a campaign for specific app.
     *
     * @param request Id of specific app, body of specific messageBody
     * @return ID for created campaign.
     * @throws PushServerException In case request is invalid.
     */
    @PostMapping(value = "create")
    @Operation(summary = "Create a campaign",
                  description = "Creating a campaign requires in request body an application id to be related with " +
                          "and a certain message that users will receive")
    public ObjectResponse<CreateCampaignResponse> createCampaign(@RequestBody ObjectRequest<CreateCampaignRequest> request) throws PushServerException {
        final CreateCampaignRequest requestObject = request.getRequestObject();
        if (requestObject == null) {
            throw new PushServerException("Request object must not be empty");
        }
        final String appId = requestObject.getAppId();
        logger.info("Received createCampaign request, app ID: {}", appId);
        final String errorMessage = CreateCampaignRequestValidator.validate(requestObject);
        if (errorMessage != null) {
            throw new PushServerException(errorMessage);
        }

        final AppCredentialsEntity applicationCredentials = findApplicationCredentials(appId);

        PushCampaignEntity campaign = new PushCampaignEntity();
        final PushMessageBody message = requestObject.getMessage();
        final String messageString = jsonSerialization.serializePushMessageBody(message);
        campaign.setAppCredentials(applicationCredentials);
        campaign.setSent(false);
        campaign.setTimestampCreated(new Date());
        campaign.setMessage(messageString);
        campaign = pushCampaignRepository.save(campaign);
        final CreateCampaignResponse response = new CreateCampaignResponse();
        response.setId(campaign.getId());
        logger.info("The createCampaign request succeeded, app ID: {}, campaign ID: {}", appId, campaign.getId());
        return new ObjectResponse<>(response);
    }

    /**
     * Removes a certain campaign along with users related to this campaign.
     *
     * @param campaignId Identifier for deleting a campaign
     * @return Remove campaign status response.
     */
    @RequestMapping(value = "{id}/delete", method = { RequestMethod.POST, RequestMethod.DELETE })
    @Operation(summary = "Delete a campaign",
                  description = "Specified with id. Also users associated with this campaign are going to be deleted. If deletion was applied then deleted status is true. False if such campaign does not exist")
    public ObjectResponse<DeleteCampaignResponse> deleteCampaign(@PathVariable(value = "id") Long campaignId) {
        logger.info("Received deleteCampaign request, campaign ID: {}", campaignId);
        final DeleteCampaignResponse deleteCampaignResponse = new DeleteCampaignResponse();
        final Optional<PushCampaignEntity> campaignEntityOptional = pushCampaignRepository.findById(campaignId);
        if (campaignEntityOptional.isEmpty()) {
            deleteCampaignResponse.setDeleted(false);
        } else {
            pushCampaignRepository.delete(campaignEntityOptional.get());
            deleteCampaignResponse.setDeleted(true);
        }
        pushCampaignUserRepository.deleteByCampaignId(campaignId);
        logger.info("The deleteCampaign request succeeded, campaign ID: {}", campaignId);
        return new ObjectResponse<>(deleteCampaignResponse);
    }

    /**
     * Method used for getting a specific campaign
     *
     * @param campaignId Id of specific campaign
     * @return Campaign Response
     * @throws PushServerException In case campaign with provided ID does not exist.
     */
    @GetMapping(value = "{id}/detail")
    @Operation(summary = "Return details about campaign",
                  description = "Campaign specified by id. Details contain campaign id, application id, status if campaign was sent and message.")
    public ObjectResponse<CampaignResponse> getCampaign(@PathVariable(value = "id") Long campaignId) throws PushServerException {
        logger.debug("Received getCampaign request, campaign ID: {}", campaignId);
        final PushCampaignEntity campaign = findPushCampaignById(campaignId);
        final CampaignResponse campaignResponse = new CampaignResponse();
        campaignResponse.setId(campaign.getId());
        campaignResponse.setSent(campaign.isSent());
        campaignResponse.setAppId(campaign.getAppCredentials().getAppId());
        final PushMessageBody message = jsonSerialization.deserializePushMessageBody(campaign.getMessage());
        campaignResponse.setMessage(message);
        logger.debug("The getCampaign request succeeded, campaign ID: {}", campaignId);
        return new ObjectResponse<>(campaignResponse);
    }

    /**
     * Method used for getting a list of campaigns.
     *
     * @param all If true, method returns list of all campaigns. If false, it returns
     * only campaigns that were not sent yet.
     * @return List of campaigns.
     * @throws PushServerException In case campaign message cannot be deserialized.
     */
    @GetMapping(value = "list")
    @Operation(summary = "Return a detailed list of campaigns",
                  description = "Restricted with all param. This parameter decides if return campaigns that are 'only sent'(statement false)" +
                          " or return all registered campaigns (statement true). Details are same as in getCampaign method")
    public ObjectResponse<ListOfCampaignsResponse> getListOfCampaigns(@RequestParam(value = "all", required = false) boolean all) throws PushServerException {
        logger.debug("Received getListOfCampaigns request");
        // Fetch campaigns from the repository
        final Iterable<PushCampaignEntity> campaignList;
        if (all) {
            campaignList = pushCampaignRepository.findAll();
        } else {
            campaignList = pushCampaignRepository.findAllBySent(false);
        }
        // Prepare response with list of campaigns
        final ListOfCampaignsResponse listOfCampaignsResponse = new ListOfCampaignsResponse();
        for (PushCampaignEntity campaign : campaignList) {
            final CampaignResponse campaignResponse = new CampaignResponse();
            campaignResponse.setId(campaign.getId());
            campaignResponse.setAppId(campaign.getAppCredentials().getAppId());
            campaignResponse.setSent(campaign.isSent());
            final PushMessageBody pushMessageBody = jsonSerialization.deserializePushMessageBody(campaign.getMessage());
            campaignResponse.setMessage(pushMessageBody);
            listOfCampaignsResponse.add(campaignResponse);
        }
        logger.debug("The getListOfCampaigns request succeeded");
        return new ObjectResponse<>(listOfCampaignsResponse);
    }


    /**
     * Add specific request to specific campaign
     *
     * @param id ID of certain campaign
     * @param request List of IDs referred to request
     * @return Response status
     * @throws PushServerException In case campaign with given ID does not exist.
     */
    @RequestMapping(value = "{id}/user/add", method = { RequestMethod.POST, RequestMethod.PUT })
    @Operation(summary = "Associate users to campaign",
                  description = "Users are identified in request body as an array of strings in request body.")
    public Response addUsersToCampaign(@PathVariable(value = "id") Long id, @RequestBody ObjectRequest<ListOfUsers> request) throws PushServerException {
        checkRequestNullity(request);
        logger.info("Received addUsersToCampaign request, campaign ID: {}, users: {}", id, request.getRequestObject());
        assureExistsPushCampaignById(id);
        final ListOfUsers listOfUsers = request.getRequestObject();
        for (String user : listOfUsers) {
            if (pushCampaignUserRepository.findFirstByUserIdAndCampaignId(user, id) == null) {
                final PushCampaignUserEntity pushCampaignUserEntity = new PushCampaignUserEntity();
                pushCampaignUserEntity.setCampaignId(id);
                pushCampaignUserEntity.setUserId(user);
                pushCampaignUserEntity.setTimestampCreated(new Date());
                pushCampaignUserRepository.save(pushCampaignUserEntity);
                logger.info("The addUsersToCampaign request succeeded, campaign ID: {}", id);
            } else {
                logger.warn("Duplicate user entry for push campaign: {}", user);
            }
        }
        return new Response();
    }

    /**
     * Method for getting users from specific campaign in paged format
     *
     * @param id Campaign id
     * @param pageable Format for pagination (?page=x&amp;size=y), where "x" is chosen page and "y" is size is number of elements per page, beginning from zero
     * @return Campaign id, list of users
     */
    @GetMapping(value = "{id}/user/list")
    @Operation(summary = "Return list of users",
                  description = "Return all users' ids from campaign that is specified in URI {id} variable. " +
                          "Users are shown in paginated format based on parameters assigned in URI. " +
                          "Page param defines which page to show (start from 0) and size param which defines how many user ids to show per page")
    public PagedResponse<ListOfUsersFromCampaignResponse> getListOfUsersFromCampaign(@PathVariable(value = "id") Long id, Pageable pageable) {
        logger.debug("Received getListOfUsersFromCampaign request, campaign ID: {}", id);
        ListOfUsersFromCampaignResponse listOfUsersFromCampaignResponse = new ListOfUsersFromCampaignResponse();
        List<PushCampaignUserEntity> users = pushCampaignUserRepository.findAllByCampaignId(id, pageable);
        ListOfUsers listOfUsers = new ListOfUsers();
        for (PushCampaignUserEntity user : users) {
            listOfUsers.add(user.getUserId());
        }
        listOfUsersFromCampaignResponse.setCampaignId(id);
        listOfUsersFromCampaignResponse.setUsers(listOfUsers);
        PagedResponse<ListOfUsersFromCampaignResponse> listOfUsersPagedResponse = new PagedResponse<>(listOfUsersFromCampaignResponse);
        listOfUsersPagedResponse.setPage(pageable.getPageNumber());
        listOfUsersPagedResponse.setSize(pageable.getPageSize());
        logger.debug("The getListOfUsersFromCampaign request succeeded, campaign ID: {}", id);
        return listOfUsersPagedResponse;
    }

    /**
     * Removes campaign users identified by list of users userID and campaignID
     *
     * @param id Campaign ID
     * @param request List of users to remove
     * @return Response status
     */
    @RequestMapping(value = "{id}/user/delete", method = { RequestMethod.POST, RequestMethod.DELETE })
    @Operation(summary = "Delete users from campaign",
                  description = "Delete users from certain campaign specified with {id} variable in URI." +
                          "Users are described as list of their ids in Request body")
    public Response deleteUsersFromCampaign(@PathVariable(value = "id") Long id, @RequestBody ObjectRequest<ListOfUsers> request) {
        logger.info("Received deleteUsersFromCampaign request, campaign ID: {}, users: {}", id, request.getRequestObject());
        ListOfUsers listOfUsers = request.getRequestObject();
        for (String user : listOfUsers) {
            pushCampaignUserRepository.deleteByCampaignIdAndUserId(id, user);
        }
        logger.info("The deleteUsersFromCampaign request succeeded, campaign ID: {}", id);
        return new Response();
    }

    /**
     * Find application credentials by PowerAuth app ID.
     *
     * @param powerAuthAppId PowerAuth App ID.
     * @return Application credentials.
     * @throws PushServerException In case the application credentials do not exist for provided PowerAuth app ID.
     */
    private AppCredentialsEntity findApplicationCredentials(String powerAuthAppId) throws PushServerException {
        return appCredentialsRepository.findFirstByAppId(powerAuthAppId).orElseThrow(() ->
                new PushServerException("Application with a provided PowerAuth app ID does not exist: " + powerAuthAppId));
    }

    /**
     * Method used for checking exception about nullity of http request
     *
     * @param request An object request to check the nullity
     * @throws PushServerException In case request object is null.
     */
    private void checkRequestNullity(ObjectRequest<?> request) throws PushServerException {
        if (request.getRequestObject() == null) {
            throw new PushServerException("Empty requestObject data");
        }
    }

    /**
     * Find push campaign entity by ID.
     * @param campaignId Campaign ID.
     * @return Push campaign entity.
     * @throws PushServerException Thrown when campaign entity does not exist.
     */
    private PushCampaignEntity findPushCampaignById(Long campaignId) throws PushServerException {
        return pushCampaignRepository.findById(campaignId).orElseThrow(() ->
            new PushServerException("Campaign with entered ID does not exist"));
    }

    /**
     * Assure that a campaign by given ID exists campaign entity by ID.
     * @param campaignId Campaign ID.
     * @throws PushServerException Thrown when campaign entity does not exist.
     */
    private void assureExistsPushCampaignById(Long campaignId) throws PushServerException {
        final Optional<PushCampaignEntity> campaignEntityOptional = pushCampaignRepository.findById(campaignId);
        if (campaignEntityOptional.isEmpty()) {
            throw new PushServerException("Campaign with entered ID does not exist");
        }
    }
}
