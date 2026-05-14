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

package com.wultra.push.controller.rest;

import com.wultra.core.rest.model.base.request.ObjectRequest;
import com.wultra.core.rest.model.base.response.Response;
import com.wultra.push.errorhandling.exceptions.PushServerException;
import com.wultra.push.model.entity.PushMessage;
import com.wultra.push.model.enumeration.Mode;
import com.wultra.push.model.request.TestCampaignRequest;
import com.wultra.push.model.validator.TestCampaignRequestValidator;
import com.wultra.push.repository.PushCampaignRepository;
import com.wultra.push.repository.model.PushCampaignEntity;
import com.wultra.push.repository.serialization.JsonSerialization;
import com.wultra.push.service.PushMessageSenderService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.InvalidJobParametersException;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Controller class storing send campaign methods
 *
 * @author Martin Tupy, martin.tupy.work@gmail.com
 */
@Slf4j
@RestController
@RequestMapping(value = "push/campaign/send")
public class SendCampaignController {

    private final JobOperator jobOperator;
    private final Job job;
    private final PushCampaignRepository pushCampaignRepository;
    private final PushMessageSenderService pushMessageSenderService;
    private final JsonSerialization jsonSerialization;

    /**
     * Constructor with autowired dependencies.
     * @param jobOperator Batch job operator (Spring Batch 6 replacement for {@code JobLauncher}).
     * @param job Job instance.
     * @param pushCampaignRepository Push campaign repository.
     * @param pushMessageSenderService Push message sender service.
     * @param jsonSerialization Helper JSON serialization class.
     */
    @Autowired
    public SendCampaignController(JobOperator jobOperator,
                                  Job job,
                                  PushCampaignRepository pushCampaignRepository,
                                  PushMessageSenderService pushMessageSenderService, JsonSerialization jsonSerialization) {
        this.jobOperator = jobOperator;
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
        logger.info("action: sendCampaign, state: initiated, campaignId: {}", id);
        try {
            final Optional<PushCampaignEntity> campaignEntityOptional = pushCampaignRepository.findById(id);
            if (campaignEntityOptional.isEmpty()) {
                logger.warn("action: sendCampaign, state: failed, error: Campaign not found");
                throw new PushServerException("Campaign with entered ID does not exist");
            }
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("campaignId", id)
                    .addDate("timestamp", new Date())
                    .toJobParameters();
            jobOperator.start(job, jobParameters);
            logger.info("action: sendCampaign, state: succeeded");
            return new Response();
        } catch (JobExecutionAlreadyRunningException e) {
            logger.error("action: sendCampaign, state: failed, error: {}", e.getMessage());
            throw new PushServerException("Job execution already running", e);
        } catch (JobRestartException e) {
            logger.error("action: sendCampaign, state: failed, error: {}", e.getMessage());
            throw new PushServerException("Job is restarted", e);
        } catch (JobInstanceAlreadyCompleteException e) {
            logger.error("action: sendCampaign, state: failed, error: {}", e.getMessage());
            throw new PushServerException("Job instance already completed", e);
        } catch (InvalidJobParametersException e) {
            logger.error("action: sendCampaign, state: failed, error: {}", e.getMessage());
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
    public Response sendTestCampaign(@PathVariable(value = "id") Long id, @Valid @RequestBody ObjectRequest<TestCampaignRequest> request) throws PushServerException {
        final TestCampaignRequest requestedObject = request.getRequestObject();
        logger.info("action: sendTestCampaign, state: initiated, campaignId: {}, userId: {}", id, requestedObject.getUserId());
        final PushCampaignEntity campaign = pushCampaignRepository.findById(id).orElseThrow(() -> {
            logger.error("action: sendTestCampaign, state: failed, error: Campaign with entered ID does not exist");
            return new PushServerException("Campaign with entered ID does not exist");
        });
        String errorMessage = TestCampaignRequestValidator.validate(requestedObject);
        if (errorMessage != null) {
            logger.error("action: sendTestCampaign, state: failed, error: {}", errorMessage);
            throw new PushServerException(errorMessage);
        }
        PushMessage pushMessage = new PushMessage();
        pushMessage.setUserId(requestedObject.getUserId());
        pushMessage.setBody(jsonSerialization.deserializePushMessageBody(campaign.getMessage()));
        List<PushMessage> message = new ArrayList<>();
        message.add(pushMessage);
        pushMessageSenderService.sendPushMessage(campaign.getAppCredentials().getAppId(), Mode.SYNCHRONOUS, message);
        logger.info("action: sendTestCampaign, state: succeeded");
        return new Response();
    }
}
