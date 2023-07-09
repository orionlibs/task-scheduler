package io.github.orionlibs.task_scheduler;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.orionlibs.task_scheduler.config.FakeTestingSpringConfiguration;
import io.github.orionlibs.task_scheduler.log.ListLogHandler;
import io.github.orionlibs.task_scheduler.utils.RunnableExample;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("testing")
@ContextConfiguration(classes = FakeTestingSpringConfiguration.FakeConfiguration.class)
@WebAppConfiguration
@TestInstance(Lifecycle.PER_CLASS)
public class SingleExecutionSchedulerTest
{
    private ListLogHandler listLogHandler;
    private MockMvc mockMvc;
    @Autowired
    private SingleExecutionScheduler singleExecutionScheduler;


    @BeforeEach
    void setUp()
    {
        listLogHandler = new ListLogHandler();
        SingleExecutionScheduler.addLogHandler(listLogHandler);
        RunnableExample.addLogHandler(listLogHandler);
    }


    @AfterEach
    public void teardown()
    {
        SingleExecutionScheduler.removeLogHandler(listLogHandler);
        RunnableExample.removeLogHandler(listLogHandler);
    }


    @Test
    void test_schedule() throws Exception
    {
        singleExecutionScheduler.schedule(new RunnableExample(), 100, TimeUnit.MILLISECONDS);
        singleExecutionScheduler.shutdown();
        assertTrue(listLogHandler.getLogRecords().stream()
                        .anyMatch(record -> record.getMessage().contains("schedule started")));
        Thread.sleep(150);
        assertTrue(listLogHandler.getLogRecords().stream()
                        .anyMatch(record -> record.getMessage().contains("Runnable is running")));
    }
}
