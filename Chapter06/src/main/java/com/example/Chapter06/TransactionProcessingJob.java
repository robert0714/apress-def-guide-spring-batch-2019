/**
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.Chapter06;
 
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;

import com.example.Chapter06.batch.TransactionApplierProcessor;
import com.example.Chapter06.batch.TransactionReader;
import com.example.Chapter06.domain.AccountSummary;
import com.example.Chapter06.domain.Transaction;
import com.example.Chapter06.domain.TransactionDao;
import com.example.Chapter06.domain.support.TransactionDaoSupport; 

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.PassThroughFieldSetMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 339599,2018-01-13 18:53:29,884.16
 * 991085,2018-01-13 18:53:29,-15.05
 * 297579,2018-01-13 18:53:29,-344.38
 *
 * @author Michael Minella
 */
@SpringBootApplication
@ConditionalOnProperty(prefix = "main", name = "scenario", havingValue = "transactionJob") 
public class TransactionProcessingJob 
//implements CommandLineRunner 
{
	
	@Autowired
	private GenericApplicationContext context ;	  
	
	@Autowired
	private  JobLauncher jobLauncher;
	@Bean
	@StepScope
	public TransactionReader transactionReader() {
		return new TransactionReader(fileItemReader(null));
	}
	 
	
	@Bean
	@StepScope
	public FlatFileItemReader<FieldSet> fileItemReader(
			@Value("#{jobParameters['transactionFile']}") Resource inputFileResource) {
//		String reosurceStr = String .format("classpath:%s", inputFile);
//		Resource inputFileResource = this.context.getResource(reosurceStr);
		
		return new FlatFileItemReaderBuilder<FieldSet>()
				.name("fileItemReader")
				.resource(inputFileResource)
				.lineTokenizer(new DelimitedLineTokenizer())
				.fieldSetMapper(new PassThroughFieldSetMapper())
				.build();
	}

	@Bean
	public JdbcBatchItemWriter<Transaction> transactionWriter(DataSource dataSource) {
		return new JdbcBatchItemWriterBuilder<Transaction>()
				.itemSqlParameterSourceProvider(
						new BeanPropertyItemSqlParameterSourceProvider<>())
				.sql("INSERT INTO TRANSACTION " +
						"(ACCOUNT_SUMMARY_ID, TIMESTAMP, AMOUNT) " +
						"VALUES ((SELECT ID FROM ACCOUNT_SUMMARY " +
						"	WHERE ACCOUNT_NUMBER = :accountNumber), " +
						":timestamp, :amount)")
				.dataSource(dataSource)
				.build();
	}

	@Bean
	public Step importTransactionFileStep(
			final JobRepository jobRepository,
			final PlatformTransactionManager transactionManager 
			) { 
		return new StepBuilder("importTransactionFileStep", jobRepository)
				.<Transaction, Transaction> chunk(100, transactionManager)
				.reader(transactionReader())
				.writer(transactionWriter(null))
				.allowStartIfComplete(true)
				.listener(transactionReader())
				.build();
	}

	@Bean
	@StepScope
	public JdbcCursorItemReader<AccountSummary> accountSummaryReader(DataSource dataSource) {
		return new JdbcCursorItemReaderBuilder<AccountSummary>()
				.name("accountSummaryReader")
				.dataSource(dataSource)
				.sql("SELECT ACCOUNT_NUMBER, CURRENT_BALANCE " +
						"FROM ACCOUNT_SUMMARY A " +
						"WHERE A.ID IN (" +
						"	SELECT DISTINCT T.ACCOUNT_SUMMARY_ID " +
						"	FROM TRANSACTION T) " +
						"ORDER BY A.ACCOUNT_NUMBER")
				.rowMapper((resultSet, rowNumber) -> {
					AccountSummary summary = new AccountSummary();

					summary.setAccountNumber(resultSet.getString("account_number"));
					summary.setCurrentBalance(resultSet.getDouble("current_balance"));

					return summary;
				}).build();
	}

	@Bean
	public TransactionDao transactionDao(DataSource dataSource) {
		return new TransactionDaoSupport(dataSource);
	}

	@Bean
	public TransactionApplierProcessor transactionApplierProcessor() {
		return new TransactionApplierProcessor(transactionDao(null));
	}

	@Bean
	public JdbcBatchItemWriter<AccountSummary> accountSummaryWriter(DataSource dataSource) {
		return new JdbcBatchItemWriterBuilder<AccountSummary>()
				.dataSource(dataSource)
				.itemSqlParameterSourceProvider(
						new BeanPropertyItemSqlParameterSourceProvider<>())
				.sql("UPDATE ACCOUNT_SUMMARY " +
						"SET CURRENT_BALANCE = :currentBalance " +
						"WHERE ACCOUNT_NUMBER = :accountNumber")
				.build();
	}

	@Bean
	public Step applyTransactionsStep(
			final JobRepository jobRepository,
			final PlatformTransactionManager transactionManager 
			) { 
		return new StepBuilder("applyTransactionsStep", jobRepository)
				.<AccountSummary, AccountSummary>chunk(100, transactionManager)
				.reader(accountSummaryReader(null))
				.processor(transactionApplierProcessor())
				.writer(accountSummaryWriter(null))
				.build();
	}

	@Bean
	@StepScope
	public FlatFileItemWriter<AccountSummary> accountSummaryFileWriter(
			@Value("#{jobParameters['summaryFile']}") WritableResource summaryFile) {

		DelimitedLineAggregator<AccountSummary> lineAggregator =
				new DelimitedLineAggregator<>();
		BeanWrapperFieldExtractor<AccountSummary> fieldExtractor =
				new BeanWrapperFieldExtractor<>();
		fieldExtractor.setNames(new String[] {"accountNumber", "currentBalance"});
		fieldExtractor.afterPropertiesSet();
		lineAggregator.setFieldExtractor(fieldExtractor);

		return new FlatFileItemWriterBuilder<AccountSummary>()
				.name("accountSummaryFileWriter")				
				.resource(summaryFile)
				.lineAggregator(lineAggregator)
				.build();
	}

	@Bean
	public Step generateAccountSummaryStep(
			final JobRepository jobRepository,
			final PlatformTransactionManager transactionManager 
			) { 
		return new StepBuilder("generateAccountSummaryStep", jobRepository)
				.<AccountSummary, AccountSummary>chunk(100,transactionManager)
				.reader(accountSummaryReader(null))
				.writer(accountSummaryFileWriter(null))
				.build();
	}

	@Bean
	public Job transactionJob(
			JobRepository jobRepository ,
			@Qualifier("importTransactionFileStep")
    		org.springframework.batch.core.Step importTransactionFileStep,
    		@Qualifier("applyTransactionsStep")
    		org.springframework.batch.core.Step applyTransactionsStep,
    		@Qualifier("generateAccountSummaryStep")
    		org.springframework.batch.core.Step generateAccountSummaryStep
			) { 
		
		return new JobBuilder("transactionJob", jobRepository)
				.preventRestart()
				.start(importTransactionFileStep)
				.next(applyTransactionsStep)
				.next(generateAccountSummaryStep)
				.build();
		
//		return new JobBuilder("transactionJob", jobRepository)
//				.start(importTransactionFileStep)
//				.on("STOPPED").stopAndRestart(importTransactionFileStep)
//				.from(importTransactionFileStep).on("*").to(applyTransactionsStep)
//				.from(applyTransactionsStep).next(generateAccountSummaryStep)
//				.end()
//				.build();
		
	}

	public static void main(String[] args) {
		List<String> realArgs = new ArrayList<>(Arrays.asList(args));
		
//		ClassLoader classLoader = TransactionProcessingJob.class.getClassLoader();
//		URL inputUrl = classLoader.getResource("input/transactionFile.csv"); 		
//		String inputFile = inputUrl.getFile();
//		String transactionFileArgs = String.format("transactionFile=%s", inputFile) ;
//		
//		realArgs.add(transactionFileArgs);
//		
//		String tmpdir = System.getProperty("java.io.tmpdir");		
//		String summaryFileArgs = String.format("summaryFile=file://%s/summaryFile3.csv", tmpdir) ;	
//		
//		realArgs.add(summaryFileArgs);
        
        realArgs.add("transactionFile=input/transactionFile.csv");
        realArgs.add("summaryFile=file:///tmp/summaryFile3.csv");

		SpringApplication.run(TransactionProcessingJob.class, realArgs.toArray(new String[realArgs.size()]));
	} 
	
//	public void run(String... args) throws Exception {
//		JobParametersBuilder builder = new JobParametersBuilder() ;
//		if(args!=null && args.length > 0 ) {
//			for (String arg :args ) {
//				String [] lines= arg.split("=");
//				builder.addString(lines[0], lines[1]);
//			} 
//			JobParameters jobParameters = builder.toJobParameters();
//			
//			var job = context.getBean("transactionJob", Job.class); 
//			
//			JobExecution execution = jobLauncher.run(job, jobParameters);
//			System.out.println("STATUS :: " + execution.getStatus());
//		}
//	}
}
