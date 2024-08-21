package com.example.Chapter07;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
 

import lombok.extern.slf4j.Slf4j;
@Slf4j
@SpringBatchTest
@SpringBootTest( 
                properties = { "spring.batch.job.name=job",
                		       "main.scenario=customInputJob",
                		       "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration,org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration",
				               "spring.batch.job.enabled=false" },
                classes = CustomInputJob.class)
//@Import(value= {
//		TestMongoDBApplication.class, })
class CustomInputJobTest {

	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	@Autowired
	private JobRepositoryTestUtils jobRepositoryTestUtils; 
	
	@BeforeEach
	protected void setUp() throws Exception {
//		assertThat(TestMongoDBApplication.container.isRunning()).isTrue();
	}

	@AfterEach
	public void cleanUp() {
		jobRepositoryTestUtils.removeJobExecutions(); 
//		TestMongoDBApplication.container.close();
	}
	@Test
	public void givenJob_whenJobExecuted_thenSuccess(@Autowired @Qualifier("job") Job job) throws Exception {		
 
		jobLauncherTestUtils.setJob(job);
		
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(); 
        JobInstance jobInstance = jobExecution.getJobInstance();
        ExitStatus jobExitStatus = jobExecution.getExitStatus();

        assertEquals("job", jobInstance.getJobName());
        assertEquals("COMPLETED", jobExitStatus.getExitCode());
	} 

}
