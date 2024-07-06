package com.example.Chapter05;

import com.example.Chapter05.batch.ExploringTasklet;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

@EnableBatchProcessing
@SpringBootApplication
public class DemoApplication {

//	@Autowired
//	private JobBuilderFactory jobBuilderFactory;
//
//	@Autowired
//	private StepBuilderFactory stepBuilderFactory;

	@Autowired
	private JobExplorer jobExplorer;

	@Bean
	public Tasklet explorerTasklet() {
		return new ExploringTasklet(this.jobExplorer);
	}

	@Bean
	public Step explorerStep(
			final JobRepository jobRepository,
			final PlatformTransactionManager transactionManager 
			) {
//		return this.stepBuilderFactory.get("explorerStep")
//				.tasklet(explorerTasklet())
//				.build();
		 return new StepBuilder("explorerStep", jobRepository)
	    			.tasklet(explorerTasklet(), transactionManager)
	    			.build();
	}

	@Bean
	public Job explorerJob(JobRepository jobRepository,final Step explorerStep) {
//		return this.jobBuilderFactory.get("explorerJob")
//				.start(explorerStep())
//				.build();
		return new JobBuilder("explorerJob", jobRepository)
				.start(explorerStep) 
	            .build();
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}

