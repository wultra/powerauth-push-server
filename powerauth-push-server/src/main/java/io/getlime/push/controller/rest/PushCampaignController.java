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
import io.getlime.push.repository.PushCampaignRepository;
import io.getlime.push.repository.PushCampaignUserRepository;
import io.getlime.push.repository.model.PushCampaignEntity;
import io.getlime.push.repository.model.PushCampaignUserEntity;
import io.getlime.push.repository.serialization.JSONSerialization;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Controller for push campaign related methods
 *
 * @author Martin Tupy, martin.tupy.work@gmail.com
 */

@Controller
@RequestMapping(value = "push/campaign")
public class PushCampaignController {

    private static final Logger logger = LoggerFactory.getLogger(PushCampaignController.class);

    private PushCampaignRepository pushCampaignRepository;
    private PushCampaignUserRepository pushCampaignUserRepository;

    @Autowired
    public PushCampaignController(PushCampaignRepository pushCampaignRepository,
                                  PushCampaignUserRepository pushCampaignUserRepository) {
        this.pushCampaignRepository = pushCampaignRepository;
        this.pushCampaignUserRepository = pushCampaignUserRepository;
    }

    /**
     * Create a campaign for specific app.
     *
     * @param request Id of specific app, body of specific messageBody
     * @return ID for created campaign.
     * @throws PushServerException In case request is invalid.
     */
    @RequestMapping(value = "create", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "Create a campaign",
                  notes = "Creating a campaign requires in request body an application id to be related with " +
                          "and a certain message that users will receive")
    public ObjectResponse<CreateCampaignResponse> createCampaign(@RequestBody ObjectRequest<CreateCampaignRequest> request) throws PushServerException {
        CreateCampaignRequest requestObject = request.getRequestObject();
        String errorMessage = CreateCampaignRequestValidator.validate(requestObject);
        if (errorMessage != null) {
            throw new PushServerException(errorMessage);
        }
        PushCampaignEntity campaign = new PushCampaignEntity();
        PushMessageBody message = requestObject.getMessage();
        String messageString = JSONSerialization.serializePushMessageBody(message);
        campaign.setAppId(requestObject.getAppId());
        campaign.setSent(false);
        campaign.setTimestampCreated(new Date());
        campaign.setMessage(messageString);
        campaign = pushCampaignRepository.save(campaign);
        CreateCampaignResponse response = new CreateCampaignResponse();
        response.setId(campaign.getId());
        return new ObjectResponse<>(response);
    }

    /**
     * Removes a certain campaign along with users related to this campaign.
     *
     * @param campaignId Identifier for deleting a campaign
     * @return Remove campaign status response.
     */
    @RequestMapping(value = "{id}/delete", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "Delete a campaign",
                  notes = "Specified with id. Also users associated with this campaign are going to be deleted. If deletion was applied then deleted status is true. False if such campaign does not exist")
    public ObjectResponse<DeleteCampaignResponse> deleteCampaign(@PathVariable(value = "id") Long campaignId) {
        DeleteCampaignResponse deleteCampaignResponse = new DeleteCampaignResponse();
        final Optional<PushCampaignEntity> campaignEntityOptional = pushCampaignRepository.findById(campaignId);
        if (!campaignEntityOptional.isPresent()) {
            deleteCampaignResponse.setDeleted(false);
        } else {
            pushCampaignRepository.delete(campaignEntityOptional.get());
            deleteCampaignResponse.setDeleted(true);
        }
        pushCampaignUserRepository.deleteByCampaignId(campaignId);
        return new ObjectResponse<>(deleteCampaignResponse);
    }

