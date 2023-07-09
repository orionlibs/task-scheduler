package io.github.orionlibs.task_scheduler;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.orionlibs.task_scheduler.config.ConfigurationService;
import io.github.orionlibs.task_scheduler.log.ListLogHandler;
import io.github.orionlibs.task_scheduler.utils.RunnableExample;
import java.io.IOException;
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
        singleExecutionScheduler.schedule(new RunnableExample("Runnable is running"), 100, TimeUnit.MILLISECONDS);
        singleExecutionScheduler.shutdown();
        assertTrue(listLogHandler.getLogRecords().stream()
                        .anyMatch(record -> record.getMessage().contains("schedule started")));
        Thread.sleep(150);
        assertTrue(listLogHandler.getLogRecords().stream()
                        .anyMatch(record -> record.getMessage().contains("Runnable is running")));
    }


    @Test
    void test_schedule_disabled()
    {
        ConfigurationService.updateProp("orionlibs.task-scheduler.enabled", "false");
        Exception exception = assertThrows(FeatureIsDisabledException.class, () -> {
            singleExecutionScheduler.schedule(new RunnableExample("Runnable is running"), 100, TimeUnit.MILLISECONDS);
        });
        ConfigurationService.updateProp("orionlibs.task-scheduler.enabled", "true");
    }


    @Test
    void test_schedule_sequentialTasks() throws Exception
    {
        singleExecutionScheduler.schedule(new RunnableExample("Runnable1 is running"), 300, TimeUnit.MILLISECONDS);
        singleExecutionScheduler.schedule(new RunnableExample("Runnable2 is running"), 200, TimeUnit.MILLISECONDS);
        singleExecutionScheduler.schedule(new RunnableExample("Runnable3 is running"), 100, TimeUnit.MILLISECONDS);
        singleExecutionScheduler.shutdown();
        assertTrue(listLogHandler.getLogRecords().stream()
                        .anyMatch(record -> record.getMessage().contains("schedule started")));
        Thread.sleep(400);
        assertTrue(listLogHandler.getLogRecords().get(3).getMessage().equals("Runnable3 is running"));
        assertTrue(listLogHandler.getLogRecords().get(4).getMessage().equals("Runnable2 is running"));
        assertTrue(listLogHandler.getLogRecords().get(5).getMessage().equals("Runnable1 is running"));
    }
}
