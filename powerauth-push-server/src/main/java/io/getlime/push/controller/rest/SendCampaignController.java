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

import io.getlime.core.rest.model.base.response.Response;
import io.getlime.push.errorhandling.exceptions.PushServerException;
import io.getlime.push.repository.PushCampaignRepository;
import io.getlime.push.repository.model.PushCampaignEntity;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.Date;

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

    @Autowired
    public SendCampaignController(JobLauncher jobLauncher, Job job, PushCampaignRepository pushCampaignRepository) {
        this.job = job;
        this.jobLauncher = jobLauncher;
        this.pushCampaignRepository = pushCampaignRepository;
    }

    @RequestMapping(value = "{id}", method = RequestMethod.POST)
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
}
