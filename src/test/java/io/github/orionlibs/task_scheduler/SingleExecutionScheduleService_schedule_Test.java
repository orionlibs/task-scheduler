package io.github.orionlibs.task_scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.orionlibs.task_scheduler.config.ConfigurationService;
import io.github.orionlibs.task_scheduler.log.ListLogHandler;
import io.github.orionlibs.task_scheduler.utils.Callback;
import io.github.orionlibs.task_scheduler.utils.RunnableExample;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_METHOD)
//@Execution(ExecutionMode.CONCURRENT)
public class SingleExecutionScheduleService_schedule_Test
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
    void test_schedule() throws Exception
    {
        runnableExample1.addLogMessage("Runnable is running");
        singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .taskID("runnable")
                        .taskToSchedule(runnableExample1)
                        .delay(50)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        Thread.sleep(150);
        assertTrue(listLogHandler.getLogRecords().stream()
                        .anyMatch(record -> record.getMessage().contains("schedule started")));
        assertTrue(listLogHandler.getLogRecords().stream()
                        .anyMatch(record -> record.getMessage().contains("Runnable is running")));
    }


    @Test
    void test_schedule_now() throws Exception
    {
        runnableExample1.addLogMessage("Runnable is running");
        singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .taskID("runnable")
                        .taskToSchedule(runnableExample1)
                        .delay(0)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        Thread.sleep(150);
        assertTrue(listLogHandler.getLogRecords().stream()
                        .anyMatch(record -> record.getMessage().contains("schedule started")));
        assertTrue(listLogHandler.getLogRecords().stream()
                        .anyMatch(record -> record.getMessage().contains("Runnable is running")));
    }


    @Test
    void test_schedule_past() throws Exception
    {
        runnableExample1.addLogMessage("Runnable is running");
        singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .taskID("runnable")
                        .taskToSchedule(runnableExample1)
                        .delay(-1000)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        Thread.sleep(150);
        assertTrue(listLogHandler.getLogRecords().stream()
                        .anyMatch(record -> record.getMessage().contains("schedule started")));
        assertTrue(listLogHandler.getLogRecords().stream()
                        .anyMatch(record -> record.getMessage().contains("Runnable is running")));
    }


    @Test
    void test_schedule_missingTaskID() throws Exception
    {
        runnableExample1.addLogMessage("Runnable is running");
        Exception exception = assertThrows(InvalidArgumentException.class, () -> {
            singleExecutionScheduler.schedule(ScheduledTask.builder()
                            .taskID(null)
                            .taskToSchedule(runnableExample1)
                            .delay(50)
                            .unit(TimeUnit.MILLISECONDS)
                            .build());
        });
    }


    @Test
    void test_schedule_missingTaskToSchedule() throws Exception
    {
        Exception exception = assertThrows(InvalidArgumentException.class, () -> {
            singleExecutionScheduler.schedule(ScheduledTask.builder()
                            .taskID("task1")
                            .taskToSchedule(null)
                            .delay(50)
                            .unit(TimeUnit.MILLISECONDS)
                            .build());
        });
    }


    @Test
    void test_schedule_missingUnit() throws Exception
    {
        runnableExample1.addLogMessage("Runnable is running");
        Exception exception = assertThrows(InvalidArgumentException.class, () -> {
            singleExecutionScheduler.schedule(ScheduledTask.builder()
                            .taskID("runnable")
                            .taskToSchedule(runnableExample1)
                            .delay(-1000)
                            .unit(null)
                            .build());
        });
    }


    @Test
    void test_schedule_disabled()
    {
        config.updateProp("orionlibs.task-scheduler.enabled", "false");
        runnableExample1.addLogMessage("Runnable is running");
        Exception exception = assertThrows(FeatureIsDisabledException.class, () -> {
            singleExecutionScheduler.schedule(ScheduledTask.builder()
                            .taskID("runnable")
                            .taskToSchedule(runnableExample1)
                            .delay(50)
                            .unit(TimeUnit.MILLISECONDS)
                            .build());
        });
        config.updateProp("orionlibs.task-scheduler.enabled", "true");
    }


    @Test
    void test_schedule_sequentialTasks() throws Exception
    {
        runnableExample1.addLogMessage("Runnable1 is running");
        runnableExample2.addLogMessage("Runnable2 is running");
        runnableExample3.addLogMessage("Runnable3 is running");
        singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .taskID("runnable1")
                        .taskToSchedule(runnableExample1)
                        .delay(600)
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
                        .delay(200)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        Thread.sleep(700);
        assertTrue(listLogHandler.getLogRecords().stream()
                        .anyMatch(record -> record.getMessage().contains("schedule started")));
        assertTrue(listLogHandler.getLogRecords().get(3).getMessage().equals("Runnable3 is running"));
        assertTrue(listLogHandler.getLogRecords().get(4).getMessage().equals("Runnable2 is running"));
        assertTrue(listLogHandler.getLogRecords().get(5).getMessage().equals("Runnable1 is running"));
    }


    @Test
    void test_getScheduledTasksToRunnablesMapper() throws Exception
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
        Map<String, ScheduledTask> scheduledTasks = singleExecutionScheduler.getScheduledTasksToRunnablesMapper();
        assertEquals(runnableExample1, scheduledTasks.get("runnable1").getTaskToSchedule());
        assertEquals(runnableExample2, scheduledTasks.get("runnable2").getTaskToSchedule());
        assertEquals(runnableExample3, scheduledTasks.get("runnable3").getTaskToSchedule());
    }


    @Test
    void test_schedule_listOfTasks() throws Exception
    {
        runnableExample1.addLogMessage("Runnable1 is running");
        runnableExample2.addLogMessage("Runnable2 is running");
        runnableExample3.addLogMessage("Runnable3 is running");
        List<ScheduledTask> tasksToSchedule = new ArrayList<>();
        tasksToSchedule.add(ScheduledTask.builder()
                        .taskID("runnable1")
                        .taskToSchedule(runnableExample1)
                        .delay(600)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        tasksToSchedule.add(ScheduledTask.builder()
                        .taskID("runnable2")
                        .taskToSchedule(runnableExample2)
                        .delay(400)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        tasksToSchedule.add(ScheduledTask.builder()
                        .taskID("runnable3")
                        .taskToSchedule(runnableExample3)
                        .delay(200)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        singleExecutionScheduler.schedule(tasksToSchedule);
        Thread.sleep(800);
        assertTrue(listLogHandler.getLogRecords().stream()
                        .anyMatch(record -> record.getMessage().contains("schedule started")));
        assertTrue(listLogHandler.getLogRecords().stream()
                        .anyMatch(record -> record.getMessage().contains("Runnable1 is running")));
        assertTrue(listLogHandler.getLogRecords().stream()
                        .anyMatch(record -> record.getMessage().contains("Runnable2 is running")));
        assertTrue(listLogHandler.getLogRecords().stream()
                        .anyMatch(record -> record.getMessage().contains("Runnable3 is running")));
        /*assertTrue(listLogHandler.getLogRecords().get(3).getMessage().equals("Runnable3 is running"));
        assertTrue(listLogHandler.getLogRecords().get(4).getMessage().equals("Runnable2 is running"));
        assertTrue(listLogHandler.getLogRecords().get(5).getMessage().equals("Runnable1 is running"));*/
    }


    @Test
    void test_schedule_withCallbackAfterTaskCompletes() throws Exception
    {
        runnableExample1.addLogMessage("Runnable is running");
        singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .taskID("runnable")
                        .taskToSchedule(runnableExample1)
                        .delay(50)
                        .unit(TimeUnit.MILLISECONDS)
                        .callbackAfterTaskCompletes(new Callback())
                        .build());
        Thread.sleep(150);
        assertTrue(listLogHandler.getLogRecords().stream()
                        .anyMatch(record -> record.getMessage().contains("schedule started")));
        assertTrue(listLogHandler.getLogRecords().stream()
                        .anyMatch(record -> record.getMessage().contains("Runnable is running")));
        assertTrue(listLogHandler.getLogRecords().stream()
                        .anyMatch(record -> record.getMessage().contains("callback has been called")));
    }


    @Test
    void test_schedule_withNegativeRetry() throws Exception
    {
        runnableExample1.addLogMessageAndDelay("Runnable is running", 100L);
        singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .taskID("runnable")
                        .taskToSchedule(runnableExample1)
                        .delay(500)
                        .unit(TimeUnit.MILLISECONDS)
                        .numberOfRetriesOnError(-5)
                        .build());
        Thread.sleep(1000);
        assertEquals(2, listLogHandler.getLogRecords().size());
        assertEquals("schedule started", listLogHandler.getLogRecords().get(0).getMessage());
        assertEquals("Runnable is running", listLogHandler.getLogRecords().get(1).getMessage());
    }


    @Test
    void test_schedule_with1Retry() throws Exception
    {
        runnableExample1.addLogMessageAndDelay("Runnable is running", 100L);
        singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .taskID("runnable")
                        .taskToSchedule(runnableExample1)
                        .delay(500)
                        .unit(TimeUnit.MILLISECONDS)
                        .numberOfRetriesOnError(1)
                        .build());
        Thread.sleep(2000);
        assertEquals(4, listLogHandler.getLogRecords().size());
        assertEquals("schedule started", listLogHandler.getLogRecords().get(0).getMessage());
        assertEquals("Runnable is running", listLogHandler.getLogRecords().get(1).getMessage());
        assertEquals("schedule started", listLogHandler.getLogRecords().get(2).getMessage());
        assertEquals("Runnable is running", listLogHandler.getLogRecords().get(3).getMessage());
    }


    @Test
    void test_schedule_with3Retries() throws Exception
    {
        runnableExample1.addLogMessageAndDelay("Runnable is running", 100L);
        singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .taskID("runnable")
                        .taskToSchedule(runnableExample1)
                        .delay(1000)
                        .unit(TimeUnit.MILLISECONDS)
                        .numberOfRetriesOnError(3)
                        .build());
        Thread.sleep(6000);
        assertEquals(8, listLogHandler.getLogRecords().size());
        assertEquals("schedule started", listLogHandler.getLogRecords().get(0).getMessage());
        assertEquals("Runnable is running", listLogHandler.getLogRecords().get(1).getMessage());
        assertEquals("schedule started", listLogHandler.getLogRecords().get(2).getMessage());
        assertEquals("Runnable is running", listLogHandler.getLogRecords().get(3).getMessage());
        assertEquals("schedule started", listLogHandler.getLogRecords().get(4).getMessage());
        assertEquals("Runnable is running", listLogHandler.getLogRecords().get(5).getMessage());
        assertEquals("schedule started", listLogHandler.getLogRecords().get(6).getMessage());
        assertEquals("Runnable is running", listLogHandler.getLogRecords().get(7).getMessage());
    }
}
