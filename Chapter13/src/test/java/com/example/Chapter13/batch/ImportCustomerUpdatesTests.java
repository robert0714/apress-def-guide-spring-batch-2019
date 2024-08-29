/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.Chapter13.batch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
 
import com.example.Chapter13.DemoApplication;
import com.example.Chapter13.configuration.ImportJobConfiguration;
import com.example.Chapter13.domain.CustomerUpdate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test; 

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.batch.item.validator.ValidatingItemProcessor;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.StepScopeTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate; 
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup; 
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig; 

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Michael Minella
 * https://github.com/eugenp/tutorials/blob/master/spring-batch/src/test/java/com/baeldung/batchtesting/SpringBatchStepScopeIntegrationTest.java<p>
 * https://docs.spring.io/spring-batch/reference/testing.html<p>
 */   
@SpringBatchTest
@SpringBootTest( 
               properties = {   
               		       "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration,org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration",
               		       "spring.batch.job.enabled=false" } ,
               classes =  DemoApplication.class  )
@SpringJUnitConfig({ImportJobConfiguration.class ,CustomerItemValidator.class,AccountItemProcessor.class  }) 
@TestPropertySource(properties = "debug=true")
public class ImportCustomerUpdatesTests {

	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	@Autowired
	private DataSource dataSource;

	private JdbcOperations jdbcTemplate;

	@Autowired
	private FlatFileItemReader<CustomerUpdate> customerUpdateItemReader;
	
	@Autowired
	private ValidatingItemProcessor<CustomerUpdate> customerValidatingItemProcessor;	
	
	@Autowired
	private ClassifierCompositeItemWriter<CustomerUpdate> customerUpdateItemWriter;

	@BeforeEach
	public void setUp() {
		this.jdbcTemplate = new JdbcTemplate(this.dataSource);
	}
	 
	
	@SqlGroup({
		@Sql(scripts = { "classpath:schema-mysql.sql" , "classpath:data.sql" },  
				executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(scripts = {"classpath:schema-drop.sql"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD) })
	@Test 
    public void givenMockedStepDetail_whenReaderCalled_thenSuccess() throws Exception {		 
		// given 
        StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution(defaultJobParameters());
       
        // when
		StepScopeTestUtils.doInStepScope(stepExecution, () -> {
			CustomerUpdate line;
			customerUpdateItemReader.open(stepExecution.getExecutionContext());
			while ((line = customerUpdateItemReader.read()) != null) {
				// then
				long id = line.getCustomerId();
				assertEquals(5l, id);

				CustomerUpdate item = customerValidatingItemProcessor.process(line);
				customerUpdateItemWriter.write(new Chunk<>(List.of(item)));
			}
			customerUpdateItemReader.close();
			return null;
		});
       
		verifyData();
    }  
	
	@SqlGroup({
		@Sql(scripts = { "classpath:schema-mysql.sql" , "classpath:data.sql" },  
				executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(scripts = {"classpath:schema-drop.sql"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD) })
	@Test  
    public void givenMockedStep_whenReaderCalled_thenSuccess() throws Exception {		 
		// given 
		JobExecution jobExecution = this.jobLauncherTestUtils.launchStep("importCustomerUpdates", defaultJobParameters());

		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
       
		verifyData();
    } 
	@SqlGroup({
		@Sql(scripts = { "classpath:schema-mysql.sql" , "classpath:data.sql" },  
				executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(scripts = {"classpath:schema-drop.sql"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD) })
	@Test
	public void test(@Autowired @Qualifier("importJob") Job job) throws Exception {
		 
		// given.
		JobParameters jobParameters = defaultJobParameters(); 
		
		jobLauncherTestUtils.setJob(job);
        
        // when
        JobExecution jobExecution = this.jobLauncherTestUtils.launchJob(jobParameters); 
        
		JobInstance jobInstance = jobExecution.getJobInstance();
        ExitStatus jobExitStatus = jobExecution.getExitStatus(); 
        
        assertEquals("importJob", jobInstance.getJobName());
        assertEquals("COMPLETED", jobExitStatus.getExitCode());
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());

        verifyData();
	}
	private void verifyData() {
		List<Map<String, String>> results = this.jdbcTemplate.query("select * from customer where customer_id = 5",
				(rs, rowNum) -> {
					Map<String, String> item = new HashMap<>();

					item.put("customer_id", rs.getString("customer_id"));
					item.put("first_name", rs.getString("first_name"));
					item.put("middle_name", rs.getString("middle_name"));
					item.put("last_name", rs.getString("last_name"));
					item.put("address1", rs.getString("address1"));
					item.put("address2", rs.getString("address2"));
					item.put("city", rs.getString("city"));
					item.put("state", rs.getString("state"));
					item.put("postal_code", rs.getString("postal_code"));
					item.put("ssn", rs.getString("ssn"));
					item.put("email_address", rs.getString("email_address"));
					item.put("home_phone", rs.getString("home_phone"));
					item.put("cell_phone", rs.getString("cell_phone"));
					item.put("work_phone", rs.getString("work_phone"));
					item.put("notification_pref", rs.getString("notification_pref"));

					return item;
				});

		Map<String, String> result = results.get(0);

		assertEquals("5", result.get("customer_id"));
		assertEquals("Rozelle", result.get("first_name"));
		assertEquals("Heda", result.get("middle_name"));
		assertEquals("Farnill", result.get("last_name"));
		assertEquals("36 Ronald Regan Terrace", result.get("address1"));
		assertEquals("P.O. Box 33", result.get("address2"));
		assertEquals("Montgomery", result.get("city"));
		assertEquals("Alabama", result.get("state"));
		assertEquals("36134", result.get("postal_code"));
		assertEquals("832-86-3661", result.get("ssn"));
		assertEquals("tlangelay4@mac.com", result.get("email_address"));
		assertEquals("240-906-7652", result.get("home_phone"));
		assertEquals("907-709-2649", result.get("cell_phone"));
		assertEquals("316-510-9138", result.get("work_phone"));
		assertEquals("2", result.get("notification_pref"));
		
	}
    private JobParameters defaultJobParameters() {
        JobParametersBuilder paramsBuilder = new JobParametersBuilder();
        paramsBuilder.addString("customerUpdateFile", "classpath:customerUpdateFile.csv"); 
        paramsBuilder.addString("transactionFile", "classpath:transactions.xml"); 
        paramsBuilder.addString("outputDirectory", "file:///tmp/"); 
        
        return paramsBuilder.toJobParameters();
    }
}
