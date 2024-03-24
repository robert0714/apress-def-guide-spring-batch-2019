package com.example.Chapter04.jobs.config; 

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step; 
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.core.job.flow.Flow;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
 

@Configuration
public class FlowJobConfig { 
	@Bean
	public Tasklet loadStockFile() {
		return (contribution, chunkContext) -> {
			System.out.println("The stock file has been loaded");
			return RepeatStatus.FINISHED;
		};
	}

	@Bean
	public Tasklet loadCustomerFile() {
		return (contribution, chunkContext) -> {
			System.out.println("The customer file has been loaded");
			return RepeatStatus.FINISHED;
		};
	}

	@Bean
	public Tasklet updateStart() {
		return (contribution, chunkContext) -> {
			System.out.println("The start has been updated");
			return RepeatStatus.FINISHED;
		};
	}

	@Bean
	public Tasklet runBatchTasklet() {
		return (contribution, chunkContext) -> {
			System.out.println("The batch has been run");
			return RepeatStatus.FINISHED;
		};
	}

	@Bean
	public Flow preProcessingFlow(final Step loadFileStep, final Step loadCustomerStep, final Step updateStartStep) {
		return new FlowBuilder<Flow>("preProcessingFlow").start(loadFileStep)
				.next(loadCustomerStep)
				.next(updateStartStep)
				.build();
	}

	@Bean
	public Job conditionalStepLogicJob(JobRepository jobRepository ,Step intializeBatch,Step runBatch) {
		return  new JobBuilder("conditionalStepLogicJob", jobRepository)
				.start(intializeBatch)
				.next(runBatch)
				.build();
	}

	@Bean
	public Step intializeBatch(final JobRepository jobRepository,final Flow preProcessingFlow) {
		return new StepBuilder("initalizeBatch" ,jobRepository)				
				.flow(preProcessingFlow)
				.build();
	}

	@Bean
	public Step loadFileStep(final JobRepository jobRepository,final PlatformTransactionManager transactionManager) {		
		return new StepBuilder("loadFileStep" ,jobRepository)				
    			.tasklet(loadStockFile(), 
    					transactionManager)
    			.build();
	}

	@Bean
	public Step loadCustomerStep(final JobRepository jobRepository,final PlatformTransactionManager transactionManager) {
	
		return new StepBuilder("loadCustomerStep" ,jobRepository)				
    			.tasklet(loadCustomerFile(), 
    					transactionManager)
    			.build();
	}

	@Bean
	public Step updateStartStep(final JobRepository jobRepository,final PlatformTransactionManager transactionManager) {		 
		return new StepBuilder("updateStartStep" ,jobRepository)				
    			.tasklet(updateStart(), 
    					transactionManager)
    			.build();
	}

	@Bean
	public Step runBatch(final JobRepository jobRepository,final PlatformTransactionManager transactionManager) {
		return new StepBuilder("runBatch" ,jobRepository)				
    			.tasklet(runBatchTasklet(), 
    					transactionManager)
    			.build();
	}

}
