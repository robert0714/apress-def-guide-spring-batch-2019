package com.example.Chapter07;


 
import java.util.Collection;
import java.util.Collections; 
import java.util.Map;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.event.EventListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step; 
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.extensions.neo4j.Neo4jItemReader;
import org.springframework.batch.extensions.neo4j.builder.Neo4jItemReaderBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;
import org.springframework.data.neo4j.core.Neo4jTemplate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration; 
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.example.Chapter07.domain.Company; 

/**
 * To load the data for this job into MongoDB, unzip the zip file src/main/resources/input/tweets.zip.  Then execute
 * the following command:
 * <code>mongorestore -d tweets -c tweets_collection &lt;PATH_TO_UNZIPED_DIR&gt;/dump\ 2/twitter/tweets.bson</code>
 */  
@SpringBootApplication
@ConditionalOnProperty(prefix = "main", name = "scenario", havingValue = "neo4jJob")
public class Neo4jJob {
//	@Bean
//	public Neo4jProperties neo4jProperties(Neo4jProperties properties) {
//		return new Neo4jProperties();
//	}
	@Bean
	public org.neo4j.ogm.config.Configuration configuration(Neo4jProperties properties) {
		Neo4jTemplate neo4jTemplate =null;
   	 
    	org.neo4j.ogm.config.Configuration.Builder builder = new org.neo4j.ogm.config.Configuration.Builder();
    	builder.uri(properties.getUri().toString());
    	builder.credentials(properties.getAuthentication().getUsername() ,properties.getAuthentication().getPassword());
    	
    	org.neo4j.ogm.config.Configuration configuration =builder.build();
		return configuration;
	}
    @Bean
	public SessionFactory sessionFactory(org.neo4j.ogm.config.Configuration configurations,
			ObjectProvider<EventListener> eventListeners) {
    	
		final SessionFactory sessionFactory = new SessionFactory(configurations,
				"com.example.Chapter07.domain");
		eventListeners.stream().forEach(sessionFactory::register);
		return sessionFactory;
	}
	@Bean
	public Neo4jItemReader<Map> catagoriesBySupplierItemReader(org.neo4j.ogm.session.SessionFactory  sessionFactory) {
		return new Neo4jItemReaderBuilder<Map>()
				.name("catagoriesBySupplierItemReader")
				.startStatement("n=node(*)")
				.matchStatement("(s:Supplier)-->(:Product)-->(c:Category)")
//				.returnStatement("s.companyName, collect(distinct c.categoryName) as categories")
				.returnStatement("s.companyName, c.categoryName")
//				.returnStatement("s.companyName")
				.orderByStatement("s.companyName")
				.parameterValues(Collections.emptyMap())
				.pageSize(10)
//				.targetType(Company.class)
				.targetType(Map.class)
				.sessionFactory(sessionFactory)
//				.targetType(String.class) 
				.build();
	}

	@Bean
	public ItemWriter<Map> itemWriter() {
		return (items) -> items.forEach(System.out::println);
	}
	 
	@Bean
	public Step copyFileStep(
			final JobRepository jobRepository,
			final PlatformTransactionManager transactionManager 
			) {
		return new StepBuilder("copyFileStep", jobRepository)
				.<Map, Map>chunk(10, transactionManager)
				.reader(catagoriesBySupplierItemReader(null))
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

		SpringApplication.run(Neo4jJob.class, args);
	}

}

