/*
 * Copyright 2018 the original author or authors.
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
package com.apress.batch.chapter10.batch;

import org.junit.jupiter.api.Test; 
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step; 
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication; 
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean; 
import org.springframework.test.context.TestPropertySource; 
import org.springframework.transaction.PlatformTransactionManager;

/**
 * @author Michael Minella
 */
@SpringBatchTest
@SpringBootTest(  properties = { "spring.batch.job.name=job", 
                                "spring.job.names=job",
                                "spring.batch.job.enabled=false" } , 
                 classes =  SimpleJdbcTestExampleTest.BatchConfiguration.class  )  
@TestPropertySource(properties = "debug=true") 
public class SimpleJdbcTestExampleTest {

	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	@Test
	public void test() {
		this.jobLauncherTestUtils.launchStep("step1");
	}

	@SpringBootApplication
	public static class BatchConfiguration { 
		@Bean
		public Step step1(
				final JobRepository jobRepository,
				final PlatformTransactionManager transactionManager 
				) {
			return new StepBuilder("step1", jobRepository)
					.tasklet((stepContribution, chunkContext) -> {
						System.out.println("I was executed");
						return RepeatStatus.FINISHED;
					}  ,   transactionManager )
					.build();
		}

		@Bean
		public Job job(
				final JobRepository jobRepository,
				@Qualifier("step1") Step step1
				) {
			return new JobBuilder("job", jobRepository)
					.start(step1)
					.build();
		}
	}
}
