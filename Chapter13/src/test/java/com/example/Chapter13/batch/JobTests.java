/*
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
package com.example.Chapter13.batch;

import java.util.Arrays; 

import org.junit.jupiter.api.Test; 

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution; 
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication; 
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean; 
import org.springframework.test.context.TestPropertySource; 
import org.springframework.transaction.PlatformTransactionManager; 
 

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Michael Minella
 */ 
@SpringBatchTest
@SpringBootTest(  properties = {  
                                "spring.batch.job.enabled=false" } , 
                 classes =  JobTests.BatchConfiguration.class  )  
@TestPropertySource(properties = "debug=true")
public class JobTests {

	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	@Test
	public void test() throws Exception {
		JobExecution jobExecution =
				this.jobLauncherTestUtils.launchJob();

		assertEquals(BatchStatus.COMPLETED,
				jobExecution.getStatus());

		StepExecution stepExecution =
				jobExecution.getStepExecutions().iterator().next();

		assertEquals(BatchStatus.COMPLETED, stepExecution.getStatus());
		assertEquals(3, stepExecution.getReadCount());
		assertEquals(3, stepExecution.getWriteCount());
	}

	@SpringBootApplication
	public static class BatchConfiguration { 
		@Bean
		public ListItemReader<String> itemReader() {
			return new ListItemReader<>(Arrays.asList("foo", "bar", "baz"));
		}

		@Bean
		public ItemWriter<String> itemWriter() {
			return (list -> {
				list.forEach(System.out::println);
			});
		}

		@Bean
		public Step step1(
				final JobRepository jobRepository,
				final PlatformTransactionManager transactionManager 
				) {
			return new StepBuilder("step1", jobRepository)
					.<String, String>chunk(10, transactionManager)
					.reader(itemReader())
					.writer(itemWriter())
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
