package io.github.orionlibs.task_scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.orionlibs.task_scheduler.config.ConfigurationService;
import io.github.orionlibs.task_scheduler.log.ListLogHandler;
import io.github.orionlibs.task_scheduler.utils.RunnableExample;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class SingleExecutionSchedulerTest
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
                        .command(new RunnableExample("Runnable is running"))
                        .delay(100)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        singleExecutionScheduler.shutdown();
        assertTrue(listLogHandler.getLogRecords().stream()
                        .anyMatch(record -> record.getMessage().contains("schedule started")));
        Thread.sleep(250);
        assertTrue(listLogHandler.getLogRecords().stream()
                        .anyMatch(record -> record.getMessage().contains("Runnable is running")));
    }


    @Test
    void test_schedule_disabled()
    {
        ConfigurationService.updateProp("orionlibs.task-scheduler.enabled", "false");
        Exception exception = assertThrows(FeatureIsDisabledException.class, () -> {
            singleExecutionScheduler.schedule(ScheduledTask.builder()
                            .command(new RunnableExample("Runnable is running"))
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
                        .command(new RunnableExample("Runnable1 is running"))
                        .delay(600)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .command(new RunnableExample("Runnable2 is running"))
                        .delay(400)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .command(new RunnableExample("Runnable3 is running"))
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
        ScheduledFuture<?> task1 = singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .command(new RunnableExample("Runnable1 is running"))
                        .delay(200)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        ScheduledFuture<?> task2 = singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .command(new RunnableExample("Runnable2 is running"))
                        .delay(400)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        ScheduledFuture<?> task3 = singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .command(new RunnableExample("Runnable3 is running"))
                        .delay(600)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        singleExecutionScheduler.shutdown();
        singleExecutionScheduler.cancelTask(task3);
        Thread.sleep(700);
        assertTrue(listLogHandler.getLogRecords().get(3).getMessage().equals("Runnable1 is running"));
        assertTrue(listLogHandler.getLogRecords().get(4).getMessage().equals("Runnable2 is running"));
        assertEquals(5, listLogHandler.getLogRecords().size());
    }


    @Test
    void test_cancelTask_disabled() throws Exception
    {
        ConfigurationService.updateProp("orionlibs.task-scheduler.cancellation.enabled", "false");
        ScheduledFuture<?> task1 = singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .command(new RunnableExample("Runnable1 is running"))
                        .delay(200)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        ScheduledFuture<?> task2 = singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .command(new RunnableExample("Runnable2 is running"))
                        .delay(400)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        ScheduledFuture<?> task3 = singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .command(new RunnableExample("Runnable3 is running"))
                        .delay(600)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        singleExecutionScheduler.shutdown();
        Exception exception = assertThrows(FeatureIsDisabledException.class, () -> {
            singleExecutionScheduler.cancelTask(task3);
        });
        ConfigurationService.updateProp("orionlibs.task-scheduler.cancellation.enabled", "true");
    }


    @Test
    void test_cancelTask_nonExistentTask() throws Exception
    {
        ScheduledFuture<?> task1 = singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .command(new RunnableExample("Runnable1 is running"))
                        .delay(200)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        ScheduledFuture<?> task2 = singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .command(new RunnableExample("Runnable2 is running"))
                        .delay(400)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        ScheduledFuture<?> task3 = singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .command(new RunnableExample("Runnable3 is running"))
                        .delay(600)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        singleExecutionScheduler.shutdown();
        singleExecutionScheduler.cancelTask(task3);
        Thread.sleep(500);
        Exception exception = assertThrows(TaskDoesNotExistException.class, () -> {
            singleExecutionScheduler.cancelTask(task3);
        });
    }


    @Test
    void test_getScheduledTasksToRunnablesMapper() throws Exception
    {
        RunnableExample command1 = new RunnableExample("Runnable1 is running");
        RunnableExample command2 = new RunnableExample("Runnable2 is running");
        RunnableExample command3 = new RunnableExample("Runnable3 is running");
        ScheduledFuture<?> task1 = singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .command(command1)
                        .delay(200)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        ScheduledFuture<?> task2 = singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .command(command2)
                        .delay(400)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        ScheduledFuture<?> task3 = singleExecutionScheduler.schedule(ScheduledTask.builder()
                        .command(command3)
                        .delay(600)
                        .unit(TimeUnit.MILLISECONDS)
                        .build());
        Map<ScheduledFuture<?>, ScheduledTask> scheduledTasks = singleExecutionScheduler.getScheduledTasksToRunnablesMapper();
        singleExecutionScheduler.shutdown();
        assertEquals(command1, scheduledTasks.get(task1).getCommand());
        assertEquals(command2, scheduledTasks.get(task2).getCommand());
        assertEquals(command3, scheduledTasks.get(task3).getCommand());
    }
}
