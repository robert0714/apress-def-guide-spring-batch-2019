package com.example.Chapter04.jobs.config;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder; 
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder; 
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.CompletionPolicy;
import org.springframework.batch.repeat.policy.CompositeCompletionPolicy;
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy;
import org.springframework.batch.repeat.policy.TimeoutTerminationPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
 
import com.example.Chapter04.batch.LoggingStepStartStopListener;
import com.example.Chapter04.batch.RandomChunkSizePolicy; 

@Configuration
public class ChunkJobConfig { 

	@Bean
	public Job chunkBasedJob(JobRepository jobRepository ,Step chunkStep) { 
		return  new JobBuilder("chunkBasedJob", jobRepository)
				.start(chunkStep) 
				.build();
	}

	@Bean
	public Step chunkStep(final JobRepository jobRepository,final PlatformTransactionManager transactionManager) {
		return new StepBuilder("chunkStep", jobRepository)
//				.<String, String>chunk(1000)
				.<String, String>chunk(randomCompletionPolicy(),transactionManager)
				.reader(itemReader())
				.writer(itemWriter())
				.listener(new LoggingStepStartStopListener())
				.build();
	}

	@Bean
	public ListItemReader<String> itemReader() {
		List<String> items = new ArrayList<>(100000);

		for (int i = 0; i < 100000; i++) {
			items.add(UUID.randomUUID().toString());
		}

		return new ListItemReader<>(items);
	}

	@Bean
	public ItemWriter<String> itemWriter() {
		return items -> {
			for (String item : items) {
				System.out.println(">> current item = " + item);
			}
		};
	}

	@Bean
	public CompletionPolicy completionPolicy() {
		CompositeCompletionPolicy policy =
				new CompositeCompletionPolicy();

		policy.setPolicies(
				new CompletionPolicy[] {
						new TimeoutTerminationPolicy(3),
						new SimpleCompletionPolicy(1000)});

		return policy;
	}

	@Bean
	public CompletionPolicy randomCompletionPolicy() {
		return new RandomChunkSizePolicy();
	}

}
