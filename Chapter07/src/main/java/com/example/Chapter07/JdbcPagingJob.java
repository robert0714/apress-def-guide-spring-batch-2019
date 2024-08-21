package com.example.Chapter07;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step; 
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.Chapter07.domain.Customer;
import com.example.Chapter07.domain.CustomerRowMapper;

@SpringBootApplication
@ConditionalOnProperty(prefix = "main", name = "scenario", havingValue = "jdbcPagingJob")
public class JdbcPagingJob { 
	
	@Bean
	@StepScope
	public JdbcPagingItemReader<Customer> customerItemReader(DataSource dataSource,
			PagingQueryProvider queryProvider,
			@Value("#{jobParameters['city']}") String city) {

		Map<String, Object> parameterValues = new HashMap<>(1);
		parameterValues.put("city", city);

		return new JdbcPagingItemReaderBuilder<Customer>()
				.name("customerItemReader")
				.dataSource(dataSource)
				.queryProvider(queryProvider)
				.parameterValues(parameterValues)
				.pageSize(10)
				.rowMapper(new CustomerRowMapper())
				.build();
	}

	@Bean
	public SqlPagingQueryProviderFactoryBean pagingQueryProvider(DataSource dataSource) {
		SqlPagingQueryProviderFactoryBean factoryBean = new SqlPagingQueryProviderFactoryBean();

		factoryBean.setDataSource(dataSource);
		factoryBean.setSelectClause("select *");
		factoryBean.setFromClause("from customer");
		factoryBean.setWhereClause("where city = :city");
		factoryBean.setSortKey("lastName");

		return factoryBean;
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
				.reader(customerItemReader( null,null,null))
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

