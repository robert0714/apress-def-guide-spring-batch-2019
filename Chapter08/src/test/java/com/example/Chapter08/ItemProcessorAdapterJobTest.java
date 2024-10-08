package com.example.Chapter08;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBatchTest
@SpringBootTest(args = { "customerFile=classpath:/input/customer.csv"}, 
                properties = { "spring.batch.job.name=job",
                               "main.scenario=itemProcessorAdapterJob",
				               "spring.batch.job.enabled=false" },
                classes = ItemProcessorAdapterJob.class)
class ItemProcessorAdapterJobTest {

	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	@Autowired
	private JobRepositoryTestUtils jobRepositoryTestUtils; 
	
	@BeforeEach
	protected void setUp() throws Exception { 
	}

	@AfterEach
	public void cleanUp() {
		jobRepositoryTestUtils.removeJobExecutions();  
	}
	
	@Test 
	public void givenJob_whenJobExecuted_thenSuccess(@Autowired @Qualifier("job") Job job) throws Exception {		
		JobParameters jprarms = composeParameters(); 
		jobLauncherTestUtils.setJob(job);
		
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jprarms);  
        JobInstance jobInstance = jobExecution.getJobInstance();
        ExitStatus jobExitStatus = jobExecution.getExitStatus();

        assertEquals("job", jobInstance.getJobName());
        assertEquals("COMPLETED", jobExitStatus.getExitCode());
	} 
	protected JobParameters composeParameters() {
        JobParametersBuilder jpBuilder =  new JobParametersBuilder() ; 		
		jpBuilder.addString("customerFile", "classpath:/input/customer.csv");

		JobParameters jobParameters = jpBuilder.toJobParameters();
		
		return jobParameters ; 
	}

}
