/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.apress.batch.chapter9.configuration; 
import com.apress.batch.chapter9.domain.Customer;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step; 
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;  
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder; 
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty; 
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;  
import org.springframework.transaction.PlatformTransactionManager;

/**
 * @author Michael Minella
 */
@Configuration
@ConditionalOnProperty(prefix = "main", name = "scenario", havingValue = "formatJob")
public class FormattedTextFileJob { 
	@Bean
	@StepScope
	public FlatFileItemReader<Customer> customerFileReader(
			@Value("#{jobParameters['customerFile']}")Resource inputFile) {

		return new FlatFileItemReaderBuilder<Customer>()
				.name("customerFileReader")
				.resource(inputFile)
				.delimited()
				.names(new String[] {"firstName",
						"middleInitial",
						"lastName",
						"address",
						"city",
						"state",
						"zip"})
				.targetType(Customer.class)
				.build();
	}

	@Bean
	@StepScope
	public FlatFileItemWriter<Customer> customerItemWriter(
			@Value("#{jobParameters['outputFile']}") WritableResource outputFile) {

//		BeanWrapperFieldExtractor<Customer> fieldExtractor = new BeanWrapperFieldExtractor<>();
//
//		fieldExtractor.setNames(new String[] {"firstName", "lastName", "address", "city", "state", "zip"});
//
//		fieldExtractor.afterPropertiesSet();
//
//		FormatterLineAggregator<Customer> lineAggregator = new FormatterLineAggregator<>();
//
//		lineAggregator.setFieldExtractor(fieldExtractor);
//		lineAggregator.setFormat("%s %s lives at %s %s in %s, %s.");

		return new FlatFileItemWriterBuilder<Customer>()
				.name("customerItemWriter")
				.resource(outputFile)
				.formatted()
				.format("%s %s lives at %s %s in %s, %s.")
				.names(new String[] {"firstName", "lastName", "address", "city", "state", "zip"})
//				.lineAggregator(lineAggregator)
				.build();
	}

	@Bean
	public Step formatStep(
			final JobRepository jobRepository,
			final PlatformTransactionManager transactionManager 
			) {
		return new StepBuilder("formatStep", jobRepository)
				.<Customer, Customer>chunk(10, transactionManager)
				.reader(customerFileReader(null))
				.writer(customerItemWriter(null))
				.build();
	}

	@Bean
	public Job formatJob(
			final JobRepository jobRepository,
			@Qualifier("formatStep") Step formatStep
			) {
		return new JobBuilder("formatJob", jobRepository)
				.start(formatStep)
				.build();
	}
}
