package com.example.Chapter07;
 
import java.util.Collections;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.MongoItemReader;
import org.springframework.batch.item.data.MongoPagingItemReader;
import org.springframework.batch.item.data.builder.MongoPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.Chapter07.domain.Customer;

/**
 * To load the data for this job into MongoDB, unzip the zip file src/main/resources/input/tweets.zip.  Then execute
 * the following command:
 * <code>mongorestore -d tweets -c tweets_collection &lt;PATH_TO_UNZIPED_DIR&gt;/dump\ 2/twitter/tweets.bson</code>
 */

@SpringBootApplication
@ConditionalOnProperty(prefix = "main", name = "scenario", havingValue = "mongoDbJob")
public class MongoDbJob { 
	
	@Bean
	@StepScope
	public MongoPagingItemReader<Map> tweetsItemReader(MongoOperations mongoTemplate,
			@Value("#{jobParameters['hashTag']}") String hashtag) {
		return new MongoPagingItemReaderBuilder<Map>()
				.name("tweetsItemReader")
				.targetType(Map.class)
				.jsonQuery("{ \"entities.hashtags.text\": { $eq: ?0 }}")
				.collection("tweets_collection")
				.parameterValues(Collections.singletonList(hashtag))
				.pageSize(10)
				.sorts(Collections.singletonMap("created_at", Sort.Direction.ASC))
				.template(mongoTemplate)
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
				.reader(tweetsItemReader( null,null))
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

		SpringApplication.run(MongoDbJob.class, "hashTag=nodejs");
	}

}

