package io.github.orionlibs.task_scheduler;

import io.github.orionlibs.task_scheduler.config.ConfigurationService;
import io.github.orionlibs.task_scheduler.config.OrionConfiguration;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Logger;

public class SingleExecutionScheduleService
{
    private final static Logger log;
    private OrionConfiguration featureConfiguration;
    private ScheduledExecutorService executorService;

    static
    {
        log = Logger.getLogger(SingleExecutionScheduleService.class.getName());
    }

    public SingleExecutionScheduleService() throws IOException
    {
        setupConfiguration();
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }


    private void setupConfiguration() throws IOException
    {
        this.featureConfiguration = OrionConfiguration.loadFeatureConfiguration();
        ConfigurationService.registerConfiguration(featureConfiguration);
    }


    static void addLogHandler(Handler handler)
    {
        log.addHandler(handler);
    }


    static void removeLogHandler(Handler handler)
    {
        log.removeHandler(handler);
    }


    public void schedule(Runnable command, long delay, TimeUnit unit) throws FeatureIsDisabledException
    {
        if(ConfigurationService.getBooleanProp("orionlibs.task-scheduler.enabled"))
        {
            executorService.schedule(command, delay, unit);
            log.info("schedule started");
        }
        else
        {
            throw new FeatureIsDisabledException();
        }
    }


    public void shutdown()
    {
        executorService.shutdown();
    }
}
