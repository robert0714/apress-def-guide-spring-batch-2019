package com.example.Chapter07;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder; 
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller; 
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.Chapter07.domain.Customer;
import com.example.Chapter07.domain.Transaction;

@SpringBootApplication
@ConditionalOnProperty(prefix = "main", name = "scenario", havingValue = "xmlJob")
public class XmlJob {
 
	@Bean
	@StepScope
	public StaxEventItemReader<Customer> customerFileReader(
			@Value("#{jobParameters['customerFile']}") Resource inputFile) {

		return new StaxEventItemReaderBuilder<Customer>()
				.name("customerFileReader")
				.resource(inputFile)
				.addFragmentRootElements("customer")
				.unmarshaller(customerMarshaller())
				.build();
	}

	@Bean
	public Jaxb2Marshaller customerMarshaller() {
		Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();

		jaxb2Marshaller.setClassesToBeBound(Customer.class,
				Transaction.class);

		return jaxb2Marshaller;
	} 
	@Bean
	public ItemWriter  itemWriter() {
		return (items) -> items.forEach(System.out::println);
	}

	@Bean
	public Step copyFileStep(
			final JobRepository jobRepository,
			final PlatformTransactionManager transactionManager 
			) {
		return new StepBuilder("copyFileStep", jobRepository)
				.<Customer, Customer>chunk(10, transactionManager)
				.reader(customerFileReader(null))
				.writer(itemWriter())
				.build();
	}

	@Bean
	public Job job(
			JobRepository jobRepository,
			org.springframework.batch.core.Step copyFileStep
			) {
		return new JobBuilder("job", jobRepository)
				.start(copyFileStep)
				.build();
	}
	public static void main(String[] args) {
		List<String> realArgs = Collections.singletonList("customerFile=classpath:/input/customer.xml");

		SpringApplication.run(XmlJob.class, realArgs.toArray(new String[1]));
	}

}

