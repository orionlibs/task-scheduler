package io.github.orionlibs.task_scheduler;

import io.github.orionlibs.task_scheduler.config.ConfigurationService;
import io.github.orionlibs.task_scheduler.config.OrionConfiguration;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Handler;
import java.util.logging.Logger;

public class SingleExecutionScheduleService
{
    private final static Logger log;
    private OrionConfiguration featureConfiguration;
    private ScheduledExecutorService executorService;
    private ConcurrentMap<String, ScheduledTask> scheduledTasksToRunnablesMapper;

    static
    {
        log = Logger.getLogger(SingleExecutionScheduleService.class.getName());
    }

    public SingleExecutionScheduleService() throws IOException
    {
        setupConfiguration();
        this.executorService = Executors.newSingleThreadScheduledExecutor();
        this.scheduledTasksToRunnablesMapper = new ConcurrentHashMap<>();
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
     * @param taskToSchedule
     * @return
     * @throws FeatureIsDisabledException
     * @throws RejectedExecutionException
     * @throws NullPointerException
     */
    public ScheduledFuture<?> schedule(ScheduledTask taskToSchedule) throws FeatureIsDisabledException
    {
        if(ConfigurationService.getBooleanProp("orionlibs.task-scheduler.enabled"))
        {
            Runnable taskWrapper = () -> {
                try
                {
                    taskToSchedule.getCommand().run();
                }
                finally
                {
                    scheduledTasksToRunnablesMapper.remove(taskToSchedule.getTaskID());
                }
            };
            //cleanUpTasks();
            ScheduledFuture<?> task = executorService.schedule(taskWrapper, taskToSchedule.getDelay(), taskToSchedule.getUnit());
            taskToSchedule.setTask(task);
            scheduledTasksToRunnablesMapper.put(taskToSchedule.getTaskID(), taskToSchedule);
            log.info("schedule started");
            return task;
        }
        else
        {
            throw new FeatureIsDisabledException();
        }
    }


    public boolean cancelTask(String taskToCancel) throws FeatureIsDisabledException, TaskDoesNotExistException
    {
        if(ConfigurationService.getBooleanProp("orionlibs.task-scheduler.enabled")
                        && ConfigurationService.getBooleanProp("orionlibs.task-scheduler.cancellation.enabled"))
        {
            //cleanUpTasks();
            if(scheduledTasksToRunnablesMapper.get(taskToCancel) != null && !scheduledTasksToRunnablesMapper.get(taskToCancel).getTask().isCancelled())
            {
                return scheduledTasksToRunnablesMapper.get(taskToCancel).getTask().cancel(true);
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


    public void shutdown()
    {
        executorService.shutdown();
    }


    /*private void cleanUpTasks()
    {
        scheduledTasksToRunnablesMapper.entrySet()
                        .stream()
                        .map(task -> task.getKey())
                        .filter(task -> task.isDone())
                        .collect(Collectors.toSet())
                        .forEach(task -> scheduledTasksToRunnablesMapper.remove(task));
    }*/


    public Map<String, ScheduledTask> getScheduledTasksToRunnablesMapper()
    {
        return scheduledTasksToRunnablesMapper;
    }
}
