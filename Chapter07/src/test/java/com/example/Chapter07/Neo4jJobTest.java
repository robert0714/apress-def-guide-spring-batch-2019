package com.example.Chapter07;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.net.URI;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance; 
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties.Authentication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
 

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBatchTest
@SpringBootTest(  
                properties = { "spring.batch.job.name=job",
                		       "main.scenario=neo4jJob",
                               "spring.batch.job.enabled=false" },
                classes = Neo4jJob.class)
@Import(value= {
		TestNeo4jApplication.class, TestMongoDBApplication.class,})
class Neo4jJobTest {

	@Autowired
	private Neo4jProperties properties;
	
	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	@Autowired
	private JobRepositoryTestUtils jobRepositoryTestUtils; 
	
	@BeforeEach
	protected void setUp() throws Exception {
		assertThat(TestNeo4jApplication.container.isRunning()).isTrue();
		assertThat(TestMongoDBApplication.container.isRunning()).isTrue();
//		URI uri = mock(URI.class);
//		Authentication auth =  mock(Authentication.class);		
//		String authData = TestNeo4jApplication.container.getEnvMap().get("NEO4J_AUTH");
//		String user = authData.split("//")[0];
//		String passwd = authData.split("//")[1];
//		when(auth.getUsername()).thenReturn(user);
//		when(auth.getPassword()).thenReturn(passwd);
//		when(properties.getAuthentication()).thenReturn(auth);
//		when(uri.toString()).thenReturn(TestNeo4jApplication.container.getBoltUrl());
//		when(properties.getUri()).thenReturn(uri);
	}

	@AfterEach
	public void cleanUp() {
		jobRepositoryTestUtils.removeJobExecutions(); 
		TestNeo4jApplication.container.close();
		TestMongoDBApplication.container.close();
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