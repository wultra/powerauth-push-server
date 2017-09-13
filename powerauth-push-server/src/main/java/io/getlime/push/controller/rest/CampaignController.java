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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.core.rest.model.base.response.Response;
import io.getlime.push.errorhandling.exceptions.PushServerException;
import io.getlime.push.model.base.PagedResponse;
import io.getlime.push.model.entity.ListOfUsers;
import io.getlime.push.model.entity.PushMessage;
import io.getlime.push.model.entity.PushMessageBody;
import io.getlime.push.model.request.CreateCampaignRequest;
import io.getlime.push.model.request.TestCampaignRequest;
import io.getlime.push.model.response.*;
import io.getlime.push.repository.PushCampaignRepository;
import io.getlime.push.repository.PushCampaignUsersRepository;
import io.getlime.push.repository.model.PushCampaign;
import io.getlime.push.repository.model.PushCampaignUser;
import io.getlime.push.service.PushSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.ArrayList;
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
public class CampaignController {
    private PushCampaignRepository pushCampaignRepository;
    private PushCampaignUsersRepository pushCampaignUsersRepository;
    private PushSenderService pushSenderService;

    @Autowired
    public CampaignController(PushCampaignRepository pushCampaignRepository, PushCampaignUsersRepository pushCampaignUsersRepository, PushSenderService pushSenderService) {
        this.pushCampaignRepository = pushCampaignRepository;
        this.pushCampaignUsersRepository = pushCampaignUsersRepository;
        this.pushSenderService = pushSenderService;
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
        PushCampaign campaign = new PushCampaign();
        campaign.setAppId(appId);
        campaign.setSent(false);
        campaign.setTimestampCreated(new Date());
        PushMessageBody message = requestObject.getMessage();
        String messageString = serializePushMessageBody(message);
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
        Iterable<PushCampaignUser> usersFromCampaign = pushCampaignUsersRepository.findAllByCampaignId(campaignId);
        for (PushCampaignUser user : usersFromCampaign) {
            pushCampaignUsersRepository.delete(user.getId());
        }
        return new ObjectResponse<>(deleteCampaignResponse);
    }

