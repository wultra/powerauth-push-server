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

package io.getlime.push.configuration;

import io.getlime.push.repository.model.aggregate.UserDevice;
import io.getlime.push.service.batch.SendCampaignJobListener;
import io.getlime.push.service.batch.UserDeviceItemProcessor;
import io.getlime.push.service.batch.UserDeviceItemReader;
import io.getlime.push.service.batch.UserDeviceItemWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.transaction.TransactionManager;

import javax.sql.DataSource;

/**
 * Configuration class for job used in batch sending campaign
 *
 * @author Petr Dvorak, petr@wultra.com
 * @author Martin Tupy, martin.tupy.work@gmail.com
 */
@Configuration
public class BatchSendingConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(BatchSendingConfiguration.class);

    private final PushServiceConfiguration pushServiceConfiguration;
    private final UserDeviceItemReader userDeviceItemReader;
    private final UserDeviceItemProcessor userDeviceItemProcessor;
    private final UserDeviceItemWriter userDeviceItemWriter;
    private final SendCampaignJobListener sendCampaignJobListener;

    private DataSource dataSource;
    private final EntityManagerFactory entityManagerFactory;
    private JobRepository jobRepository;
    private PlatformTransactionManager transactionManager;

    /**
     * Constructor with autowired dependencies.
     * @param pushServiceConfiguration Push service configuration.
     * @param userDeviceItemReader Step user device item reader.
     * @param userDeviceItemProcessor Step user device item processor.
     * @param userDeviceItemWriter Step user device item writer.
     * @param sendCampaignJobListener Batch job listener.
     * @param entityManagerFactory Entity manager factory.
     */
    @Autowired
    public BatchSendingConfiguration(PushServiceConfiguration pushServiceConfiguration,
                                     UserDeviceItemReader userDeviceItemReader,
                                     UserDeviceItemProcessor userDeviceItemProcessor,
                                     UserDeviceItemWriter userDeviceItemWriter,
                                     SendCampaignJobListener sendCampaignJobListener,
                                     EntityManagerFactory entityManagerFactory) {
        this.pushServiceConfiguration = pushServiceConfiguration;
        this.userDeviceItemReader = userDeviceItemReader;
        this.userDeviceItemProcessor = userDeviceItemProcessor;
        this.userDeviceItemWriter = userDeviceItemWriter;
        this.sendCampaignJobListener = sendCampaignJobListener;
        this.entityManagerFactory = entityManagerFactory;
    }

    private TaskletStep buildTaskletStep() {
        return new StepBuilder("SendCampaignStep", jobRepository)
                .<UserDevice, UserDevice>chunk(pushServiceConfiguration.getCampaignBatchSize(), transactionManager)
                .reader(userDeviceItemReader)
                .processor(userDeviceItemProcessor)
                .writer(userDeviceItemWriter)
                .build();
    }

    /**
     * Bean producer for Tasklet with sending the campaign.
     * @return Job.
     */
    @Bean
    public Job sendCampaignJob() {
        final TaskletStep step = buildTaskletStep();
        return new JobBuilder("SendCampaignJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .flow(step)
                .end()
                .listener(sendCampaignJobListener)
                .build();
    }

    /**
     * Bean producer for TransactionManager.
     * @return Transaction manager.
     */
    @Bean
    public TransactionManager transactionManager() {
        return transactionManager;
    }

    /**
     * Autowire data source with setter injection.
     * @param dataSource Data source.
     */
    @Autowired(required = false)
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Initialize the beans.
     */
    @PostConstruct
    public void initialize() {
        try {
            this.transactionManager = createTransactionManager();
            this.jobRepository = createJobRepository();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private PlatformTransactionManager createTransactionManager() {
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setDataSource(dataSource);
        transactionManager.setJpaDialect(new HibernateJpaDialect());
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
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
