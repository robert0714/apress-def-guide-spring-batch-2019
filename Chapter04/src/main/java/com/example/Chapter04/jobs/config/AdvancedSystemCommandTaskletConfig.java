package com.example.Chapter04.jobs.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.SimpleSystemProcessExitCodeMapper;
import org.springframework.batch.core.step.tasklet.SystemCommandTasklet; 
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class AdvancedSystemCommandTaskletConfig {
	@Bean
	public Job job(JobRepository jobRepository ,Step systemCommandStep) {		
		return  new JobBuilder("systemCommandJob", jobRepository)
				.start(systemCommandStep) 
				.build(); 
	}

	@Bean
	public Step systemCommandStep(final JobRepository jobRepository,final PlatformTransactionManager transactionManager) {
        return new StepBuilder("systemCommandStep" ,jobRepository)				
    			.tasklet(systemCommandTasklet(), 
    					transactionManager)
    			.build();
	}

	@Bean
	public SystemCommandTasklet systemCommandTasklet() {
		SystemCommandTasklet tasklet = new SystemCommandTasklet();
		String os = System.getProperty("os.name");
		String tmpdir = System.getProperty("java.io.tmpdir");
		String[] cmd;
		if(os.toLowerCase().contains("windows")) { 
			cmd = new String[] { "C:\\Windows\\System32\\cmd.exe","/c" ,"echo", ">", "tmp.txt"};
		}else { 
			cmd = new String[] { "touch","tmp.txt"};
		}
		tasklet.setCommand(cmd);
		tasklet.setTimeout(5000);
		tasklet.setInterruptOnCancel(true);

		// Change this directory to something appropriate for your environment
		tasklet.setWorkingDirectory(tmpdir);
		

		tasklet.setSystemProcessExitCodeMapper(touchCodeMapper());
		tasklet.setTerminationCheckInterval(5000);
		tasklet.setTaskExecutor(new SimpleAsyncTaskExecutor());


		if(os.toLowerCase().contains("windows")) { 
			String javaHome = System.getProperty("JAVA_HOME");
			tasklet.setEnvironmentParams(new String[] {String.format("JAVA_HOME=%s", javaHome) });			
		}else { 
			tasklet.setEnvironmentParams(new String[] {
			"JAVA_HOME=/java",
			"BATCH_HOME=/Users/batch"});
		}
		return tasklet;
	}

	@Bean
	public SimpleSystemProcessExitCodeMapper touchCodeMapper() {
		return new SimpleSystemProcessExitCodeMapper();
	}

}
