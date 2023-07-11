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

@TestInstance(Lifecycle.PER_CLASS)
public class SingleExecutionScheduleServiceTest
{
    private ListLogHandler listLogHandler;
    private SingleExecutionScheduleService singleExecutionScheduler;


    @BeforeEach
    void setUp() throws IOException
    {
        singleExecutionScheduler = new SingleExecutionScheduleService();
        listLogHandler = new ListLogHandler();
        SingleExecutionScheduleService.addLogHandler(listLogHandler);
        RunnableExample.addLogHandler(listLogHandler);
    }


    @AfterEach
    public void teardown()
    {
        SingleExecutionScheduleService.removeLogHandler(listLogHandler);
        RunnableExample.removeLogHandler(listLogHandler);
    }


    @Test
    void test_schedule() throws Exception
    {
        singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .taskID("runnable")
                        .taskToSchedule(new RunnableExample("Runnable is running"))
                        .delay(100)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        singleExecutionScheduler.shutdown();
        assertTrue(listLogHandler.getLogRecords().stream()
                        .anyMatch(record -> record.getMessage().contains("schedule started")));
        Thread.sleep(200);
        assertTrue(listLogHandler.getLogRecords().stream()
                        .anyMatch(record -> record.getMessage().contains("Runnable is running")));
    }


    @Test
    void test_schedule_disabled()
    {
        ConfigurationService.updateProp("orionlibs.task-scheduler.enabled", "false");
        Exception exception = assertThrows(FeatureIsDisabledException.class, () -> {
            singleExecutionScheduler.schedule(ScheduledTask.builder()
                            .taskID("runnable")
                            .taskToSchedule(new RunnableExample("Runnable is running"))
                            .delay(100)
                            .unit(TimeUnit.MILLISECONDS)
                            .build());
        });
        ConfigurationService.updateProp("orionlibs.task-scheduler.enabled", "true");
    }


    @Test
    void test_schedule_sequentialTasks() throws Exception
    {
        singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .taskID("runnable1")
                        .taskToSchedule(new RunnableExample("Runnable1 is running"))
                        .delay(600)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .taskID("runnable2")
                        .taskToSchedule(new RunnableExample("Runnable2 is running"))
                        .delay(400)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .taskID("runnable3")
                        .taskToSchedule(new RunnableExample("Runnable3 is running"))
                        .delay(200)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        singleExecutionScheduler.shutdown();
        assertTrue(listLogHandler.getLogRecords().stream()
                        .anyMatch(record -> record.getMessage().contains("schedule started")));
        Thread.sleep(700);
        assertTrue(listLogHandler.getLogRecords().get(3).getMessage().equals("Runnable3 is running"));
        assertTrue(listLogHandler.getLogRecords().get(4).getMessage().equals("Runnable2 is running"));
        assertTrue(listLogHandler.getLogRecords().get(5).getMessage().equals("Runnable1 is running"));
    }


    @Test
    void test_cancelTask() throws Exception
    {
        singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .taskID("runnable1")
                        .taskToSchedule(new RunnableExample("Runnable1 is running"))
                        .delay(200)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .taskID("runnable2")
                        .taskToSchedule(new RunnableExample("Runnable2 is running"))
                        .delay(400)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .taskID("runnable3")
                        .taskToSchedule(new RunnableExample("Runnable3 is running"))
                        .delay(600)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        singleExecutionScheduler.shutdown();
        singleExecutionScheduler.cancel("runnable3");
        Thread.sleep(700);
        assertTrue(listLogHandler.getLogRecords().get(3).getMessage().equals("Runnable1 is running"));
        assertTrue(listLogHandler.getLogRecords().get(4).getMessage().equals("Runnable2 is running"));
        assertEquals(5, listLogHandler.getLogRecords().size());
    }


    @Test
    void test_cancelTask_disabled() throws Exception
    {
        ConfigurationService.updateProp("orionlibs.task-scheduler.cancellation.enabled", "false");
        singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .taskID("runnable1")
                        .taskToSchedule(new RunnableExample("Runnable1 is running"))
                        .delay(200)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .taskID("runnable2")
                        .taskToSchedule(new RunnableExample("Runnable2 is running"))
                        .delay(400)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .taskID("runnable3")
                        .taskToSchedule(new RunnableExample("Runnable3 is running"))
                        .delay(600)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        singleExecutionScheduler.shutdown();
        Exception exception = assertThrows(FeatureIsDisabledException.class, () -> {
            singleExecutionScheduler.cancel("runnable3");
        });
        ConfigurationService.updateProp("orionlibs.task-scheduler.cancellation.enabled", "true");
    }


