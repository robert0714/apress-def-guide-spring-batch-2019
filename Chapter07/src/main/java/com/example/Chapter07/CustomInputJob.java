package com.example.Chapter07;

import com.example.Chapter07.batch.CustomerItemListener;
import com.example.Chapter07.batch.CustomerItemReader;
import com.example.Chapter07.domain.Customer;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step; 
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter; 
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean; 
import org.springframework.transaction.PlatformTransactionManager;

@SpringBootApplication
@ConditionalOnProperty(prefix = "main", name = "scenario", havingValue = "customInputJob") 
public class CustomInputJob {
	 
		@Bean
		public CustomerItemReader customerItemReader() {
			CustomerItemReader customerItemReader = new CustomerItemReader();
	
			customerItemReader.setName("customerItemReader");
	
			return customerItemReader;
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
			return  new StepBuilder("copyFileStep", jobRepository)
					.<Customer, Customer>chunk(10, transactionManager)
					.reader(customerItemReader())
					.writer(itemWriter())
					.listener(new CustomerItemListener())
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

		SpringApplication.run(CustomInputJob.class, args);
	}

}

