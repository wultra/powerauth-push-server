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
import io.getlime.push.repository.PushCampaignRepository;
import io.getlime.push.repository.PushCampaignUserRepository;
import io.getlime.push.repository.model.PushCampaignEntity;
import io.getlime.push.repository.model.PushCampaignUserEntity;
import io.getlime.push.repository.serialization.JSONSerialization;
import io.getlime.push.service.PushMessageSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for push campaign related methods
 *
 * @author Martin Tupy, martin.tupy.work@gmail.com
 */

@Controller
@RequestMapping(value = "push/campaign")
public class PushCampaignController {
    private PushCampaignRepository pushCampaignRepository;
    private PushCampaignUserRepository pushCampaignUserRepository;
    private PushMessageSenderService pushMessageSenderService;

    @Autowired
    public PushCampaignController(PushCampaignRepository pushCampaignRepository, PushCampaignUserRepository pushCampaignUserRepository, PushMessageSenderService pushMessageSenderService) {
        this.pushCampaignRepository = pushCampaignRepository;
        this.pushCampaignUserRepository = pushCampaignUserRepository;
        this.pushMessageSenderService = pushMessageSenderService;
    }

    /**
     * Create a campaign for specific app.
     *
     * @param request id of specific app, body of specific messageBody
     * @return ID for created campaign.
     */
    @RequestMapping(value = "create", method = RequestMethod.POST)
    @ResponseBody
    public ObjectResponse<CreateCampaignResponse> createCampaign(@RequestBody ObjectRequest<CreateCampaignRequest> request) throws PushServerException {
        checkRequestNullity(request);
        CreateCampaignRequest requestObject = request.getRequestObject();
        Long appId = requestObject.getAppId();
        if (appId == null) {
            throw new PushServerException("Empty appId attribute");
        }
        PushCampaignEntity campaign = new PushCampaignEntity();
        campaign.setAppId(appId);
        campaign.setSent(false);
        campaign.setTimestampCreated(new Date());
        PushMessageBody message = requestObject.getMessage();
        String messageString = JSONSerialization.serializePushMessageBody(message);
        campaign.setMessage(messageString);
        campaign = pushCampaignRepository.save(campaign);
        CreateCampaignResponse response = new CreateCampaignResponse();
        response.setId(campaign.getId());
        return new ObjectResponse<>(response);
    }

    /**
     * Removes a certain campaign along with users related to this campaign.
     *
     * @param campaignId identifier for deleting a campaign
     * @return Remove campaign status response.
     */
    @RequestMapping(value = "{id}/delete", method = RequestMethod.POST)
    @ResponseBody
    public ObjectResponse<DeleteCampaignResponse> deleteCampaign(@PathVariable(value = "id") Long campaignId) {
        DeleteCampaignResponse deleteCampaignResponse = new DeleteCampaignResponse();
        if (pushCampaignRepository.findOne(campaignId) == null) {
            deleteCampaignResponse.setDeleted(false);
        } else {
            pushCampaignRepository.delete(campaignId);
            deleteCampaignResponse.setDeleted(true);
        }
        Iterable<PushCampaignUserEntity> usersFromCampaign = pushCampaignUserRepository.findAllByCampaignId(campaignId);
        for (PushCampaignUserEntity user : usersFromCampaign) {
            pushCampaignUserRepository.delete(user.getId());
        }
        return new ObjectResponse<>(deleteCampaignResponse);
    }

