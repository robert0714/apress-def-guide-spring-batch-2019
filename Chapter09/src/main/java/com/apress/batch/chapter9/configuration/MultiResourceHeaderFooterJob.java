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

 

import javax.sql.DataSource; 
import com.apress.batch.chapter9.batch.CustomerRecordCountFooterCallback;
import com.apress.batch.chapter9.domain.Customer;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step; 
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;  
import org.springframework.batch.item.database.JdbcCursorItemReader; 
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder; 
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.MultiResourceItemWriter; 
import org.springframework.batch.item.file.builder.MultiResourceItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.FormatterLineAggregator; 
import org.springframework.beans.factory.annotation.Qualifier; 
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty; 
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource; 
import org.springframework.jdbc.core.BeanPropertyRowMapper; 
import org.springframework.transaction.PlatformTransactionManager;

/**
 * @author Michael Minella
 */
@Configuration
@ConditionalOnProperty(prefix = "main", name = "scenario", havingValue = "multiFlatFileGeneratorJob")

public class MultiResourceHeaderFooterJob { 
	@Bean
	public JdbcCursorItemReader<Customer> multiResourceJdbcReader(DataSource dataSource) {

		return new JdbcCursorItemReaderBuilder<Customer>()
				.name("customerItemReader")
				.dataSource(dataSource)
				.sql("select * from customer")
				.rowMapper(new BeanPropertyRowMapper<>(Customer.class))
				.build();
	}

	@Bean
	public MultiResourceItemWriter<Customer> multiFlatFileItemWriter() throws Exception {

		return new MultiResourceItemWriterBuilder<Customer>()
				.name("multiFlatFileItemWriter")
				.delegate(delegateCustomerItemWriter(null))
				.itemCountLimitPerResource(25)
				.resource(new FileSystemResource("Chapter09/target/customer"))
				.build();
	}

	@Bean
	@StepScope
	public FlatFileItemWriter<Customer> delegateCustomerItemWriter(CustomerRecordCountFooterCallback footerCallback) throws Exception {
		BeanWrapperFieldExtractor<Customer> fieldExtractor = new BeanWrapperFieldExtractor<>();
		fieldExtractor.setNames(new String[] {"firstName", "lastName", "address", "city", "state", "zip"});
		fieldExtractor.afterPropertiesSet();

		FormatterLineAggregator<Customer> lineAggregator = new FormatterLineAggregator<>();

		lineAggregator.setFormat("%s %s lives at %s %s in %s, %s.");
		lineAggregator.setFieldExtractor(fieldExtractor);

		FlatFileItemWriter<Customer> itemWriter = new FlatFileItemWriter<>();

		itemWriter.setName("delegateCustomerItemWriter");
		itemWriter.setLineAggregator(lineAggregator);
		itemWriter.setAppendAllowed(true);
		itemWriter.setFooterCallback(footerCallback);

		return itemWriter;
	}

	@Bean
	public Step multiFlatFileGeneratorStep(
			final JobRepository jobRepository,
			final PlatformTransactionManager transactionManager 
			) throws Exception {
		return  new StepBuilder("multiFlatFileGeneratorStep", jobRepository)
				.<Customer, Customer>chunk(10, transactionManager)
				.reader(multiResourceJdbcReader(null))
				.writer(multiFlatFileItemWriter())
				.build();
	}

	@Bean
	public Job multiFlatFileGeneratorJob(
			final JobRepository jobRepository,
			@Qualifier("multiFlatFileGeneratorStep") Step multiFlatFileGeneratorStep
			) throws Exception {
		return new JobBuilder("multiFlatFileGeneratorJob", jobRepository)
				.start(multiFlatFileGeneratorStep )
				.build();
	}
}
