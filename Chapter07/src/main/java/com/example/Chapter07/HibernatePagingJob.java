package com.example.Chapter07;
 
import java.util.Collections;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.Chapter07.domain.Customer;

import jakarta.persistence.EntityManagerFactory;

@SpringBootApplication
@ConditionalOnProperty(prefix = "main", name = "scenario", havingValue = "hibernatePagingJob")
public class HibernatePagingJob { 

	@Bean
	@StepScope
	public JpaPagingItemReader<Customer> customerItemReader(
			EntityManagerFactory entityManagerFactory,
			@Value("#{jobParameters['city']}") String city) {

		return new JpaPagingItemReaderBuilder<Customer>()
				.name("customerItemReader")
				.entityManagerFactory(entityManagerFactory)				
//				.sessionFactory(entityManagerFactory.unwrap(SessionFactory.class))
				.queryString("from Customer where city = :city")
				.parameterValues(Collections.singletonMap("city", city))
				.pageSize(10)
				.build();
	}
	
	@Bean
	public ItemWriter<Customer> itemWriter() {
		return (items) -> items.forEach(System.out::println);
	}

	@Bean
	public Step copyFileStep(
			final JobRepository jobRepository,
			final PlatformTransactionManager transactionManager 
			) {
		return new StepBuilder("copyFileStep", jobRepository)
				.<Customer, Customer>chunk(10, transactionManager)
				.reader(customerItemReader(null,null))
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

		SpringApplication.run(HibernateCursorJob.class, "city=Chicago");
	}

}

