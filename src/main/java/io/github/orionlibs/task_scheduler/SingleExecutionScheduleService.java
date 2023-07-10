package io.github.orionlibs.task_scheduler;

import io.github.orionlibs.task_scheduler.config.ConfigurationService;
import io.github.orionlibs.task_scheduler.config.OrionConfiguration;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SingleExecutionScheduleService
{
    private final static Logger log;
    private OrionConfiguration featureConfiguration;
    private ScheduledExecutorService executorService;
    private Map<ScheduledFuture<?>, Runnable> scheduledTasksToRunnablesMapper;

    static
    {
        log = Logger.getLogger(SingleExecutionScheduleService.class.getName());
    }

    public SingleExecutionScheduleService() throws IOException
    {
        setupConfiguration();
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.scheduledTasksToRunnablesMapper = new HashMap<>();
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


    /**
     *
     * @param command
     * @param delay
     * @param unit
     * @return
     * @throws FeatureIsDisabledException
     * @throws RejectedExecutionException
     * @throws NullPointerException
     */
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) throws FeatureIsDisabledException
    {
        if(ConfigurationService.getBooleanProp("orionlibs.task-scheduler.enabled"))
        {
            cleanUpTasks();
            ScheduledFuture<?> task = executorService.schedule(command, delay, unit);
            scheduledTasksToRunnablesMapper.put(task, command);
            log.info("schedule started");
            return task;
        }
        else
        {
            throw new FeatureIsDisabledException();
        }
    }


    public boolean cancelTask(ScheduledFuture<?> taskToCancel) throws FeatureIsDisabledException, TaskDoesNotExistException
    {
        if(ConfigurationService.getBooleanProp("orionlibs.task-scheduler.enabled")
                        && ConfigurationService.getBooleanProp("orionlibs.task-scheduler.cancellation.enabled"))
        {
            cleanUpTasks();
            if(scheduledTasksToRunnablesMapper.get(taskToCancel) != null && !taskToCancel.isCancelled())
            {
                return taskToCancel.cancel(true);
            }
            else
            {
                throw new TaskDoesNotExistException();
            }
        }
        else
        {
            throw new FeatureIsDisabledException();
        }
    }


    private void cleanUpTasks()
    {
        scheduledTasksToRunnablesMapper.entrySet()
                        .stream()
                        .map(task -> task.getKey())
                        .filter(task -> task.isDone())
                        .collect(Collectors.toSet())
                        .forEach(task -> scheduledTasksToRunnablesMapper.remove(task));
    }


    public void shutdown()
    {
        executorService.shutdown();
    }
}
