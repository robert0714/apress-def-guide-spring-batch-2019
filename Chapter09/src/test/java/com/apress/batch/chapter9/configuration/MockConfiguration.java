package com.apress.batch.chapter9.configuration;
 
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MockConfiguration {

	@Bean
	public org.springframework.mail.MailSender mailSender(){
		return Mockito.mock(org.springframework.mail.MailSender.class);
	}
}
