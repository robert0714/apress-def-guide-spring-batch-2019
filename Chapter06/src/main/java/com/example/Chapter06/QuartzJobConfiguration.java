/**
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.Chapter06;
 
import org.springframework.batch.core.Job;  
import org.springframework.batch.core.Step;  
import org.springframework.batch.core.job.builder.JobBuilder; 
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder; 
import org.springframework.batch.repeat.RepeatStatus; 
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty; 
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager; 

/**
 * @author Michael Minella
 */ 
@SpringBootApplication
@ConditionalOnProperty(prefix = "main", name = "scenario", havingValue = "quartzJob") 
public class QuartzJobConfiguration {

	@Configuration
	public class BatchConfiguration {

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
					.tasklet((stepContribution, chunkContext) -> {
						System.out.println("step1 ran!");
						return RepeatStatus.FINISHED;
					} ,transactionManager )
					.build(); 
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(QuartzJobConfiguration.class, args);
	}
}
