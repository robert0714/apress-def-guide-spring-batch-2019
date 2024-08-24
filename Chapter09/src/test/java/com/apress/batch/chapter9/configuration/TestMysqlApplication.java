package com.apress.batch.chapter9.configuration;
 
 
import org.springframework.boot.SpringApplication;  
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer; 

import com.apress.batch.chapter9.Chapter9Application;
 
  

@TestConfiguration
public class TestMysqlApplication {
  public static MySQLContainer<?>  container ; 
  
  @Bean 
  @ServiceConnection
  public MySQLContainer<?> mySQLContainer() {
	 this.container = 
  	  new MySQLContainer<>("mysql:8.0.37")
  			 .withUsername("root")
  			 .withEnv("EXTRA_OPTS", "\"--lower_case_table_names=1\"")
  			 .withEnv("TZ", "Asia/Taipei")
//  			 .withPassword("") 
  			 ;
	 return this.container;
  } 
  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", container::getJdbcUrl);
		registry.add("spring.datasource.url", () -> container.getJdbcUrl());
		registry.add("spring.datasource.username", () -> container.getUsername());
		registry.add("spring.datasource.password", () -> container.getPassword());
		registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
  }
  public static void main(String[] args) {
		SpringApplication.from(Chapter9Application::main)
	  		.with(TestMysqlApplication.class)
	  		.run(args);
	}
		 
}
