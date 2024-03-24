package com.example.Chapter04.jobs.config;

import java.util.concurrent.Callable;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.CallableTaskletAdapter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class CallableTaskletConfig {
	@Bean
	public Job callableJob(JobRepository jobRepository ,Step callableStep) {
		
		return  new JobBuilder("callableJob", jobRepository)
				.start(callableStep)
				.build();
	}
 
	@Bean
	public Step callableStep(final JobRepository jobRepository,final PlatformTransactionManager transactionManager) {
		
		return  new StepBuilder("callableStep" ,jobRepository)				
				.tasklet(tasklet(),transactionManager)
				.build();
	}

	@Bean
	public Callable<RepeatStatus> callableObject() {
		return () -> {
			System.out.println("This was executed in another thread");

			return RepeatStatus.FINISHED;
		};
	}

	@Bean
	public CallableTaskletAdapter tasklet() {
		CallableTaskletAdapter callableTaskletAdapter =
				new CallableTaskletAdapter();

		callableTaskletAdapter.setCallable(callableObject());

		return callableTaskletAdapter;
	}
}
