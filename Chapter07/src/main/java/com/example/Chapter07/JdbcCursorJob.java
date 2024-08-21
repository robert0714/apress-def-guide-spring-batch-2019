package com.example.Chapter07;
 
import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.Chapter07.domain.Customer;
import com.example.Chapter07.domain.CustomerRowMapper;

@SpringBootApplication
@ConditionalOnProperty(prefix = "main", name = "scenario", havingValue = "jdbcCursorJob")
public class JdbcCursorJob {

	@Bean
	public JdbcCursorItemReader<Customer> customerItemReader(DataSource dataSource) {
		return new JdbcCursorItemReaderBuilder<Customer>()
				.name("customerItemReader")
				.dataSource(dataSource)
				.sql("select * from customer where city = ?")
				.rowMapper(new CustomerRowMapper())
				.preparedStatementSetter(citySetter(null))
				.build();
	}

	@Bean
	@StepScope
	public ArgumentPreparedStatementSetter citySetter(
			@Value("#{jobParameters['city']}") String city) {

		return new ArgumentPreparedStatementSetter(new Object [] {city});
	}
	
	@Bean
	public ItemWriter<Customer> itemWriter() {
		return (items) -> items.forEach(System.out::println);
	}

	@Bean
	public Step copyFileStep(
			final JobRepository jobRepository,
			final PlatformTransactionManager transactionManager 
			) {
		return new StepBuilder("copyFileStep", jobRepository)
				.<Customer, Customer>chunk(10, transactionManager)
				.reader(customerItemReader( null))
				.writer(itemWriter())
				.build();
	}

	@Bean
	public Job job(
			JobRepository jobRepository,
			org.springframework.batch.core.Step copyFileStep
			) {
		return new JobBuilder("job", jobRepository)
				.start(copyFileStep)
				.build();
	}
	public static void main(String[] args) {

		SpringApplication.run(HibernateCursorJob.class, "city=Chicago");
	}

}

