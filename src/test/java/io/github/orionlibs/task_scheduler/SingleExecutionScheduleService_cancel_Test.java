package io.github.orionlibs.task_scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.orionlibs.task_scheduler.config.ConfigurationService;
import io.github.orionlibs.task_scheduler.log.ListLogHandler;
import io.github.orionlibs.task_scheduler.utils.Callback;
import io.github.orionlibs.task_scheduler.utils.RunnableExample;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@TestInstance(Lifecycle.PER_METHOD)
//@Execution(ExecutionMode.CONCURRENT)
public class SingleExecutionScheduleService_cancel_Test
{
    private ListLogHandler listLogHandler;
    private SingleExecutionScheduleService singleExecutionScheduler;
    private ConfigurationService config;
    private RunnableExample runnableExample1;
    private RunnableExample runnableExample2;
    private RunnableExample runnableExample3;
    private Callback callback;


    @BeforeEach
    void setUp() throws IOException
    {
        singleExecutionScheduler = new SingleExecutionScheduleService();
        config = singleExecutionScheduler.getConfig();
        listLogHandler = new ListLogHandler();
        singleExecutionScheduler.addLogHandler(listLogHandler);
        runnableExample1 = new RunnableExample();
        runnableExample2 = new RunnableExample();
        runnableExample3 = new RunnableExample();
        RunnableExample.addLogHandler(listLogHandler);
        callback = new Callback();
        Callback.addLogHandler(listLogHandler);
    }


    @AfterEach
    public void teardown()
    {
        singleExecutionScheduler.removeLogHandler(listLogHandler);
        RunnableExample.removeLogHandler(listLogHandler);
        Callback.removeLogHandler(listLogHandler);
    }


    @Test
    void test_cancelTask() throws Exception
    {
        runnableExample1.addLogMessage("Runnable1 is running");
        runnableExample2.addLogMessage("Runnable2 is running");
        runnableExample3.addLogMessage("Runnable3 is running");
        singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .taskID("runnable1")
                        .taskToSchedule(runnableExample1)
                        .delay(200)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .taskID("runnable2")
                        .taskToSchedule(runnableExample2)
                        .delay(400)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .taskID("runnable3")
                        .taskToSchedule(runnableExample3)
                        .delay(600)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        singleExecutionScheduler.cancel("runnable3");
        Thread.sleep(750);
        assertEquals(5, listLogHandler.getLogRecords().size());
        assertTrue(listLogHandler.getLogRecords().get(3).getMessage().equals("Runnable1 is running"));
        assertTrue(listLogHandler.getLogRecords().get(4).getMessage().equals("Runnable2 is running"));
    }


    @Test
    void test_cancelTask_disabled() throws Exception
    {
        config.updateProp("orionlibs.task-scheduler.cancellation.enabled", "false");
        runnableExample1.addLogMessage("Runnable1 is running");
        runnableExample2.addLogMessage("Runnable2 is running");
        runnableExample3.addLogMessage("Runnable3 is running");
        singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .taskID("runnable1")
                        .taskToSchedule(runnableExample1)
                        .delay(200)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .taskID("runnable2")
                        .taskToSchedule(runnableExample2)
                        .delay(400)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .taskID("runnable3")
                        .taskToSchedule(runnableExample3)
                        .delay(600)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        Exception exception = assertThrows(FeatureIsDisabledException.class, () -> {
            singleExecutionScheduler.cancel("runnable3");
        });
        config.updateProp("orionlibs.task-scheduler.cancellation.enabled", "true");
    }


    @Test
    void test_cancelTask_nonExistentTask() throws Exception
    {
        runnableExample1.addLogMessage("Runnable1 is running");
        runnableExample2.addLogMessage("Runnable2 is running");
        runnableExample3.addLogMessage("Runnable3 is running");
        singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .taskID("runnable1")
                        .taskToSchedule(runnableExample1)
                        .delay(200)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .taskID("runnable2")
                        .taskToSchedule(runnableExample2)
                        .delay(400)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .taskID("runnable3")
                        .taskToSchedule(runnableExample3)
                        .delay(600)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        singleExecutionScheduler.cancel("runnable3");
        Thread.sleep(500);
        Exception exception = assertThrows(TaskDoesNotExistException.class, () -> {
            singleExecutionScheduler.cancel("runnable3");
        });
    }


    @Test
    void test_cancelTask_withCallbackAfterTaskIsCancelled() throws Exception
    {
        runnableExample1.addLogMessage("Runnable is running");
        singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .taskID("runnable")
                        .taskToSchedule(runnableExample1)
                        .delay(200)
                        .unit(TimeUnit.MILLISECONDS)
                        .callbackAfterTaskIsCancelled(callback)
                        .build());
        singleExecutionScheduler.cancel("runnable");
        assertTrue(listLogHandler.getLogRecords().stream()
                        .anyMatch(record -> record.getMessage().contains("schedule started")));
        Thread.sleep(300);
        assertTrue(listLogHandler.getLogRecords().stream()
                        .anyMatch(record -> record.getMessage().contains("callback has been called")));
    }
}
