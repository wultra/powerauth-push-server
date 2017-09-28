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
import io.getlime.core.rest.model.base.response.Response;
import io.getlime.push.errorhandling.exceptions.PushServerException;
import io.getlime.push.model.entity.PushMessage;
import io.getlime.push.model.request.TestCampaignRequest;
import io.getlime.push.repository.PushCampaignRepository;
import io.getlime.push.repository.model.PushCampaignEntity;
import io.getlime.push.repository.serialization.JSONSerialization;
import io.getlime.push.service.PushMessageSenderService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Controller class storing send campaign methods
 *
 * @author Martin Tupy, martin.tupy.work@gmail.com
 */
@Controller
@RequestMapping(value = "push/campaign/send")
public class SendCampaignController {

    private final JobLauncher jobLauncher;
    private final Job job;
    private final PushCampaignRepository pushCampaignRepository;
    private final PushMessageSenderService pushMessageSenderService;

    @Autowired
    public SendCampaignController(JobLauncher jobLauncher, Job job, PushCampaignRepository pushCampaignRepository, PushMessageSenderService pushMessageSenderService) {
        this.job = job;
        this.jobLauncher = jobLauncher;
        this.pushCampaignRepository = pushCampaignRepository;
        this.pushMessageSenderService = pushMessageSenderService;
    }

    @RequestMapping(value = "live/{id}", method = RequestMethod.POST)
    @ResponseBody
    public Response sendCampaign(@PathVariable(value = "id") Long id) throws PushServerException {
        try {
            PushCampaignEntity campaign = pushCampaignRepository.findOne(id);
            if (campaign == null) {
                throw new PushServerException("Campaign with entered id does not exist");
            }
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("campaignId", id)
                    .addDate("timestamp", new Date())
                    .toJobParameters();
            jobLauncher.run(job, jobParameters);
            return new Response();
        } catch (JobExecutionAlreadyRunningException e) {
            throw new PushServerException("Job execution already running");
        } catch (JobRestartException e) {
            throw new PushServerException("Job is restarted");
        } catch (JobInstanceAlreadyCompleteException e) {
            throw new PushServerException("Job instance already completed");
        } catch (JobParametersInvalidException e) {
            throw new PushServerException("Job parameters are invalid");
        }
    }

    /**
     * Method for sending testing user on campaign
     *
     * @param id      Campaign ID
     * @param request User ID
     * @return Response status
     */
    @RequestMapping(value = "test/{id}", method = RequestMethod.POST)
    @ResponseBody
    public Response sendTestCampaign(@PathVariable(value = "id") Long id, @RequestBody ObjectRequest<TestCampaignRequest> request) throws PushServerException {
        PushCampaignEntity campaign = pushCampaignRepository.findOne(id);
        if (campaign == null) {
            throw new PushServerException("Campaign with entered id does not exist");
        }
        PushMessage pushMessage = new PushMessage();
        pushMessage.setUserId(request.getRequestObject().getUserId());
        pushMessage.setBody(JSONSerialization.deserializePushMessageBody(campaign.getMessage()));
        List<PushMessage> message = new ArrayList<>();
        message.add(pushMessage);
        try {
            pushMessageSenderService.send(campaign.getAppId(), message);
        } catch (InterruptedException | IOException e) {
            throw new PushServerException(e.getMessage());
        }
        return new Response();
    }
}
