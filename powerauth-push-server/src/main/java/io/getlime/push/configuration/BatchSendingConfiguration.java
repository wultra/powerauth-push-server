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

package io.getlime.push.configuration;

import io.getlime.push.repository.model.aggregate.UserDevice;
import io.getlime.push.service.batch.UserDeviceItemProcessor;
import io.getlime.push.service.batch.UserDeviceItemReader;
import io.getlime.push.service.batch.UserDeviceItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Configuration class for job used in batch sending campaign
 *
 * @author Martin Tupy, martin.tupy.work@gmail.com
 */
@Configuration
public class BatchSendingConfiguration extends DefaultBatchConfigurer {

    private final JobRepository jobRepository;
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final PushServiceConfiguration pushServiceConfiguration;
    private final UserDeviceItemReader userDeviceItemReader;
    private final UserDeviceItemProcessor userDeviceItemProcessor;
    private final UserDeviceItemWriter userDeviceItemWriter;
    private final PlatformTransactionManager transactionManager;

    @Autowired
    public BatchSendingConfiguration(JobRepository jobRepository,
                                     PlatformTransactionManager transactionManager,
                                     JobBuilderFactory jobBuilderFactory,
                                     StepBuilderFactory stepBuilderFactory,
                                     PushServiceConfiguration pushServiceConfiguration,
                                     UserDeviceItemReader userDeviceItemReader,
                                     UserDeviceItemProcessor userDeviceItemProcessor,
                                     UserDeviceItemWriter userDeviceItemWriter) {
        this.jobRepository = jobRepository;
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.pushServiceConfiguration = pushServiceConfiguration;
        this.userDeviceItemReader = userDeviceItemReader;
        this.userDeviceItemProcessor = userDeviceItemProcessor;
        this.userDeviceItemWriter = userDeviceItemWriter;
        this.transactionManager = transactionManager;
    }

    @Override
    protected JobLauncher createJobLauncher() {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        return jobLauncher;
    }

    @Bean
    public Job sendCampaignJob() {
        final TaskletStep step = buildTaskletStep();
        return jobBuilderFactory.get("SendCampaignJob")
                .incrementer(new RunIdIncrementer())
                .flow(step)
                .end()
                .build();
    }

    private TaskletStep buildTaskletStep() {
        return stepBuilderFactory.get("SendCampaignStep")
                    .<UserDevice, UserDevice>chunk(pushServiceConfiguration.getCampaignBatchSize())
                    .reader(userDeviceItemReader)
                    .processor(userDeviceItemProcessor)
                    .writer(userDeviceItemWriter)
                    .transactionManager(transactionManager)
                    .build();
    }
}
