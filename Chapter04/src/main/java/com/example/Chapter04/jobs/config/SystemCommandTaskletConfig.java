package com.example.Chapter04.jobs.config;
 
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.SystemCommandTasklet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class SystemCommandTaskletConfig {
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
		String tmpdir = System.getProperty("java.io.tmpdir");
		SystemCommandTasklet systemCommandTasklet = new SystemCommandTasklet();
		String os = System.getProperty("os.name");
		String[] cmd;
		if(os.toLowerCase().contains("windows")) { 
			cmd = new String[] { "C:\\Windows\\System32\\cmd.exe","/c" ,"del",  "tmp.txt"};
		}else { 
			cmd = new String[] { "rm","tmp.txt"};
		}
		systemCommandTasklet.setCommand(cmd);
		
		systemCommandTasklet.setTimeout(5000);
		systemCommandTasklet.setInterruptOnCancel(true);
		
		// Change this directory to something appropriate for your environment
		systemCommandTasklet.setWorkingDirectory(tmpdir);
		return systemCommandTasklet;
	}

}
