package com.apress.batch.chapter9.configuration;

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
import org.springframework.context.annotation.Import;

import com.apress.batch.chapter9.Chapter9Application;
@SpringBatchTest
@SpringBootTest(args = { "customerFile=classpath:/data/customerWithEmail.csv",
		                 "outputFile=file:/tmp/customer.xml"
               }, 
               properties = { "spring.batch.job.name=multiFlatFileGeneratorJob",
               		       "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration,org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration",
//               		       "logging.level.org.springframework=DEBUG",
//               		       "logging.level.org.hibernate.engine.transaction.jta=DEBUG",
                              "main.scenario=multiFlatFileGeneratorJob",
                              "spring.job.names=multiFlatFileGeneratorJob",
				              "spring.batch.job.enabled=false" },
               classes =  Chapter9Application.class  )
@Import(value= {
//		TestMysqlApplication.class,
		MultiResourceHeaderFooterJob.class , MockConfiguration.class })
class MultiResourceHeaderFooterJobTest {

	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	@Autowired
	private JobRepositoryTestUtils jobRepositoryTestUtils; 
	
	@BeforeEach
	protected void setUp() throws Exception {
//		assertThat(TestMysqlApplication.container.isRunning()).isTrue();
	}

	@AfterEach
	public void cleanUp() {
		jobRepositoryTestUtils.removeJobExecutions(); 
//		TestMysqlApplication.container.close();
	}

//	@SqlGroup({
//			@Sql(scripts = { "classpath:schema-mysql.sql" }, 
//					executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD) })
	@Test 
	public void givenJob_whenJobExecuted_thenSuccess(@Autowired @Qualifier("multiFlatFileGeneratorJob") Job job) throws Exception {		
		JobParameters jprarms = composeParameters(); 
		jobLauncherTestUtils.setJob(job);
		
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jprarms);  
        JobInstance jobInstance = jobExecution.getJobInstance();
        ExitStatus jobExitStatus = jobExecution.getExitStatus();

        assertEquals("multiFlatFileGeneratorJob", jobInstance.getJobName());
        assertEquals("COMPLETED", jobExitStatus.getExitCode());
	} 
	protected JobParameters composeParameters() {
        JobParametersBuilder jpBuilder =  new JobParametersBuilder() ; 		
		jpBuilder.addString("customerFile", "classpath:/data/customerWithEmail.csv"); 
		jpBuilder.addString("outputFile", "file:/tmp/customer.xml");  
		JobParameters jobParameters = jpBuilder.toJobParameters();
		
		return jobParameters ; 
	}

}