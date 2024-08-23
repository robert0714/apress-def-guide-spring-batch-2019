package com.example.Chapter08;
 
import java.util.Arrays;
import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.adapter.ItemProcessorAdapter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.support.ClassifierCompositeItemProcessor;
import org.springframework.batch.item.support.ScriptItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.classify.Classifier;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.Chapter08.batch.ZipCodeClassifier;
import com.example.Chapter08.domain.Customer;
import com.example.Chapter08.service.UpperCaseNameService;
 
 

@SpringBootApplication
@ConditionalOnProperty(prefix = "main", name = "scenario", havingValue = "classifierCompositeItemProcessorJob")
public class ClassifierCompositeItemProcessorJob {
 
	@Bean
	@StepScope
	public FlatFileItemReader<Customer> customerItemReader(
			@Value("#{jobParameters['customerFile']}")Resource inputFile) {

		return new FlatFileItemReaderBuilder<Customer>()
				.name("customerItemReader")
				.delimited()
				.names(new String[] {"firstName",
						"middleInitial",
						"lastName",
						"address",
						"city",
						"state",
						"zip"})
				.targetType(Customer.class)
				.resource(inputFile)
				.build();
	}

	@Bean
	public ItemWriter<Customer> itemWriter() {
		return (items) -> items.forEach(System.out::println);
	}

	@Bean
	public ItemProcessorAdapter<Customer, Customer> upperCaseItemProcessor(UpperCaseNameService service) {
		ItemProcessorAdapter<Customer, Customer> adapter = new ItemProcessorAdapter<>();

		adapter.setTargetObject(service);
		adapter.setTargetMethod("upperCase");

		return adapter;
	}

	@Bean
	@StepScope
	public ScriptItemProcessor<Customer, Customer> lowerCaseItemProcessor(
			@Value("#{jobParameters['script']}") Resource script) {

		ScriptItemProcessor<Customer, Customer> itemProcessor =
				new ScriptItemProcessor<>();

		itemProcessor.setScript(script);

		return itemProcessor;
	}

	@Bean
	public Classifier classifier() {
		return new ZipCodeClassifier(upperCaseItemProcessor(null),
				lowerCaseItemProcessor(null));
	}

	@Bean
	public ClassifierCompositeItemProcessor<Customer, Customer> itemProcessor() {
		ClassifierCompositeItemProcessor<Customer, Customer> itemProcessor =
				new ClassifierCompositeItemProcessor<>();

		itemProcessor.setClassifier(classifier());

		return itemProcessor;
	} 

	@Bean
	public Step copyFileStep(
			final JobRepository jobRepository,
			final PlatformTransactionManager transactionManager 
			) {
		return new StepBuilder("copyFileStep", jobRepository)
				.<Customer, Customer>chunk(5, transactionManager)
				.reader(customerItemReader(null))
				.processor(itemProcessor())
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
		List<String> realArgs = Arrays.asList("customerFile=classpath:/input/customer.csv","script=classpath:/lowerCase.js");

		SpringApplication.run(ClassifierCompositeItemProcessorJob.class, realArgs.toArray(new String[1]));
	}

}