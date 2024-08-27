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

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;
 
import com.apress.batch.chapter9.batch.CustomerOutputFileSuffixCreator;
import com.apress.batch.chapter9.batch.CustomerXmlHeaderCallback;
import com.apress.batch.chapter9.domain.Customer;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step; 
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;  
import org.springframework.batch.item.database.JdbcCursorItemReader; 
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder; 
import org.springframework.batch.item.file.MultiResourceItemWriter; 
import org.springframework.batch.item.file.builder.MultiResourceItemWriterBuilder; 
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.batch.item.xml.builder.StaxEventItemWriterBuilder;
import org.springframework.beans.factory.annotation.Qualifier; 
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty; 
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource; 
import org.springframework.jdbc.core.BeanPropertyRowMapper; 
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * @author Michael Minella
 */
@Configuration
@ConditionalOnProperty(prefix = "main", name = "scenario", havingValue = "xmlGeneratorJob")

public class MultiResourceJob { 
	@Bean
	public JdbcCursorItemReader<Customer> customerJdbcCursorItemReader(DataSource dataSource) {

		return new JdbcCursorItemReaderBuilder<Customer>()
				.name("customerItemReader")
				.dataSource(dataSource)
				.sql("select * from customer")
				.rowMapper(new BeanPropertyRowMapper<>(Customer.class))
				.build();
	}

	@Bean
	public MultiResourceItemWriter<Customer> multiCustomerFileWriter(CustomerOutputFileSuffixCreator suffixCreator) throws Exception {

		return new MultiResourceItemWriterBuilder<Customer>()
				.name("multiCustomerFileWriter")
				.delegate(delegateItemWriter(null))
				.itemCountLimitPerResource(25)
				.resource(new FileSystemResource("Chapter09/target/customer"))
				.resourceSuffixCreator(suffixCreator)
				.build();
	}

	@Bean
	@StepScope
	public StaxEventItemWriter<Customer> delegateItemWriter(CustomerXmlHeaderCallback headerCallback) throws Exception {

		Map<String, Class> aliases = new HashMap<>();
		aliases.put("customer", Customer.class);

		XStreamMarshaller marshaller = new XStreamMarshaller();

		marshaller.setAliases(aliases);

		marshaller.afterPropertiesSet();

		return new StaxEventItemWriterBuilder<Customer>()
				.name("customerItemWriter")
				.marshaller(marshaller)
				.rootTagName("customers")
				.headerCallback(headerCallback)
				.build();
	}

	@Bean
	public Step multiXmlGeneratorStep(
			final JobRepository jobRepository,
			final PlatformTransactionManager transactionManager 
			) throws Exception {
		return new StepBuilder("multiXmlGeneratorStep", jobRepository)
				.<Customer, Customer>chunk(10, transactionManager)
				.reader(customerJdbcCursorItemReader(null))
				.writer(multiCustomerFileWriter(null))
				.build();
	}

	@Bean
	public Job xmlGeneratorJob(
			final JobRepository jobRepository,
			@Qualifier("multiXmlGeneratorStep") Step multiXmlGeneratorStep
			) throws Exception {
		return new JobBuilder("xmlGeneratorJob", jobRepository)
				.start(multiXmlGeneratorStep)
				.build();
	}
}
