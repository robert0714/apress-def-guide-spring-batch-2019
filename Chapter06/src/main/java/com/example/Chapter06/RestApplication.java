package com.example.Chapter06;

 
import java.util.Map;
import java.util.Map.Entry; 
import java.util.Set;
 

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job; 
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step; 
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder; 
import org.springframework.batch.repeat.RepeatStatus; 
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean; 
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
 

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@SpringBootApplication
@ConditionalOnProperty(prefix = "main", name = "scenario", havingValue = "restJob") 
public class RestApplication {
 
	@Bean
	public Job job(
			JobRepository jobRepository,
			org.springframework.batch.core.Step step1
			) {		
		return new JobBuilder("job", jobRepository)
				.incrementer(new RunIdIncrementer())
				.start(step1)
				.build();
	}

	@Bean
	public Step step1(
			final JobRepository jobRepository,
			final PlatformTransactionManager transactionManager 
			) {  
		return new StepBuilder("step1", jobRepository)
				.tasklet(
					(stepContribution, chunkContext) -> {
   					   System.out.println("step 1 ran today!");
	   				  return RepeatStatus.FINISHED;
			        }  ,transactionManager )
				.build(); 
	}

	@RestController
	public static class JobLaunchingController {
		
		private final JobLauncher jobLauncher;

		private final  ApplicationContext context;

		private final JobExplorer jobExplorer;
		
		public JobLaunchingController(
				JobLauncher jobLauncher ,
				ApplicationContext context,
				JobExplorer jobExplorer) {
			this.jobLauncher = jobLauncher ;
			this.context = context ;
			this.jobExplorer = jobExplorer ;
		}  

		@PostMapping(path = "/run")
		public ExitStatus runJob(@RequestBody JobLaunchRequest request) throws Exception {
			Job job = this.context.getBean(request.getName(), Job.class);
			JobParametersBuilder builder = new JobParametersBuilder() ;
			
			Set<Entry<String, String>> entrySet = request.getJobParameters().entrySet() ;
			for(Entry<String, String> unit :entrySet ) {
				builder.addString(unit.getKey().toString(), unit.getValue().toString());
			} 
			
			JobParameters jobParameters = 
							new JobParametersBuilder(builder.toJobParameters(),
								this.jobExplorer)
							.getNextJobParameters(job)
							.toJobParameters();

			return this.jobLauncher.run(job, jobParameters).getExitStatus();
//			Job job = this.context.getBean(request.getName(), Job.class);
//
//			return this.jobLauncher.run(job, request.getJobParameters()).getExitStatus();
		}
	}
	@NoArgsConstructor
	@AllArgsConstructor
	@Data
	@Builder
	public static class JobLaunchRequest {
		private String name;
		private Map<String,String> jobParameters; 
	}

	public static void main(String[] args) {
		new SpringApplication(RestApplication.class).run(args);
	}
}
