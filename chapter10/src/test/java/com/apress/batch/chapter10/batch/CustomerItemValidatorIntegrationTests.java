/*
 * Copyright 2018 the original author or authors.
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
package com.apress.batch.chapter10.batch;

import javax.sql.DataSource;

import com.apress.batch.chapter10.Chapter10Application;
import com.apress.batch.chapter10.configuration.ImportJobConfiguration;
import com.apress.batch.chapter10.domain.CustomerUpdate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test; 

import org.springframework.batch.item.validator.ValidationException;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup; 
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Michael Minella
 */
@SpringBatchTest
@SpringBootTest( 
               properties = {   
               		       "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration,org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration",
               		       "spring.batch.job.enabled=false" },
               classes =  Chapter10Application.class  )
@SpringJUnitConfig({ImportJobConfiguration.class ,CustomerItemValidator.class,AccountItemProcessor.class  }) 

public class CustomerItemValidatorIntegrationTests {

	@Autowired
	private DataSource dataSource;

	private CustomerItemValidator customerItemValidator;

	@BeforeEach
	public void setUp() {
		this.customerItemValidator = new CustomerItemValidator(this.dataSource);
	}

	@SqlGroup({
		@Sql(scripts = { "classpath:schema-mysql.sql" , "classpath:data.sql" },  
				executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(scripts = {"classpath:schema-drop.sql"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD) })
	@Test 
	public void testNoCustomers() {
		CustomerUpdate customerUpdate = new CustomerUpdate(-5L);
		ValidationException exception =
				assertThrows(ValidationException.class,
						() -> this.customerItemValidator.validate(customerUpdate));
		assertEquals("Customer id -5 was not able to be found",
				exception.getMessage());

	}

	@SqlGroup({
		@Sql(scripts = { "classpath:schema-mysql.sql" , "classpath:data.sql" },  
				executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(scripts = {"classpath:schema-drop.sql"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD) })
	@Test 
	public void testCustomers() {
		CustomerUpdate customerUpdate = new CustomerUpdate(5L);
		this.customerItemValidator.validate(customerUpdate);
	}
}
