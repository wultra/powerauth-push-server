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
import io.getlime.core.rest.model.base.response.Response;
import io.getlime.push.errorhandling.exceptions.PushServerException;
import io.getlime.push.model.entity.PushMessage;
import io.getlime.push.model.enumeration.Mode;
import io.getlime.push.model.request.TestCampaignRequest;
import io.getlime.push.model.validator.TestCampaignRequestValidator;
import io.getlime.push.repository.PushCampaignRepository;
import io.getlime.push.repository.model.PushCampaignEntity;
import io.getlime.push.repository.serialization.JsonSerialization;
import io.getlime.push.service.PushMessageSenderService;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Controller class storing send campaign methods
 *
 * @author Martin Tupy, martin.tupy.work@gmail.com
 */
@RestController
@RequestMapping(value = "push/campaign/send")
public class SendCampaignController {

    private static final Logger logger = LoggerFactory.getLogger(SendCampaignController.class);

    private final JobLauncher jobLauncher;
    private final Job job;
    private final PushCampaignRepository pushCampaignRepository;
    private final PushMessageSenderService pushMessageSenderService;
    private final JsonSerialization jsonSerialization;

    /**
     * Constructor with autowired dependencies.
     * @param jobLauncher Batch job launcher.
     * @param job Job instance.
     * @param pushCampaignRepository Push campaign repository.
     * @param pushMessageSenderService Push message sender service.
     * @param jsonSerialization Helper JSON serialization class.
     */
    @Autowired
    public SendCampaignController(JobLauncher jobLauncher,
                                  Job job,
                                  PushCampaignRepository pushCampaignRepository,
                                  PushMessageSenderService pushMessageSenderService, JsonSerialization jsonSerialization) {
        this.jobLauncher = jobLauncher;
        this.job = job;
        this.pushCampaignRepository = pushCampaignRepository;
        this.pushMessageSenderService = pushMessageSenderService;
        this.jsonSerialization = jsonSerialization;
    }

    /**
     * Run sending job with campaignID and timestamp parameters.
     *
     * @param id Specific campaign ID.
     * @return Response with status.
     * @throws PushServerException In case campaign with given ID is not found.
     */
    @PostMapping(value = "live/{id}")
    @Operation(summary = "Send a campaign",
                  description = """
                          Send message from a specific campaign to devices belonged to users associated with that campaign. Whereas each device gets a campaign only once.

                          If sending was successful then sent parameter is set on true and timestampSent is set on current time.""")
    public Response sendCampaign(@PathVariable(value = "id") Long id) throws PushServerException {
        logger.info("Received sendCampaign request, campaign ID: {}", id);
        try {
            final Optional<PushCampaignEntity> campaignEntityOptional = pushCampaignRepository.findById(id);
            if (campaignEntityOptional.isEmpty()) {
                throw new PushServerException("Campaign with entered ID does not exist");
            }
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("campaignId", id)
                    .addDate("timestamp", new Date())
                    .toJobParameters();
            jobLauncher.run(job, jobParameters);
            logger.info("The sendCampaign request succeeded, campaign ID: {}", id);
            return new Response();
        } catch (JobExecutionAlreadyRunningException e) {
            throw new PushServerException("Job execution already running", e);
        } catch (JobRestartException e) {
            throw new PushServerException("Job is restarted", e);
        } catch (JobInstanceAlreadyCompleteException e) {
            throw new PushServerException("Job instance already completed", e);
        } catch (JobParametersInvalidException e) {
            throw new PushServerException("Job parameters are invalid", e);
        }
    }

    /**
     * Method for sending testing user on campaign through PushMessge sending.
     *
     * @param id Campaign ID
     * @param request Testing user ID
     * @return Response with status
     * @throws PushServerException In case request object is invalid.
     */
    @PostMapping(value = "test/{id}")
    @Operation(summary = "Send a test campaign",
                  description = "Send message from a specific campaign on test user identified in request body, userId param, to check rightness of that campaign.")
    public Response sendTestCampaign(@PathVariable(value = "id") Long id, @RequestBody ObjectRequest<TestCampaignRequest> request) throws PushServerException {
        logger.info("Received sendTestCampaign request, campaign ID: {}", id);
        final PushCampaignEntity campaign = pushCampaignRepository.findById(id).orElseThrow(() ->
                new PushServerException("Campaign with entered ID does not exist"));
        TestCampaignRequest requestedObject = request.getRequestObject();
        String errorMessage = TestCampaignRequestValidator.validate(requestedObject);
        if (errorMessage != null) {
            throw new PushServerException(errorMessage);
        }
        PushMessage pushMessage = new PushMessage();
        pushMessage.setUserId(request.getRequestObject().getUserId());
        pushMessage.setBody(jsonSerialization.deserializePushMessageBody(campaign.getMessage()));
        List<PushMessage> message = new ArrayList<>();
        message.add(pushMessage);
        pushMessageSenderService.sendPushMessage(campaign.getAppCredentials().getAppId(), Mode.SYNCHRONOUS, message);
        logger.info("The sendTestCampaign request succeeded, campaign ID: {}", id);
        return new Response();
    }
}
