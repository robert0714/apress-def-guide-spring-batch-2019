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
import com.apress.batch.chapter9.domain.CustomerRepository;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step; 
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;  
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder; 
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;  
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty; 
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource; 
import org.springframework.transaction.PlatformTransactionManager;
/**
 * @author Michael Minella
 */
//@EnableJpaRepositories(basePackageClasses = Customer.class)
@Configuration
@ConditionalOnProperty(prefix = "main", name = "scenario", havingValue = "repositoryFormatJob")

public class RepositoryImportJob { 
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
	public RepositoryItemWriter<Customer> repositoryItemWriter(CustomerRepository repository) {
		return new RepositoryItemWriterBuilder<Customer>()
				.repository(repository)
				.methodName("save")
				.build();
	}

	@Bean
	public Step repositoryFormatStep(
			final JobRepository jobRepository,
			final PlatformTransactionManager transactionManager 
			) throws Exception {
		return new StepBuilder("repositoryFormatStep", jobRepository)
				.<Customer, Customer>chunk(10, transactionManager)
				.reader(customerFileReader(null))
				.writer(repositoryItemWriter(null))
				.build();
	}

	@Bean
	public Job repositoryFormatJob(
			final JobRepository jobRepository,
			@Qualifier("repositoryFormatStep") Step repositoryFormatStep
			) throws Exception {
		return new JobBuilder("repositoryFormatJob", jobRepository)
				.start(repositoryFormatStep )
				.build();
	}
}