    /**
     * Method used for getting a list of campaigns.
     *
     * @param all If true, method returns list of all campaigns. If false, it returns
     *            only campaigns that were not sent yet.
     * @return List of campaigns.
     */
    @RequestMapping(value = "list", method = RequestMethod.GET)
    @ResponseBody
    public ObjectResponse<ListOfCampaignsResponse> getListOfCampaigns(@RequestParam(value = "all", required = false) boolean all) {
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
     * Method used for getting a specific campaign
     *
     * @param campaignId Id of specific campaign
     * @return Campaign Response
     */
    @RequestMapping(value = "{id}/detail", method = RequestMethod.GET)
    @ResponseBody
    public ObjectResponse<CampaignResponse> getCampaign(@PathVariable(value = "id") Long campaignId) throws PushServerException {
        PushCampaignEntity campaign = pushCampaignRepository.findOne(campaignId);
        if (campaign == null) {
            throw new PushServerException("Campaign with entered ID does not exist");
        }
        CampaignResponse campaignResponse = new CampaignResponse();
        campaignResponse.setId(campaign.getId());
        campaignResponse.setSent(campaign.isSent());
        campaignResponse.setAppId(campaign.getAppId());
        PushMessageBody message = JSONSerialization.deserializePushMessageBody(campaign.getMessage());
        campaignResponse.setMessage(message);
        return new ObjectResponse<>(campaignResponse);
    }

    /**
     * Add specific request to specific campaign
     *
     * @param campaignId ID of certain campaign
     * @param request    list of IDs referred to request
     * @return Response status
     */
    @RequestMapping(value = "{id}/user/add", method = RequestMethod.PUT)
    @ResponseBody
    @Transactional
    public Response addUsersToCampaign(@PathVariable(value = "id") Long campaignId, @RequestBody ObjectRequest<ListOfUsers> request) throws PushServerException {
        checkRequestNullity(request);
        final PushCampaignEntity campaignEntity = pushCampaignRepository.findOne(campaignId);
        if (campaignEntity == null) {
            throw new PushServerException("Campaign with entered ID does not exist");
        }
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
                Logger.getLogger(PushCampaignController.class.getName()).log(Level.WARNING, "Duplicate user entry for push campaign: " + user);
            }
        }
        return new Response();
    }

    /**
     * Method for getting users from specific campaign in paged format
     *
     * @param id       Campaign id
     * @param pageable Format for pagination (?page=x&size=y), where "x" is chosen page and "y" is size is number of elements per page, beginning from zero
     * @return campaign id, list of users
     */
    @RequestMapping(value = "{id}/user/list", method = RequestMethod.GET)
    @ResponseBody
    public PagedResponse<ListOfUsersFromCampaignResponse> getListOfUsersFromCampaign(@PathVariable(value = "id") Long id, Pageable pageable) {
        ListOfUsersFromCampaignResponse listOfUsersFromCampaignResponse = new ListOfUsersFromCampaignResponse();
        List<PushCampaignUserEntity> users = pushCampaignUserRepository.findAllByCampaignId(id, pageable);
        ListOfUsers listOfUsers = new ListOfUsers();
        for (PushCampaignUserEntity user : users) {
            listOfUsers.add(user.getUserId());
        }
        listOfUsersFromCampaignResponse.setCampaingId(id);
        listOfUsersFromCampaignResponse.setUsers(listOfUsers);
        PagedResponse<ListOfUsersFromCampaignResponse> pagedResponse = new PagedResponse<>(listOfUsersFromCampaignResponse);
        pagedResponse.setPage(pageable.getPageNumber());
        pagedResponse.setSize(pageable.getPageSize());
        return pagedResponse;
    }

    /**
     * Removes campaign users identified by list of users userID and campaignID
     *
     * @param id      Campaign ID
     * @param request List of users to remove
     * @return Response status
     */
    @RequestMapping(value = "{id}/user/delete", method = RequestMethod.POST)
    @ResponseBody
    public Response deleteUsersFromCampaign(@PathVariable(value = "id") Long id, @RequestBody ObjectRequest<ListOfUsers> request) {
        Iterable<PushCampaignUserEntity> listOfUsersFromCampaign = pushCampaignUserRepository.findAllByCampaignId(id);
        ListOfUsers listOfUsers = request.getRequestObject();
        for (PushCampaignUserEntity userFromCampaign : listOfUsersFromCampaign) {
            for (String user : listOfUsers) {
                if (user.equals(userFromCampaign.getUserId())) {
                    pushCampaignUserRepository.delete(userFromCampaign.getId());
                }
            }
        }
        return new Response();
    }

    /**
     * Method used for checking exception about nullity of http request
     *
     * @param request an object request to check the nullity
     * @throws PushServerException defined by certain message
     */
    private void checkRequestNullity(ObjectRequest request) throws PushServerException {
        if (request.getRequestObject() == null) {
            throw new PushServerException("Empty requestObject data");
        }
    }


}
