package io.github.orionlibs.task_scheduler;

import io.github.orionlibs.task_scheduler.config.ConfigurationService;
import io.github.orionlibs.task_scheduler.config.OrionConfiguration;
import java.io.IOException;
import java.util.Collection;
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
    private Logger log;
    private OrionConfiguration featureConfiguration;
    private ConcurrentMap<String, ScheduledTask> scheduledTasksToRunnablesMapper;
    private ConfigurationService config;


    public SingleExecutionScheduleService() throws IOException
    {
        log = Logger.getLogger(SingleExecutionScheduleService.class.getName());
        this.config = new ConfigurationService();
        setupConfiguration();
        this.scheduledTasksToRunnablesMapper = new ConcurrentHashMap<>();
    }


    private void setupConfiguration() throws IOException
    {
        this.featureConfiguration = OrionConfiguration.loadFeatureConfiguration();
        config.registerConfiguration(featureConfiguration);
    }


    void addLogHandler(Handler handler)
    {
        log.addHandler(handler);
    }


    void removeLogHandler(Handler handler)
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
    public void schedule(ScheduledTask taskToSchedule) throws FeatureIsDisabledException, RejectedExecutionException
    {
        if(config.getBooleanProp("orionlibs.task-scheduler.enabled"))
        {
            Runnable taskWrapper = TaskWrapper.buildTaskWrapper(taskToSchedule, scheduledTasksToRunnablesMapper, this);
            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            try
            {
                ScheduledFuture<?> task = executorService.schedule(taskWrapper, taskToSchedule.getDelay(), taskToSchedule.getUnit());
                taskToSchedule.setTask(task);
                scheduledTasksToRunnablesMapper.put(taskToSchedule.getTaskID(), taskToSchedule);
                log.info("schedule started");
            }
            finally
            {
                executorService.shutdown();
            }
        }
        else
        {
            throw new FeatureIsDisabledException();
        }
    }


    public void schedule(Collection<ScheduledTask> tasksToSchedule) throws FeatureIsDisabledException
    {
        if(config.getBooleanProp("orionlibs.task-scheduler.enabled"))
        {
            if(tasksToSchedule != null)
            {
                for(ScheduledTask task : tasksToSchedule)
                {
                    schedule(task);
                }
            }
        }
        else
        {
            throw new FeatureIsDisabledException();
        }
    }


    public boolean cancel(String taskToCancel) throws FeatureIsDisabledException, TaskDoesNotExistException
    {
        if(config.getBooleanProp("orionlibs.task-scheduler.enabled")
                        && config.getBooleanProp("orionlibs.task-scheduler.cancellation.enabled"))
        {
            ScheduledTask task = getScheduledTaskByID(taskToCancel);
            if(task != null && !task.getTask().isCancelled())
            {
                boolean wasTaskCancelled = task.getTask().cancel(true);
                if(wasTaskCancelled)
                {
                    scheduledTasksToRunnablesMapper.remove(taskToCancel);
                }
                if(task.getCallbackAfterTaskIsCancelled() != null)
                {
                    task.getCallbackAfterTaskIsCancelled().run();
                }
                return wasTaskCancelled;
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


    public Map<String, ScheduledTask> getScheduledTasksToRunnablesMapper()
    {
        return scheduledTasksToRunnablesMapper;
    }


    public ScheduledTask getScheduledTaskByID(String taskID)
    {
        return scheduledTasksToRunnablesMapper.get(taskID);
    }


    public ConfigurationService getConfig()
    {
        return config;
    }
}
