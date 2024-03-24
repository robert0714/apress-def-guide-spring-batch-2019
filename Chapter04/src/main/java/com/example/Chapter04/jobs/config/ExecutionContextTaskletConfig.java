package com.example.Chapter04.jobs.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder; 
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.Chapter04.batch.HelloWorldTasklet; 

@Configuration
public class ExecutionContextTaskletConfig {
	@Bean
	public Job helloWorldBatchJob(JobRepository jobRepository ,Step helloWorldStep) {		 
		return  new JobBuilder("helloWorldBatchJob", jobRepository)
				.start(helloWorldStep) 
				.build();
	}

	@Bean
	public Step helloWorldStep(final JobRepository jobRepository,final PlatformTransactionManager transactionManager) {		 
		return new StepBuilder("helloWorldStep" ,jobRepository)				
    			.tasklet(tasklet(), 
    					transactionManager)
    			.build();
	}
	@StepScope
	@Bean
	public HelloWorldTasklet tasklet() {
		return new HelloWorldTasklet();
	}
}
