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
import io.getlime.push.service.batch.SendCampaignJobListener;
import io.getlime.push.service.batch.UserDeviceItemProcessor;
import io.getlime.push.service.batch.UserDeviceItemReader;
import io.getlime.push.service.batch.UserDeviceItemWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * Configuration class for job used in batch sending campaign
 *
 * @author Petr Dvorak, petr@lime-company.eu
 * @author Martin Tupy, martin.tupy.work@gmail.com
 */
@Configuration
public class BatchSendingConfiguration implements BatchConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(BatchSendingConfiguration.class);

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final PushServiceConfiguration pushServiceConfiguration;
    private final UserDeviceItemReader userDeviceItemReader;
    private final UserDeviceItemProcessor userDeviceItemProcessor;
    private final UserDeviceItemWriter userDeviceItemWriter;
    private final SendCampaignJobListener sendCampaignJobListener;

    private DataSource dataSource;
    private EntityManagerFactory entityManagerFactory;
    private PlatformTransactionManager transactionManager;
    private JobRepository jobRepository;
    private JobLauncher jobLauncher;
    private JobExplorer jobExplorer;

    @Autowired
    public BatchSendingConfiguration(JobBuilderFactory jobBuilderFactory,
                                     StepBuilderFactory stepBuilderFactory,
                                     PushServiceConfiguration pushServiceConfiguration,
                                     UserDeviceItemReader userDeviceItemReader,
                                     UserDeviceItemProcessor userDeviceItemProcessor,
                                     UserDeviceItemWriter userDeviceItemWriter,
                                     SendCampaignJobListener sendCampaignJobListener,
                                     EntityManagerFactory entityManagerFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.pushServiceConfiguration = pushServiceConfiguration;
        this.userDeviceItemReader = userDeviceItemReader;
        this.userDeviceItemProcessor = userDeviceItemProcessor;
        this.userDeviceItemWriter = userDeviceItemWriter;
        this.sendCampaignJobListener = sendCampaignJobListener;
        this.entityManagerFactory = entityManagerFactory;
    }

    private TaskletStep buildTaskletStep() {
        return stepBuilderFactory.get("SendCampaignStep")
                .<UserDevice, UserDevice>chunk(pushServiceConfiguration.getCampaignBatchSize())
                .reader(userDeviceItemReader)
                .processor(userDeviceItemProcessor)
                .writer(userDeviceItemWriter)
                .build();
    }

    @Bean
    public Job sendCampaignJob() {
        final TaskletStep step = buildTaskletStep();
        return jobBuilderFactory.get("SendCampaignJob")
                .incrementer(new RunIdIncrementer())
                .flow(step)
                .end()
                .listener(sendCampaignJobListener)
                .build();
    }

    @Autowired(required = false)
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        this.transactionManager = createTransactionManager();
    }

    @Override
    public JobRepository getJobRepository() {
        return jobRepository;
    }

    @Override
    public PlatformTransactionManager getTransactionManager() {
        return transactionManager;
    }

    @Override
    public JobLauncher getJobLauncher() {
        return jobLauncher;
    }

    @Override
    public JobExplorer getJobExplorer() {
        return jobExplorer;
    }

    @PostConstruct
    public void initialize() {
        try {
            this.jobRepository = createJobRepository();
            this.jobExplorer = createJobExplorer();
            this.jobLauncher = createJobLauncher();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private PlatformTransactionManager createTransactionManager() {
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }

    private JobExplorer createJobExplorer() throws Exception {
        JobExplorerFactoryBean jobExplorerFactoryBean = new JobExplorerFactoryBean();
        jobExplorerFactoryBean.setDataSource(this.dataSource);
        jobExplorerFactoryBean.afterPropertiesSet();
        return jobExplorerFactoryBean.getObject();
    }

    private JobLauncher createJobLauncher() throws Exception {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }

    private JobRepository createJobRepository() throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTransactionManager(transactionManager);
        factory.setIsolationLevelForCreate("ISOLATION_DEFAULT");
        factory.afterPropertiesSet();
        return  factory.getObject();
    }

}
