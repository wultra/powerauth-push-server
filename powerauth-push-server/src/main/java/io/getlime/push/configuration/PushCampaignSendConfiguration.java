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
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.FieldExtractor;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

/**
 * Configuration class for job used in batch sending campaign
 *
 * @author Martin Tupy, martin.tupy.work@gmail.com
 */
@Configuration
public class PushCampaignSendConfiguration extends DefaultBatchConfigurer {

    private final JobRepository jobRepository;
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final PushServiceConfiguration pushServiceConfiguration;
    private final UserDeviceItemReader userDeviceItemReader;
    private final UserDeviceItemProcessor userDeviceItemProcessor;

    @Autowired
    public PushCampaignSendConfiguration(JobRepository jobRepository, JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, PushServiceConfiguration pushServiceConfiguration, UserDeviceItemReader userDeviceItemReader, UserDeviceItemProcessor userDeviceItemProcessor) {
        this.jobRepository = jobRepository;
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.pushServiceConfiguration = pushServiceConfiguration;
        this.userDeviceItemReader = userDeviceItemReader;
        this.userDeviceItemProcessor = userDeviceItemProcessor;
    }

    @Override
    protected JobLauncher createJobLauncher() throws Exception {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        return jobLauncher;
    }

    @Bean
    ItemWriter<UserDevice> campaignDeviceWriter() {
        FlatFileItemWriter<UserDevice> csvFileWriter = new FlatFileItemWriter<>();
        csvFileWriter.setResource(new FileSystemResource("/Users/petrdvorak/result.txt"));
        LineAggregator<UserDevice> lineAggregator = createStudentLineAggregator();
        csvFileWriter.setLineAggregator(lineAggregator);
        return csvFileWriter;
    }

    private LineAggregator<UserDevice> createStudentLineAggregator() {
        DelimitedLineAggregator<UserDevice> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(";");
        FieldExtractor<UserDevice> fieldExtractor = createStudentFieldExtractor();
        lineAggregator.setFieldExtractor(fieldExtractor);
        return lineAggregator;
    }

    private FieldExtractor<UserDevice> createStudentFieldExtractor() {
        BeanWrapperFieldExtractor<UserDevice> extractor = new BeanWrapperFieldExtractor<>();
        extractor.setNames(new String[] {"campaignId", "userId"});
        return extractor;
    }

    @Bean
    public Job sendCampaignJob(){
        return jobBuilderFactory.get("sendCampaignJob")
                .incrementer(new RunIdIncrementer())
                .flow(
                        stepBuilderFactory.get("sendCampaignStep")
                                .<UserDevice,UserDevice>chunk(pushServiceConfiguration.getCampaignBatchSize())
                                .reader(userDeviceItemReader)
                                .processor(userDeviceItemProcessor)
                                .writer(campaignDeviceWriter())
                                .build())
                .end()
                .build();
    }

}
