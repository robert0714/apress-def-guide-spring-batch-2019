package io.spring.batch.helloworld;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.Assert;
 
@SpringBootApplication
public class HelloWorldApplication {
	@Bean
	public Step step(final JobRepository jobRepository,final PlatformTransactionManager transactionManager) {
		return new StepBuilder("step1" ,jobRepository)				
    			.tasklet(helloWorldTasklet(), transactionManager)
    			.build();
	}
	@Bean
	public Job job(JobRepository jobRepository ,Step step1) {
		return  new JobBuilder("hellowJob", jobRepository)
				.start(step1)
				.build();
	}
	@Bean
	public HelloWorldTasklet helloWorldTasklet() {
		HelloWorldTasklet tasklet = new HelloWorldTasklet();
		tasklet.setUserName("Robert");
		return tasklet;
	}

	public static void main(String[] args) {
		SpringApplication.run(HelloWorldApplication.class, args);
	}

	static class HelloWorldTasklet implements Tasklet , InitializingBean{
		private String userName;

		public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
			String msg = String.format("Hello, World!  %s", this.userName);
			System.out.println(msg);
			return RepeatStatus.FINISHED;
		}

		public void setUserName(String userName) {
			this.userName = userName;
		}

		@Override
		public void afterPropertiesSet() throws Exception {
			 Assert.state(userName != null, "userName must be set");
		}

	}
}
