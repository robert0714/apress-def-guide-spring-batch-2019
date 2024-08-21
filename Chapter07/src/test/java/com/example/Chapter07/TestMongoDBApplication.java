package com.example.Chapter07;
 
  
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean; 
import org.testcontainers.containers.MongoDBContainer; 
  

@TestConfiguration
public class TestMongoDBApplication {
  public static MongoDBContainer container ; 
  @Bean 
  @ServiceConnection
  public MongoDBContainer  mongoDBContainer() {
	 this.container = new MongoDBContainer ("mongo:4.0.10")
//			 .withEnv("EXTRA_OPTS", "\"--lower_case_table_names=1\"")
			 .withEnv("TZ", "Asia/Taipei") 
//			 .withPassword("") 
			 ;
  	 return container;
  } 
	
		 
}