    @Test
    void test_cancelTask_nonExistentTask() throws Exception
    {
        singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .taskID("runnable1")
                        .taskToSchedule(new RunnableExample("Runnable1 is running"))
                        .delay(200)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .taskID("runnable2")
                        .taskToSchedule(new RunnableExample("Runnable2 is running"))
                        .delay(400)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .taskID("runnable3")
                        .taskToSchedule(new RunnableExample("Runnable3 is running"))
                        .delay(600)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        singleExecutionScheduler.shutdown();
        singleExecutionScheduler.cancel("runnable3");
        Thread.sleep(500);
        Exception exception = assertThrows(TaskDoesNotExistException.class, () -> {
            singleExecutionScheduler.cancel("runnable3");
        });
    }


    @Test
    void test_getScheduledTasksToRunnablesMapper() throws Exception
    {
        RunnableExample command1 = new RunnableExample("Runnable1 is running");
        RunnableExample command2 = new RunnableExample("Runnable2 is running");
        RunnableExample command3 = new RunnableExample("Runnable3 is running");
        singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .taskID("runnable1")
                        .taskToSchedule(command1)
                        .delay(200)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .taskID("runnable2")
                        .taskToSchedule(command2)
                        .delay(400)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .taskID("runnable3")
                        .taskToSchedule(command3)
                        .delay(600)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        Map<String, ScheduledTask> scheduledTasks = singleExecutionScheduler.getScheduledTasksToRunnablesMapper();
        singleExecutionScheduler.shutdown();
        assertEquals(command1, scheduledTasks.get("runnable1").getTaskToSchedule());
        assertEquals(command2, scheduledTasks.get("runnable2").getTaskToSchedule());
        assertEquals(command3, scheduledTasks.get("runnable3").getTaskToSchedule());
    }


    @Test
    void test_schedule_listOfTasks() throws Exception
    {
        List<ScheduledTask> tasksToSchedule = new ArrayList<>();
        tasksToSchedule.add(ScheduledTask.builder()
                        .taskID("runnable1")
                        .taskToSchedule(new RunnableExample("Runnable1 is running"))
                        .delay(600)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        tasksToSchedule.add(ScheduledTask.builder()
                        .taskID("runnable2")
                        .taskToSchedule(new RunnableExample("Runnable2 is running"))
                        .delay(400)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        tasksToSchedule.add(ScheduledTask.builder()
                        .taskID("runnable3")
                        .taskToSchedule(new RunnableExample("Runnable3 is running"))
                        .delay(200)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        singleExecutionScheduler.schedule(tasksToSchedule);
        singleExecutionScheduler.shutdown();
        assertTrue(listLogHandler.getLogRecords().stream()
                        .anyMatch(record -> record.getMessage().contains("schedule started")));
        Thread.sleep(700);
        assertTrue(listLogHandler.getLogRecords().get(3).getMessage().equals("Runnable3 is running"));
        assertTrue(listLogHandler.getLogRecords().get(4).getMessage().equals("Runnable2 is running"));
        assertTrue(listLogHandler.getLogRecords().get(5).getMessage().equals("Runnable1 is running"));
    }


    @Test
    void test_schedule_withCallback() throws Exception
    {
        Callback.log.addHandler(listLogHandler);
        singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .taskID("runnable")
                        .taskToSchedule(new RunnableExample("Runnable is running"))
                        .delay(100)
                        .unit(TimeUnit.MILLISECONDS)
                        .callback(new Callback())
                        .build());
        singleExecutionScheduler.shutdown();
        assertTrue(listLogHandler.getLogRecords().stream()
                        .anyMatch(record -> record.getMessage().contains("schedule started")));
        Thread.sleep(200);
        assertTrue(listLogHandler.getLogRecords().stream()
                        .anyMatch(record -> record.getMessage().contains("Runnable is running")));
        assertTrue(listLogHandler.getLogRecords().stream()
                        .anyMatch(record -> record.getMessage().contains("callback has been called")));
    }
}
