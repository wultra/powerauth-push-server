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

package com.wultra.push.configuration;

import com.wultra.push.repository.model.aggregate.UserDevice;
import com.wultra.push.service.batch.SendCampaignJobListener;
import com.wultra.push.service.batch.UserDeviceItemProcessor;
import com.wultra.push.service.batch.UserDeviceItemReader;
import com.wultra.push.service.batch.UserDeviceItemWriter;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Configuration class for job used in batch sending campaign
 *
 * @author Petr Dvorak, petr@wultra.com
 * @author Martin Tupy, martin.tupy.work@gmail.com
 */
@Configuration
public class BatchSendingConfiguration {

    /**
     * Bean producer for Tasklet with sending the campaign.
     * @param jobRepository Job repository.
     * @param transactionManager Transaction manager.
     * @param pushServiceConfiguration Push service configuration.
     * @param userDeviceItemReader Step user device item reader.
     * @param userDeviceItemProcessor Step user device item processor.
     * @param userDeviceItemWriter Step user device item writer.
     * @param sendCampaignJobListener Batch job listener.
     * @return Job.
     */
    @Bean
    public Job sendCampaignJob(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager,
                               PushServiceConfiguration pushServiceConfiguration,
                               UserDeviceItemReader userDeviceItemReader,
                               UserDeviceItemProcessor userDeviceItemProcessor,
                               UserDeviceItemWriter userDeviceItemWriter,
                               SendCampaignJobListener sendCampaignJobListener) {
        final Step step = new StepBuilder("SendCampaignStep", jobRepository)
                .<UserDevice, UserDevice>chunk(pushServiceConfiguration.getCampaignBatchSize())
                .transactionManager(transactionManager)
                .reader(userDeviceItemReader)
                .processor(userDeviceItemProcessor)
                .writer(userDeviceItemWriter)
                .build();
        return new JobBuilder("SendCampaignJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .flow(step)
                .end()
                .listener(sendCampaignJobListener)
                .build();
    }


}
