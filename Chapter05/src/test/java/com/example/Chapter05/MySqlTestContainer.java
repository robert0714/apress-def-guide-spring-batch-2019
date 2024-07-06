package com.example.Chapter05;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
 
@Testcontainers
public interface MySqlTestContainer {

    String DOCKER_IMAGE_NAME = "mysql:8.3.0";
    String INIT_SCRIPT = "db/schema.sql";

    @Container
    @ServiceConnection
    MySQLContainer container = (MySQLContainer) new MySQLContainer(DOCKER_IMAGE_NAME)
//            .withInitScript(INIT_SCRIPT)
            ;

//    why comment out here follow by github issue https://github.com/spring-projects/spring-boot/issues/35629
//    official https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#features.testcontainers.at-development-time.importing-container-declarations
//    @DynamicPropertySource // this does the magic
//    static void setUp(DynamicPropertyRegistry registry) {
//        registry.add("jdbc.driverClassName", mysql::getDriverClassName);
//        registry.add("jdbc.url", mysql::getJdbcUrl);
//        registry.add("jdbc.username", mysql::getUsername);
//        registry.add("jdbc.password", mysql::getPassword);
//    }
}
