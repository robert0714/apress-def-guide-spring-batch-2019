package com.example.Chapter07;

import java.util.Arrays;
import java.util.List;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder; 
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step; 
import org.springframework.batch.core.job.builder.JobBuilder; 
import org.springframework.batch.core.step.builder.StepBuilder; 

import com.example.Chapter07.batch.CustomerFieldSetMapper;
import com.example.Chapter07.batch.CustomerFileLineTokenizer;
import com.example.Chapter07.domain.Customer;
 

@SpringBootApplication
@ConditionalOnProperty(prefix = "main", name = "scenario", havingValue = "delimitedJob") 
public class DelimitedJob { 

//	@Bean
//	@StepScope
//	public FlatFileItemReader<Customer> customerItemReader(@Value("#{jobParameters['customerFile']}")Resource inputFile) {
//		if(inputFile ==null) {
//			System.out.println("inputFileResource is null");
//			 
//		}else {
//			System.out.println(inputFile.exists());
//		}
//		
//		
//		return new FlatFileItemReaderBuilder<Customer>()
//				.name("customerItemReader")
//				.delimited()
//				.names(new String[] {"firstName",
//						"middleInitial",
//						"lastName",
//						"addressNumber",
//						"street",
//						"city",
//						"state",
//						"zipCode"})
//				.fieldSetMapper(new CustomerFieldSetMapper())
//				.resource(inputFile)
//				.build();
//	}
	
	@Bean
	@StepScope
	public FlatFileItemReader<Customer> customerItemReader(@Value("#{jobParameters['customerFile']}")Resource inputFile) {
		if(inputFile ==null) {
			System.out.println("inputFileResource is null");			 
		}else {
			System.out.println(inputFile.exists());
		}
		return new FlatFileItemReaderBuilder<Customer>()
				.name("customerItemReader")
				.lineTokenizer(new CustomerFileLineTokenizer())
				.fieldSetMapper(new CustomerFieldSetMapper())
				.resource(inputFile)
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
				.reader(customerItemReader(null))
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
		List<String> realArgs = Arrays.asList("customerFile=classpath:/input/customer.csv");

		SpringApplication.run(DelimitedJob.class, realArgs.toArray(new String[1]));
	}

}


