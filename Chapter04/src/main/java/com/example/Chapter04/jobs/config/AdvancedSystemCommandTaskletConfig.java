package com.example.Chapter04.jobs.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.SimpleSystemProcessExitCodeMapper;
import org.springframework.batch.core.step.tasklet.SystemCommandTasklet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class AdvancedSystemCommandTaskletConfig {
	@Bean
	public Job job(JobRepository jobRepository ,Step systemCommandStep) {		
		return  new JobBuilder("systemCommandJob", jobRepository)
				.start(systemCommandStep) 
				.build(); 
	}

	@Bean
	public Step systemCommandStep(final JobRepository jobRepository,final PlatformTransactionManager transactionManager) {
        return new StepBuilder("systemCommandStep" ,jobRepository)				
    			.tasklet(systemCommandTasklet(), 
    					transactionManager)
    			.build();
	}

	@Bean
	public SystemCommandTasklet systemCommandTasklet() {
		SystemCommandTasklet tasklet = new SystemCommandTasklet();

		tasklet.setCommand("touch tmp.txt");
		tasklet.setTimeout(5000);
		tasklet.setInterruptOnCancel(true);

		// Change this directory to something appropriate for your environment
		tasklet.setWorkingDirectory("/Users/mminella/spring-batch");

		tasklet.setSystemProcessExitCodeMapper(touchCodeMapper());
		tasklet.setTerminationCheckInterval(5000);
		tasklet.setTaskExecutor(new SimpleAsyncTaskExecutor());
		tasklet.setEnvironmentParams(new String[] {
				"JAVA_HOME=/java",
				"BATCH_HOME=/Users/batch"});

		return tasklet;
	}

	@Bean
	public SimpleSystemProcessExitCodeMapper touchCodeMapper() {
		return new SimpleSystemProcessExitCodeMapper();
	}

}