    /**
     * Method used for getting a list of campaigns
     *
     * @param all true - list of all campaigns
     *            false - list of campaigns which have 'all' attribute set on false
     * @return List of campaigns.
     */
    @RequestMapping(value = "list", method = RequestMethod.GET)
    @ResponseBody
    public ObjectResponse<ListOfCampaignResponse> getListOfCampaigns(@RequestParam(value = "all") boolean all) {
        Iterable<PushCampaign> campaignList;
        ListOfCampaignResponse listOfCampaignResponse = new ListOfCampaignResponse();
        if (all) {
            campaignList = pushCampaignRepository.findAll();
        } else {
            campaignList = pushCampaignRepository.findAllBySent(false);
        }
        for (PushCampaign campaign : campaignList) {
            CampaignResponse campaignResponse = new CampaignResponse();
            campaignResponse.setId(campaign.getId());
            campaignResponse.setAppId(campaign.getAppId());
            campaignResponse.setSent(campaign.isSent());
            PushMessageBody pushMessageBody = deserializePushMessageBody(campaign.getMessage());
            campaignResponse.setMessage(pushMessageBody);
            listOfCampaignResponse.add(campaignResponse);
        }
        return new ObjectResponse<>(listOfCampaignResponse);
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
        PushCampaign campaign = pushCampaignRepository.findOne(campaignId);
        if (campaign == null) {
            throw new PushServerException("Campaign with entered ID does not exist");
        }
        CampaignResponse campaignResponse = new CampaignResponse();
        campaignResponse.setId(campaign.getId());
        campaignResponse.setSent(campaign.isSent());
        campaignResponse.setAppId(campaign.getAppId());
        PushMessageBody message = deserializePushMessageBody(campaign.getMessage());
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
        if (pushCampaignRepository.findOne(campaignId) == null) {
            throw new PushServerException("Campaign with entered ID does not exist");
        }
        ListOfUsers listOfUsers = request.getRequestObject();
        for (String user : listOfUsers) {
            if (pushCampaignUsersRepository.findFirstByUserIdAndCampaignId(user, campaignId) == null) {
                PushCampaignUser pushCampaignUser = new PushCampaignUser();
                pushCampaignUser.setCampaignId(campaignId);
                pushCampaignUser.setUserId(user);
                pushCampaignUser.setTimestampAdded(new Date());
                pushCampaignUsersRepository.save(pushCampaignUser);
            } else {
                Logger.getLogger(CampaignController.class.getName()).log(Level.WARNING, "Duplicate user entry for push campaign: " + user);
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
        List<PushCampaignUser> users = pushCampaignUsersRepository.findAllByCampaignId(id, pageable);
        ListOfUsers listOfUsers = new ListOfUsers();
        for (PushCampaignUser user : users) {
            listOfUsers.add(user.getUserId());
        }
        listOfUsersFromCampaignResponse.setCampaingId(id);
        listOfUsersFromCampaignResponse.setUsers(listOfUsers);
        PagedResponse<ListOfUsersFromCampaignResponse> pagedResponse = new PagedResponse<>(listOfUsersFromCampaignResponse);
        pagedResponse.setPage(pageable.getPageSize());
        pagedResponse.setSize(pageable.getPageNumber());
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
        Iterable<PushCampaignUser> listOfUsersFromCampaign = pushCampaignUsersRepository.findAllByCampaignId(id);
        ListOfUsers listOfUsers = request.getRequestObject();
        for (PushCampaignUser userFromCampaign : listOfUsersFromCampaign) {
            for (String user : listOfUsers) {
                if (user.equals(userFromCampaign.getUserId())) {
                    pushCampaignUsersRepository.delete(userFromCampaign.getId());
                }
            }
        }
        return new Response();
    }

    /**
     * Method for sending testing user on campaign
     *
     * @param id      Campaign ID
     * @param request User ID
     * @return Response status
     */
    @RequestMapping(value = "{id}/test/send", method = RequestMethod.POST)
    @ResponseBody
    public Response sendTestCampaign(@PathVariable(value = "id") Long id, @RequestBody ObjectRequest<TestCampaignRequest> request) throws PushServerException {
        checkRequestNullity(request);
        PushCampaign campaign = pushCampaignRepository.findOne(id);
        if (campaign == null) {
            throw new PushServerException("Campaign with entered id does not exist");
        }
        PushMessage pushMessage = new PushMessage();
        pushMessage.setUserId(request.getRequestObject().getUserId());
        pushMessage.setMessage(deserializePushMessageBody(campaign.getMessage()));
        List<PushMessage> message = new ArrayList<>();
        message.add(pushMessage);
        try {
            pushSenderService.send(campaign.getAppId(), message);
        } catch (InterruptedException | IOException e) {
            throw new PushServerException(e.getMessage());
        }
        return new Response();
    }

    @RequestMapping(value = "{id}/send", method = RequestMethod.POST)
    @ResponseBody
    public Response sendCampaign(@PathVariable(value = "id") Long id) {
        return null;
    }

    private PushMessageBody deserializePushMessageBody(String message) {
        PushMessageBody pushMessageBody = null;
        try {
            pushMessageBody = new ObjectMapper().readValue(message, PushMessageBody.class);
        } catch (IOException e) {
            Logger.getLogger(CampaignController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        }
        return pushMessageBody;
    }

    /**
     * Method used for parsing message into Json
     *
     * @param message string message
     * @return Json
     */
    private String serializePushMessageBody(PushMessageBody message) {
        String messageString = null;
        try {
            messageString = new ObjectMapper().writeValueAsString(message);
        } catch (JsonProcessingException e) {
            Logger.getLogger(CampaignController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        }
        return messageString;
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
