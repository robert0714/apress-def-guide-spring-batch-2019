package com.example.Chapter04.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.example.Chapter04.jobs.config.HelloWorldConfig; 
 
@SpringBatchTest
@DirtiesContext
@SpringJUnitConfig({HelloWorldConfig.class  }) 
@EnableAutoConfiguration
@TestPropertySource("classpath:application.properties")
class HelloWorldJobTest {

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
	@Test
    public void givenCoffeeList_whenJobExecuted_thenSuccess() throws Exception {
		JobParameters param = new JobParametersBuilder()
				.addString("name", "robert")
				.addString("fileName", "HelloWorld.csv")
				.toJobParameters();
    	JobExecution jobExecution = jobLauncherTestUtils.launchJob(param);
        //JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        JobInstance jobInstance = jobExecution.getJobInstance();
        ExitStatus jobExitStatus = jobExecution.getExitStatus();

        assertEquals("basicJob", jobInstance.getJobName());
        assertEquals("COMPLETED", jobExitStatus.getExitCode());
    }

}
