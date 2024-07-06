package com.example.Chapter05;

import org.junit.jupiter.api.*;
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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBatchTest
@SpringBootTest(properties = { "spring.batch.job.name=importEcoinventJob" })
public class DemoApplicationTest implements MySqlTestContainer {
	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	@Autowired
	private JobRepositoryTestUtils jobRepositoryTestUtils;

	@BeforeEach
	protected void setUp() throws Exception {
		assertThat(MySqlTestContainer.container.isRunning()).isTrue();
	}

	@AfterEach
	public void cleanUp() {
		jobRepositoryTestUtils.removeJobExecutions();
	}

	@Test
	public void givenEcoinventJob_whenJobExecuted_thenSuccess(@Autowired @Qualifier("explorerJob") Job job) throws Exception {
		jobLauncherTestUtils.setJob(job);
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        JobInstance jobInstance = jobExecution.getJobInstance();
        ExitStatus jobExitStatus = jobExecution.getExitStatus();

        assertEquals("explorerJob", jobInstance.getJobName());
        assertEquals("COMPLETED", jobExitStatus.getExitCode());
	}

}
