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
import org.neo4j.ogm.session.SessionFactory; 
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step; 
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.extensions.neo4j.Neo4jItemWriter;
import org.springframework.batch.extensions.neo4j.builder.Neo4jItemWriterBuilder; 
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
@Configuration
@ConditionalOnProperty(prefix = "main", name = "scenario", havingValue = "neo4jFormatJob")
public class Neo4jImportJob { 
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
	public Neo4jItemWriter<Customer> neo4jItemWriter(SessionFactory sessionFactory) {
		return new Neo4jItemWriterBuilder<Customer>()
				.sessionFactory(sessionFactory)
				.build();
	}

	@Bean
	public Step neo4jFormatStep(
			final JobRepository jobRepository,
			final PlatformTransactionManager transactionManager 
			) throws Exception {
		return new StepBuilder("neo4jFormatStep", jobRepository)
				.<Customer, Customer>chunk(10, transactionManager)
				.reader(customerFileReader(null))
				.writer(neo4jItemWriter(null))
				.build();
	}

	@Bean
	public Job neo4jFormatJob(
			final JobRepository jobRepository,
			@Qualifier("neo4jFormatStep") Step neo4jFormatStep
			) throws Exception {
		return  new JobBuilder("neo4jFormatJob", jobRepository)
				.start(neo4jFormatStep )
				.build();
	}
}
