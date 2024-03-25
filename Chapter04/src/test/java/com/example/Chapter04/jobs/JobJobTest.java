package com.example.Chapter04.jobs;

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
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.example.Chapter04.jobs.config.JobJobConfig;

@SpringBatchTest
@DirtiesContext
@SpringJUnitConfig({JobJobConfig.class  }) 
@EnableAutoConfiguration
@TestPropertySource("classpath:application.properties")
@SpringBootTest(properties = { "spring.batch.job.name=conditionalStepLogicJob" })
class JobJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils; 

    
	@BeforeEach
	protected void setUp() throws Exception {
	}

	@AfterEach
	protected void tearDown() throws Exception {
		 jobRepositoryTestUtils.removeJobExecutions();
	}
	//https://docs.spring.io/spring-batch/reference/testing.html
	@Test
    public void givenPreProcessingJob_whenJobExecuted_thenSuccess(@Autowired @Qualifier("preProcessingJob") Job job) throws Exception { 
		jobLauncherTestUtils.setJob(job);
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        JobInstance jobInstance = jobExecution.getJobInstance();
        ExitStatus jobExitStatus = jobExecution.getExitStatus();

        assertEquals("preProcessingJob", jobInstance.getJobName());
        assertEquals("COMPLETED", jobExitStatus.getExitCode());
    }

}
