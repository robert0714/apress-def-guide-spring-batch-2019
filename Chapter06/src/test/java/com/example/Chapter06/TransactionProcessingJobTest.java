package com.example.Chapter06;

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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
 

@SpringBatchTest
@SpringBootTest(args = { "transactionFile=classpath:/input/transactionFile.csv",
		                 "summaryFile=file:///tmp/summaryFile3.csv" }, 
                properties = { "spring.batch.job.name=transactionJob",
                		       "main.scenario=transactionJob",
				               "spring.batch.job.enabled=true" },
                classes = TransactionProcessingJob.class)
public class TransactionProcessingJobTest {
	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	@Autowired
	private JobRepositoryTestUtils jobRepositoryTestUtils;
	


	@BeforeEach
	protected void setUp() throws Exception {
//		assertThat(MySqlTestContainer.container.isRunning()).isTrue();
	}

	@AfterEach
	public void cleanUp() {
		jobRepositoryTestUtils.removeJobExecutions(); 
	}

	@Test
	@SqlGroup({
        @Sql(scripts = {"classpath:schema-mysql.sql"},
                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD) ,
//        @Sql(scripts = {"classpath:data-mysql.sql"},
//                executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD) 
    })
	public void givenTransactionJob_whenJobExecuted_thenSuccess(@Autowired @Qualifier("transactionJob") Job job) throws Exception {		
		JobParameters jprarms = composeParameters(); 
		jobLauncherTestUtils.setJob(job);
		
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jprarms); 
//		JobExecution jobExecution = jobLauncherTestUtils.launchJob();  
        JobInstance jobInstance = jobExecution.getJobInstance();
        ExitStatus jobExitStatus = jobExecution.getExitStatus();

        assertEquals("transactionJob", jobInstance.getJobName());
        assertEquals("COMPLETED", jobExitStatus.getExitCode());
	}
	protected JobParameters composeParameters() {
        JobParametersBuilder jpBuilder =  new JobParametersBuilder() ; 		
		jpBuilder.addString("transactionFile", "classpath:/input/transactionFile.csv");
		
		String tmpdir = System.getProperty("java.io.tmpdir");	
		String summaryFileArgs = String.format("file://%ssummaryFile3.csv", tmpdir) ;
		
		jpBuilder.addString("summaryFile", summaryFileArgs);
		
		JobParameters jobParameters = jpBuilder.toJobParameters();
		
		return jobParameters ; 
	}
	 

}
