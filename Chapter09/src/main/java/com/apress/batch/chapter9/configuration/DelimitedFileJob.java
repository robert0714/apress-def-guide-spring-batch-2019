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
import org.springframework.batch.core.launch.support.RunIdIncrementer;
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
@ConditionalOnProperty(prefix = "main", name = "scenario", havingValue = "delimitedJob")
public class DelimitedFileJob {
 
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

		return new FlatFileItemWriterBuilder<Customer>()
				.name("customerItemWriter")
				.resource(outputFile)
				.delimited()
				.delimiter(";")
				.names(new String[] {"zip",
						"state",
						"city",
						"address",
						"lastName",
						"firstName"})
				.append(true)
				.build();
	}

	@Bean
	public Step delimitedStep(
			final JobRepository jobRepository,
			final PlatformTransactionManager transactionManager 
			) {
		return  new StepBuilder("delimitedStep", jobRepository)
				.<Customer, Customer>chunk(10, transactionManager)
				.reader(customerFileReader(null))
				.writer(customerItemWriter(null))
				.build();
	}

	@Bean
	public Job delimitedJob(
			final JobRepository jobRepository,
			@Qualifier("delimitedStep") Step delimitedStep
			
			) {
		return new JobBuilder("delimitedJob", jobRepository)
				.start(delimitedStep)
				.incrementer(new RunIdIncrementer())
				.build();
	}
}
