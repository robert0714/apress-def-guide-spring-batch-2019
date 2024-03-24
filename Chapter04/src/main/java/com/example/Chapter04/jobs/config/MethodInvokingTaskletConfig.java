package com.example.Chapter04.jobs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
 
import com.example.Chapter04.service.CustomService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder; 
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.MethodInvokingTaskletAdapter;
import org.springframework.beans.factory.annotation.Value;
@Configuration
public class MethodInvokingTaskletConfig {

	@Bean
	public Job methodInvokingJob(JobRepository jobRepository ,Step methodInvokingStep) { 
		return  new JobBuilder("methodInvokingJob", jobRepository)
				.start(methodInvokingStep) 
				.build();
	}

	@Bean
	public Step methodInvokingStep(final JobRepository jobRepository,final PlatformTransactionManager transactionManager) {		 
		return new StepBuilder("methodInvokingStep" ,jobRepository)				
    			.tasklet(methodInvokingTasklet(null), 
    					transactionManager)
    			.build();
	}

	@StepScope
	@Bean
	public MethodInvokingTaskletAdapter methodInvokingTasklet(
			@Value("#{jobParameters['message']}") String message) {

		MethodInvokingTaskletAdapter methodInvokingTaskletAdapter =
				new MethodInvokingTaskletAdapter();

		methodInvokingTaskletAdapter.setTargetObject(service());
		methodInvokingTaskletAdapter.setTargetMethod("serviceMethod");
		methodInvokingTaskletAdapter.setArguments(
				new String[] {message});

		return methodInvokingTaskletAdapter;
	}

	@Bean
	public CustomService service() {
		return new CustomService();
	}
}
