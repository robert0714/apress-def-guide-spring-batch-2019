package com.apress.batch.chapter9.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.utility.DockerImageName; 
  

@TestConfiguration
public class TestNeo4jApplication {
  public static Neo4jContainer<?>  container ; 
  @Bean 
  @ServiceConnection
  public Neo4jContainer<?>  neo4jContainer() {
	 this.container =new Neo4jContainer<>(DockerImageName.parse("neo4j:5.22"))
			 .withEnv("TZ", "Asia/Taipei")  
			 .withEnv("NEO4J_AUTH", "neo4j/mminella1")  
			 .withEnv("NEO4J_dbms_connector_bolt_advertised__address", "localhost:7687")   
        //  .withAdminPassword("somePassword")
       //	.withoutAuthentication() // Disable password
			 ;
  	 return container;
  } 
	
  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
      registry.add("spring.neo4j.uri", container::getBoltUrl);  
      registry.add("spring.neo4j.authentication.password", container::getAdminPassword); 
  }
}
