package com.example.Chapter06;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.example.Chapter06.RestApplication.JobLaunchRequest;  
@SpringBatchTest
@SpringBootTest(args = { "transactionFile=input/transactionFile.csv",
		                 "summaryFile=file:///tmp/summaryFile3.csv" }, 
                properties = { "spring.batch.job.name=job",
                		       "main.scenario=restJob",
				               "spring.batch.job.enabled=false" },
                classes = RestApplication.class)
public class RestApplicationTest {
	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	@Autowired
	private JobRepositoryTestUtils jobRepositoryTestUtils;
	
	@Autowired
	private TestRestTemplate testRestTemplate; 
	
	@BeforeEach
	protected void setUp() throws Exception {
//		assertThat(MySqlTestContainer.container.isRunning()).isTrue();
	}

	@AfterEach
	public void cleanUp() {
		jobRepositoryTestUtils.removeJobExecutions(); 
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
	@Test
	public void testRunJob() {
		String uri = "/run";
		final  JobLaunchRequest request = null;		 
		assertNotNull( request ); 
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Requested-With", "XMLHttpRequest");
		 
		
		HttpEntity<Object>  httpObjectEntity = new HttpEntity<Object>(request, headers);
		ResponseEntity<String> responseEntity = testRestTemplate.postForEntity(uri, httpObjectEntity, String.class);
		assertEquals(200, responseEntity.getStatusCode().value());
		
		final String responseBody = responseEntity.getBody() ;
		
		assertNotNull( responseBody );
		 
	}
}
