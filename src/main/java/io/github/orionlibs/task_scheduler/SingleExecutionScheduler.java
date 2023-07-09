package io.github.orionlibs.task_scheduler;

import io.github.orionlibs.task_scheduler.config.ConfigurationService;
import io.github.orionlibs.task_scheduler.config.OrionConfiguration;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@NoArgsConstructor
@Component
public class SingleExecutionScheduler
{
    private final static Logger log;
    private Environment springEnv;
    private OrionConfiguration featureConfiguration;
    private ScheduledExecutorService executorService;

    static
    {
        log = Logger.getLogger(SingleExecutionScheduler.class.getName());
    }

    @Autowired
    public SingleExecutionScheduler(final Environment springEnv) throws IOException
    {
        this.springEnv = springEnv;
        this.featureConfiguration = OrionConfiguration.loadFeatureConfiguration(springEnv);
        loadLoggerConfiguration();
        ConfigurationService.registerConfiguration(featureConfiguration);
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }


    private void loadLoggerConfiguration() throws IOException
    {
        LogManager.getLogManager().readConfiguration(OrionConfiguration.loadLoggerConfigurationAndGet(springEnv).getAsInputStream());
    }


    static void addLogHandler(Handler handler)
    {
        log.addHandler(handler);
    }


    static void removeLogHandler(Handler handler)
    {
        log.removeHandler(handler);
    }


    public void schedule(Runnable command, long delay, TimeUnit unit)
    {
        executorService.schedule(command, delay, unit);
        log.info("schedule started");
    }


    public void shutdown()
    {
        executorService.shutdown();
    }
}
