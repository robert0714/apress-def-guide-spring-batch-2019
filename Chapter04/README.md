# Chapter 4. Understanding Jobs and Steps

## HelloWorldJob
* See [HelloWorldJob](src/main/java/com/example/Chapter04/jobs/HelloWorldJob.java)
 * Configuration Class [HelloWorldConfig](src/main/java/com/example/Chapter04/jobs/config/HelloWorldConfig.java)
 * Test Class: [HelloWorldJobTest](src/test/java/com/example/Chapter04/jobs/HelloWorldJobTest.java)
 * Incrementing Job Parameters
   * [DailyJobTimestamper](src/main/java/com/example/Chapter04/batch/DailyJobTimestamper.java)
 * Working with Job Listeners  
   * example: [JobLoggerListener](src/main/java/com/example/Chapter04/batch/JobLoggerListener.java)
   * This allows you to utilize these callbacks for a number of different use cases :
       * **Notifications**: Spring Cloud Task 4 provides a JobExecutionListener that emits messages over a message queue notifying other systems that a job has started or ended.
       * **Initialization**: If there are some preparations that need to occur prior to the execution of the job, the beforeJob is a good place to execute that logic.
       * **Cleanup**: Many jobs have cleanup that needs to occur after it has run (delete or archive files, etc.). This cleanup shouldn’t impact the success/failure indications of the job, but still need to be executed. The afterJob is a perfect place to handle these use cases.
   * There are two ways to create a job listener . 
       * The first is by implementing the `org.springframework.batch.core.JobExecutionListener` interface. This interface has two methods of consequence: `beforeJob` and `afterJob` . Each takes JobExecution as a parameter, and they’re executed—you guessed it, before the job executes and after the job executes, respectively. One important thing to note about the afterJob method is that it’s called regardless of the status the job finishes in. Because of this, you may need to evaluate the status in which the job ended to determine what to do. 
       * As with just about everything in Spring these days, if you can implement an interface for something, there are probably annotations that will make your life easier. Creating listeners is no exception to that. Spring Batch provides the `@BeforeJob` and `@AfterJob` annotations just for that use. 
 * Manipulating the ExecutionContext:
   * See [HelloWorldTasklet](src/main/java/com/example/Chapter04/batch/HelloWorldTasklet.java)
 * ExecutionContext Persistence
   * See Configuration Class [HelloWorldConfig](src/main/java/com/example/Chapter04/jobs/config/HelloWorldConfig.java?plain=1#L26-L38)  
### Job Parameters
The below example shows how to pass parameters to a job using the way you’ve been calling jobs up to this point .
```bash
java –jar demo.jar name=Michael
```
 In order to utilize the conversions, you tell Spring Batch the parameter type in parentheses after the parameter name, as shown in Listing 4-6. Notice that Spring Batch requires that the name of each be all lowercase .
```bash
java –jar demo.jar executionDate(date)=2017/11/28
```
### Validating Job Parameters
* See Configuration Class [HelloWorldConfig](src/main/java/com/example/Chapter04/jobs/config/HelloWorldConfig.java?plain=1#L26-L38) 
```bash
java -jar target/Chapter04-0.0.1-SNAPSHOT.jar fileName=foo.csv name=Michael
```