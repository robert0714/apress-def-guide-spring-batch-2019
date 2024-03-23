package com.example.Chapter04.jobs.config;

import java.util.Arrays;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.CompositeJobParametersValidator;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.listener.JobListenerFactoryBean;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.Chapter04.batch.DailyJobTimestamper;
import com.example.Chapter04.batch.JobLoggerListener;
import com.example.Chapter04.batch.ParameterValidator;

@Configuration
public class HelloWorldConfig {
	@Bean
	public CompositeJobParametersValidator validator() {
		CompositeJobParametersValidator validator = new CompositeJobParametersValidator();

		DefaultJobParametersValidator defaultJobParametersValidator = new DefaultJobParametersValidator(
				new String[] { "fileName" }, new String[] { "name", "currentDate" });

		defaultJobParametersValidator.afterPropertiesSet();

		validator.setValidators(Arrays.asList(new ParameterValidator(), defaultJobParametersValidator));

		return validator;
	}

	@Bean
	public Job job(JobRepository jobRepository ,Step step1) {
		return  new JobBuilder("basicJob", jobRepository)
				.start(step1)
				.validator(validator())
				.incrementer(new DailyJobTimestamper())
				.listener(new JobLoggerListener())
				.listener(JobListenerFactoryBean.getListener(new JobLoggerListener()))
				.build();
	}

//	@Bean
//	public Job job(JobRepository jobRepository ,Step step1) {
//		return new JobBuilder("basicJob", jobRepository)
//				.start(step1)
//				.build();
//	}

	@Bean
	public Step step1(final JobRepository jobRepository,final PlatformTransactionManager transactionManager) {		 
		return new StepBuilder("step1" ,jobRepository)				
    			.tasklet(
//    					helloWorldTasklet(null, null), 
    					helloWorldTasklet(), 
    					transactionManager)
    			.build();
	}

//	@StepScope
//	@Bean
//	public Tasklet helloWorldTasklet(
//			@Value("#{jobParameters['name']}") String name,
//			@Value("#{jobParameters['fileName']}") String fileName) {
//
//		return (contribution, chunkContext) -> {
//
//				System.out.println(
//						String.format("Hello, %s!", name));
//				System.out.println(
//						String.format("fileName = %s", fileName));
//
//				return RepeatStatus.FINISHED;
//			};
//	}

	@Bean
	public Tasklet helloWorldTasklet() {

		return (contribution, chunkContext) -> {
				String name = (String) chunkContext.getStepContext()
					.getJobParameters()
					.get("name");

				System.out.println(String.format("Hello, %s!", name));
				return RepeatStatus.FINISHED;
			};
	}
}
