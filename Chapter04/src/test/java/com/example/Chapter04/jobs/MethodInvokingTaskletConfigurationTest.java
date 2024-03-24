package com.example.Chapter04.jobs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance; 
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
 
import com.example.Chapter04.jobs.config.MethodInvokingTaskletConfig;
@SpringBatchTest
@DirtiesContext
@SpringJUnitConfig({MethodInvokingTaskletConfig.class  }) 
@EnableAutoConfiguration
@TestPropertySource("classpath:application.properties")
class MethodInvokingTaskletConfigurationTest {

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
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        JobInstance jobInstance = jobExecution.getJobInstance();
        ExitStatus jobExitStatus = jobExecution.getExitStatus();

        assertEquals("methodInvokingJob", jobInstance.getJobName());
        assertEquals("COMPLETED", jobExitStatus.getExitCode());
    }

}
