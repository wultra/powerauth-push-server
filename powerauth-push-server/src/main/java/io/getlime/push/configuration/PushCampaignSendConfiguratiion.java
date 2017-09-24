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

import io.getlime.push.repository.model.PushCampaignUserEntity;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.FieldExtractor;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import javax.persistence.EntityManagerFactory;
/**
 * Configuration class for job used in batch sending campaign
 *
 * @author Martin Tupy, martin.tupy.work@gmail.com
 */

@Configuration
public class PushCampaignSendConfiguratiion {
    @Autowired
    EntityManagerFactory entityManagerFactory;

    @Autowired
    JobBuilderFactory jobBuilderFactory;

    @Autowired
    StepBuilderFactory stepBuilderFactory;

    @Bean
    ItemReader<PushCampaignUserEntity> campaignDeviceReader() {

        JpaPagingItemReader<PushCampaignUserEntity> databaseReader = new JpaPagingItemReader<>();
        databaseReader.setEntityManagerFactory(entityManagerFactory);
        databaseReader.setQueryString("select c from PushCampaignUserEntity c");
        System.out.println(databaseReader.getPageSize());

        return databaseReader;
    }

    @Bean
    ItemWriter<PushCampaignUserEntity> campaignDeviceWriter() {
        FlatFileItemWriter<PushCampaignUserEntity> csvFileWriter = new FlatFileItemWriter<>();


        csvFileWriter.setResource(new FileSystemResource("C:\\Users\\Marty\\Documents\\cities.txt"));

        LineAggregator<PushCampaignUserEntity> lineAggregator = createStudentLineAggregator();
        csvFileWriter.setLineAggregator(lineAggregator);

        return csvFileWriter;
    }

    private LineAggregator<PushCampaignUserEntity> createStudentLineAggregator() {
        DelimitedLineAggregator<PushCampaignUserEntity> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(";");

        FieldExtractor<PushCampaignUserEntity> fieldExtractor = createStudentFieldExtractor();
        lineAggregator.setFieldExtractor(fieldExtractor);

        return lineAggregator;
    }

    private FieldExtractor<PushCampaignUserEntity> createStudentFieldExtractor() {
        BeanWrapperFieldExtractor<PushCampaignUserEntity> extractor = new BeanWrapperFieldExtractor<>();
        extractor.setNames(new String[] {"id", "userId"});
        return extractor;
    }

    @Bean
    public Job sendCampaignJob(){
        Job job = jobBuilderFactory.get("sendCampaignJob")
                .incrementer(new RunIdIncrementer())
                .flow(
                        stepBuilderFactory.get("sendCampaignStep")
                                .<PushCampaignUserEntity,PushCampaignUserEntity>chunk(10000)
                                .reader(campaignDeviceReader())
                                .writer(campaignDeviceWriter())
                                .build())
                .end()
                .build();
        return job;
    }

}
