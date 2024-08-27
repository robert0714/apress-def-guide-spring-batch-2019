package com.apress.batch.chapter9.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.activemq.ActiveMQContainer;
import org.testcontainers.activemq.ArtemisContainer;

@TestConfiguration
public class TestActiveMQApplication {
	public static ArtemisContainer container;

	@Bean
	@ServiceConnection
	public ArtemisContainer activeMQContainer() {
		this.container =
//			 new ActiveMQContainer ("apache/activemq-classic:5.18.3")
				new ArtemisContainer("apache/activemq-artemis:2.30.0-alpine")
//			 .withUser("testcontainers")
//			 .withPassword("testcontainers")
						.withEnv("TZ", "Asia/Taipei");
		return container;
	}

	@DynamicPropertySource
	static void registerProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.activemq.broker-url", container::getBrokerUrl);
		registry.add("spring.activemq.user", container::getUser);
		registry.add("spring.activemq.password", container::getPassword);
	}

}
