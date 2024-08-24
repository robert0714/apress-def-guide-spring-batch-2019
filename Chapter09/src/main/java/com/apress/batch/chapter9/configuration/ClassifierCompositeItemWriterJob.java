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

import com.apress.batch.chapter9.batch.CustomerClassifier;
import com.apress.batch.chapter9.domain.Customer;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step; 
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder; 
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter; 
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder; 
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder; 
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.batch.item.support.builder.ClassifierCompositeItemWriterBuilder;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.batch.item.xml.builder.StaxEventItemWriterBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.classify.Classifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource; 
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate; 
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * @author Michael Minella
 */
@Configuration
@ConditionalOnProperty(prefix = "main", name = "scenario", havingValue = "classifierCompositeWriterJob")
public class ClassifierCompositeItemWriterJob { 

	@Bean
	@StepScope
	public FlatFileItemReader<Customer> classifierCompositeWriterItemReader(
			@Value("#{jobParameters['customerFile']}")Resource inputFile) {

		return new FlatFileItemReaderBuilder<Customer>()
				.name("classifierCompositeWriterItemReader")
				.resource(inputFile)
				.delimited()
				.names(new String[] {"firstName",
						"middleInitial",
						"lastName",
						"address",
						"city",
						"state",
						"zip",
						"email"})
				.targetType(Customer.class)
				.build();
	}

	@Bean
	@StepScope
	public StaxEventItemWriter<Customer> xmlDelegate(
			@Value("#{jobParameters['outputFile']}") WritableResource outputFile) throws Exception {

		Map<String, Class> aliases = new HashMap<>();
		aliases.put("customer", Customer.class);

		XStreamMarshaller marshaller = new XStreamMarshaller();

		marshaller.setAliases(aliases);

		marshaller.afterPropertiesSet();
		 
		return new StaxEventItemWriterBuilder<Customer>()
				.name("customerItemWriter")
				.resource(outputFile)
				.marshaller(marshaller)
				.rootTagName("customers")
				.build();
	}

	@Bean
	public JdbcBatchItemWriter<Customer> jdbcDelgate(DataSource dataSource) {

		return new JdbcBatchItemWriterBuilder<Customer>()
				.namedParametersJdbcTemplate(new NamedParameterJdbcTemplate(dataSource))
				.sql("INSERT INTO customer (first_name, " +
						"middle_initial, " +
						"last_name, " +
						"address, " +
						"city, " +
						"state, " +
						"zip, " +
						"email) " +
						"VALUES(:firstName, " +
						":middleInitial, " +
						":lastName, " +
						":address, " +
						":city, " +
						":state, " +
						":zip, " +
						":email)")
				.beanMapped()
				.build();
	}

	@Bean
	public ClassifierCompositeItemWriter<Customer> classifierCompositeItemWriter() throws Exception {
		Classifier<Customer, ItemWriter<? super Customer>> classifier = new CustomerClassifier(xmlDelegate(null), jdbcDelgate(null));

		return new ClassifierCompositeItemWriterBuilder<Customer>()
				.classifier(classifier)
				.build();
	}


	@Bean
	public Step classifierCompositeWriterStep(
			final JobRepository jobRepository,
			final PlatformTransactionManager transactionManager 
			) throws Exception {
		return new StepBuilder("classifierCompositeWriterStep", jobRepository)
				.<Customer, Customer>chunk(10, transactionManager)
				.reader(classifierCompositeWriterItemReader(null))
				.writer(classifierCompositeItemWriter())
				.stream(xmlDelegate(null))
				.build();
	}

	@Bean     
	public Job classifierCompositeWriterJob(
			final JobRepository jobRepository,
			@Qualifier("classifierCompositeWriterStep") Step classifierCompositeWriterStep
			) throws Exception {
		return new JobBuilder("classifierCompositeWriterJob", jobRepository)
				.start(classifierCompositeWriterStep)
				.build();
	}
}