    /**
     * Method used for getting a specific campaign
     *
     * @param campaignId Id of specific campaign
     * @return Campaign Response
     * @throws PushServerException In case campaign with provided ID does not exist.
     */
    @RequestMapping(value = "{id}/detail", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "Return details about campaign",
                  notes = "Campaign specified by id. Details contain campaign id, application id, status if campaign was sent and message.")
    public ObjectResponse<CampaignResponse> getCampaign(@PathVariable(value = "id") Long campaignId) throws PushServerException {
        final PushCampaignEntity campaign = findPushCampaignById(campaignId);
        CampaignResponse campaignResponse = new CampaignResponse();
        campaignResponse.setId(campaign.getId());
        campaignResponse.setSent(campaign.isSent());
        campaignResponse.setAppId(campaign.getAppId());
        PushMessageBody message = JSONSerialization.deserializePushMessageBody(campaign.getMessage());
        campaignResponse.setMessage(message);
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
    @RequestMapping(value = "list", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "Return a detailed list of campaigns",
                  notes = "Restricted with all param. This parameter decides if return campaigns that are 'only sent'(statement false)" +
                          " or return all registered campaigns (statement true). Details are same as in getCampaign method")
    public ObjectResponse<ListOfCampaignsResponse> getListOfCampaigns(@RequestParam(value = "all", required = false) boolean all) throws PushServerException {
        // Fetch campaigns from the repository
        Iterable<PushCampaignEntity> campaignList;
        if (all) {
            campaignList = pushCampaignRepository.findAll();
        } else {
            campaignList = pushCampaignRepository.findAllBySent(false);
        }
        // Prepare response with list of campaigns
        ListOfCampaignsResponse listOfCampaignsResponse = new ListOfCampaignsResponse();
        for (PushCampaignEntity campaign : campaignList) {
            CampaignResponse campaignResponse = new CampaignResponse();
            campaignResponse.setId(campaign.getId());
            campaignResponse.setAppId(campaign.getAppId());
            campaignResponse.setSent(campaign.isSent());
            PushMessageBody pushMessageBody = JSONSerialization.deserializePushMessageBody(campaign.getMessage());
            campaignResponse.setMessage(pushMessageBody);
            listOfCampaignsResponse.add(campaignResponse);
        }
        return new ObjectResponse<>(listOfCampaignsResponse);
    }


    /**
     * Add specific request to specific campaign
     *
     * @param campaignId ID of certain campaign
     * @param request List of IDs referred to request
     * @return Response status
     * @throws PushServerException In case campaign with given ID does not exist.
     */
    @RequestMapping(value = "{id}/user/add", method = RequestMethod.PUT)
    @ResponseBody
    @ApiOperation(value = "Associate users to campaign",
                  notes = "Users are identified in request body as an array of strings in request body.")
    public Response addUsersToCampaign(@PathVariable(value = "id") Long campaignId, @RequestBody ObjectRequest<ListOfUsers> request) throws PushServerException {
        checkRequestNullity(request);
        final PushCampaignEntity campaignEntity = findPushCampaignById(campaignId);
        ListOfUsers listOfUsers = request.getRequestObject();
        for (String user : listOfUsers) {
            if (pushCampaignUserRepository.findFirstByUserIdAndCampaignId(user, campaignId) == null) {
                PushCampaignUserEntity pushCampaignUserEntity = new PushCampaignUserEntity();
                pushCampaignUserEntity.setCampaignId(campaignId);
                pushCampaignUserEntity.setUserId(user);
                pushCampaignUserEntity.setAppId(campaignEntity.getAppId());
                pushCampaignUserEntity.setTimestampCreated(new Date());
                pushCampaignUserRepository.save(pushCampaignUserEntity);
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
    @RequestMapping(value = "{id}/user/list", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "Return list of users",
                  notes = "Return all users' ids from campaign that is specified in URI {id} variable. " +
                          "Users are shown in paginated format based on parameters assigned in URI. " +
                          "Page param defines which page to show (start from 0) and size param which defines how many user ids to show per page")
    public PagedResponse<ListOfUsersFromCampaignResponse> getListOfUsersFromCampaign(@PathVariable(value = "id") Long id, Pageable pageable) {
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
        return listOfUsersPagedResponse;
    }

    /**
     * Removes campaign users identified by list of users userID and campaignID
     *
     * @param id Campaign ID
     * @param request List of users to remove
     * @return Response status
     */
    @RequestMapping(value = "{id}/user/delete", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "Delete users from campaign",
                  notes = "Delete users from certain campaign specified with {id} variable in URI." +
                          "Users are described as list of their ids in Request body")
    public Response deleteUsersFromCampaign(@PathVariable(value = "id") Long id, @RequestBody ObjectRequest<ListOfUsers> request) {
        ListOfUsers listOfUsers = request.getRequestObject();
        for (String user : listOfUsers) {
            pushCampaignUserRepository.deleteByCampaignIdAndUserId(id, user);
        }
        return new Response();
    }

    /**
     * Method used for checking exception about nullity of http request
     *
     * @param request An object request to check the nullity
     * @throws PushServerException In case request object is null.
     */
    private void checkRequestNullity(ObjectRequest request) throws PushServerException {
        if (request.getRequestObject() == null) {
            throw new PushServerException("Empty requestObject data");
        }
    }

    /**
     * Find push campaign entity by ID.
     * @param campaignId Campaign ID.
     * @return Push campaign entity.
     * @throws PushServerException Thrown when campaign entity does not exists.
     */
    private PushCampaignEntity findPushCampaignById(Long campaignId) throws PushServerException {
        final Optional<PushCampaignEntity> campaignEntityOptional = pushCampaignRepository.findById(campaignId);
        if (!campaignEntityOptional.isPresent()) {
            throw new PushServerException("Campaign with entered ID does not exist");
        }
        return campaignEntityOptional.get();
    }
}
