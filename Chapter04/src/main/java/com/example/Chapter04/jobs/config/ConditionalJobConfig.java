package com.example.Chapter04.jobs.config;
 

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet; 
import org.springframework.batch.repeat.RepeatStatus; 
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
  
import com.example.Chapter04.batch.RandomDecider; 
@Configuration
public class ConditionalJobConfig { 

	@Bean
	public Tasklet passTasklet() {
		return (contribution, chunkContext) -> {
//			return RepeatStatus.FINISHED;
			throw new RuntimeException("Causing a failure");
		};
	}

	@Bean
	public Tasklet successTasklet() {
		return (contribution, context) -> {
			System.out.println("Success!");
			return RepeatStatus.FINISHED;
		};
	}

	@Bean
	public Tasklet failTasklet() {
		return (contribution, context) -> {
			System.out.println("Failure!");
			return RepeatStatus.FINISHED;
		};
	}

	@Bean
	public Job job(final JobRepository jobRepository,final Step firstStep ,final Step successStep  ,final Step failureStep ) {		 
		return  new JobBuilder("conditionalJob", jobRepository)
				.start(firstStep)
//				.on("FAILED").stopAndRestart(successStep)
				.on("FAILED").to(failureStep)
				.from(firstStep)
					.on("*").to(successStep)
				.end()
				.build();
	}

	@Bean
	public Step firstStep(final JobRepository jobRepository,final PlatformTransactionManager transactionManager) {		 
		 return new StepBuilder("firstStep" ,jobRepository)				
	    			.tasklet(passTasklet(),transactionManager)
					.build();
	}

	@Bean
	public Step successStep(final JobRepository jobRepository,final PlatformTransactionManager transactionManager) {
		 return new StepBuilder("successStep" ,jobRepository)				
	    			.tasklet(successTasklet(),transactionManager)
					.build();
	}

	@Bean
	public Step failureStep(final JobRepository jobRepository,final PlatformTransactionManager transactionManager) {		
		 return new StepBuilder("failureStep" ,jobRepository)				
	    			.tasklet(successTasklet(),transactionManager)
					.build();
	}

	@Bean
	public JobExecutionDecider decider() {
		return new RandomDecider();
	}


}
