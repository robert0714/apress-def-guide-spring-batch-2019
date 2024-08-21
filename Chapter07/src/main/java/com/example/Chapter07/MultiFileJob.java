package com.example.Chapter07;

 
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.MultiResourceItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.mapping.PatternMatchingCompositeLineMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.LineTokenizer; 
import org.springframework.transaction.PlatformTransactionManager;

import com.example.Chapter07.batch.CustomerFileReader;
import com.example.Chapter07.batch.TransactionFieldSetMapper;
import com.example.Chapter07.domain.Customer;

@SpringBootApplication
@ConditionalOnProperty(prefix = "main", name = "scenario", havingValue = "multiFileJob")
public class MultiFileJob {
 
	@Bean
	@StepScope
	public FlatFileItemReader customerItemReader() {

		return new FlatFileItemReaderBuilder<Customer>()
				.name("customerItemReader")
				.lineMapper(lineTokenizer())
				.build();
	}

	@Bean
	@StepScope
	public MultiResourceItemReader multiCustomerReader(@Value("#{jobParameters['customerFile']}")Resource[] inputFiles) {
		return new MultiResourceItemReaderBuilder<>()
				.name("multiCustomerReader")
				.resources(inputFiles)
				.delegate(customerFileReader())
				.build();
	}

	@Bean
	public CustomerFileReader customerFileReader() {
		return new CustomerFileReader(customerItemReader());
	}

	@Bean
	public PatternMatchingCompositeLineMapper lineTokenizer() {
		Map<String, LineTokenizer> lineTokenizers =
				new HashMap<>(2);

		lineTokenizers.put("CUST*", customerLineTokenizer());
		lineTokenizers.put("TRANS*", transactionLineTokenizer());

		Map<String, FieldSetMapper> fieldSetMappers =
				new HashMap<>(2);

		BeanWrapperFieldSetMapper<Customer> customerFieldSetMapper =
				new BeanWrapperFieldSetMapper<>();
		customerFieldSetMapper.setTargetType(Customer.class);

		fieldSetMappers.put("CUST*", customerFieldSetMapper);
		fieldSetMappers.put("TRANS*", new TransactionFieldSetMapper());

		PatternMatchingCompositeLineMapper lineMappers =
				new PatternMatchingCompositeLineMapper();

		lineMappers.setTokenizers(lineTokenizers);
		lineMappers.setFieldSetMappers(fieldSetMappers);

		return lineMappers;
	}

	@Bean
	public DelimitedLineTokenizer transactionLineTokenizer() {
		DelimitedLineTokenizer lineTokenizer =
				new DelimitedLineTokenizer();

		lineTokenizer.setNames("prefix",
				"accountNumber",
				"transactionDate",
				"amount");

		return lineTokenizer;
	}

	@Bean
	public DelimitedLineTokenizer customerLineTokenizer() {
		DelimitedLineTokenizer lineTokenizer =
				new DelimitedLineTokenizer();

		lineTokenizer.setNames("firstName",
				"middleInitial",
				"lastName",
				"address",
				"city",
				"state",
				"zipCode");

		lineTokenizer.setIncludedFields(1, 2, 3, 4, 5, 6, 7);

		return lineTokenizer;
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
				.reader(multiCustomerReader(null))
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
		List<String> realArgs = Collections.singletonList("customerFile=classpath:/input/customerMultiFormat*");

		SpringApplication.run(MultiFileJob.class, realArgs.toArray(new String[1]));
	}

}

