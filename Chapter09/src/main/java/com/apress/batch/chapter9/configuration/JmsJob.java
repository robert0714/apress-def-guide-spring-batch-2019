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
 
import com.apress.batch.chapter9.domain.Customer;

import jakarta.jms.ConnectionFactory;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step; 
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;  
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.jms.JmsItemReader;
import org.springframework.batch.item.jms.JmsItemWriter;
import org.springframework.batch.item.jms.builder.JmsItemReaderBuilder;
import org.springframework.batch.item.jms.builder.JmsItemWriterBuilder; 
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.batch.item.xml.builder.StaxEventItemWriterBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty; 
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;  
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType; 
//import org.springframework.messaging.converter.MessageConverter;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * @author Michael Minella
 */
@Configuration
@ConditionalOnProperty(prefix = "main", name = "scenario", havingValue = "classifierCompositeWriterJob")
public class JmsJob { 
	@Bean // Serialize message content to json using TextMessage
	public MessageConverter jacksonJmsMessageConverter() {
		MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
		converter.setTargetType(MessageType.TEXT);
		converter.setTypeIdPropertyName("_type");
		return converter;
	}

	@Bean
	public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
		CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(connectionFactory);
		cachingConnectionFactory.afterPropertiesSet();

		JmsTemplate jmsTemplate = new JmsTemplate(cachingConnectionFactory);
		jmsTemplate.setDefaultDestinationName("customers");
		jmsTemplate.setReceiveTimeout(5000L);

		return jmsTemplate;
	}

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
	public StaxEventItemWriter<Customer> xmlOutputWriter(
			@Value("#{jobParameters['outputFile']}") WritableResource outputFile) {

		Map<String, Class<?>> aliases = new HashMap<>();
		aliases.put("customer", Customer.class);

		XStreamMarshaller marshaller = new XStreamMarshaller();
		marshaller.setAliases(aliases);

		return new StaxEventItemWriterBuilder<Customer>()
				.name("xmlOutputWriter")
				.resource(outputFile)
				.marshaller(marshaller)
				.rootTagName("customers")
				.build();
	}

	@Bean
	public JmsItemReader<Customer> jmsItemReader(JmsTemplate jmsTemplate) {

		return new JmsItemReaderBuilder<Customer>()
				.jmsTemplate(jmsTemplate)
				.itemType(Customer.class)
				.build();
	}

	@Bean
	public JmsItemWriter<Customer> jmsItemWriter(JmsTemplate jmsTemplate) {

		return new JmsItemWriterBuilder<Customer>()
				.jmsTemplate(jmsTemplate)
				.build();
	}

	@Bean
	public Step formatInputStep(
			final JobRepository jobRepository,
			final PlatformTransactionManager transactionManager 
			) throws Exception {
		return new StepBuilder("formatInputStep", jobRepository)
				.<Customer, Customer>chunk(10, transactionManager)
				.reader(customerFileReader(null))
				.writer(jmsItemWriter(null))
				.build();
	}

	@Bean
	public Step formatOutputStep(
			final JobRepository jobRepository,
			final PlatformTransactionManager transactionManager 
			) throws Exception {
		return new StepBuilder("formatOutputStep", jobRepository)
				.<Customer, Customer>chunk(10, transactionManager)
				.reader(jmsItemReader(null))
				.writer(xmlOutputWriter(null))
				.build();
	}

	@Bean
	public Job jmsFormatJob(
			final JobRepository jobRepository,
			@Qualifier("formatInputStep") Step formatInputStep,
			@Qualifier("formatOutputStep") Step formatOutputStep
			) throws Exception {
		return new JobBuilder("jmsFormatJob", jobRepository)
				.start(formatInputStep)
				.next(formatOutputStep)
				.build();
	}
}
