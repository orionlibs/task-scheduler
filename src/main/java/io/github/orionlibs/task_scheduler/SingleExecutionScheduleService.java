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

/**
 * Service that schedules tasks to be executed in the future only once.
 */
public class SingleExecutionScheduleService
{
    private static final String TASK_SCHEDULER_ENABLED = "orionlibs.task-scheduler.enabled";
    private static final String SCHEDULER_CANCELLATION_ENABLED = "orionlibs.task-scheduler.cancellation.enabled";
    private Logger log;
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
        config.registerConfiguration(OrionConfiguration.loadFeatureConfiguration());
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
     * Schedules a task to execute in the future only once.
     * The given ScheduledTask object will have a value for the task field which
     * will be the actual ScheduledFuture that executes.
     * @param taskToSchedule
     * @throws FeatureIsDisabledException if the scheduler is disabled.
     * @throws RejectedExecutionException if the scheduler rejects the task.
     * @throws NullPointerException
     * @throws InvalidArgumentException if the taskToSchedule argument has invalid values.
     */
    public void schedule(ScheduledTask taskToSchedule) throws FeatureIsDisabledException, RejectedExecutionException, InvalidArgumentException
    {
        if(config.getBooleanProp(TASK_SCHEDULER_ENABLED))
        {
            taskToSchedule.validate();
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


    /**
     * Schedules multiple tasks to execute in the future only once.
     * @param tasksToSchedule
     * @throws FeatureIsDisabledException if the scheduler is disabled.
     * @throws RejectedExecutionException if the scheduler rejects the task.
     * @throws NullPointerException
     * @throws InvalidArgumentException if the taskToSchedule argument has invalid values.
     */
    public void schedule(Collection<ScheduledTask> tasksToSchedule) throws FeatureIsDisabledException, RejectedExecutionException, InvalidArgumentException
    {
        if(config.getBooleanProp(TASK_SCHEDULER_ENABLED))
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


    /**
     * It cancels the given taskToCancel before it executes.
     * @param taskToCancel
     * @return
     * @throws FeatureIsDisabledException if the scheduler or the cancellation feature is disabled.
     * @throws TaskDoesNotExistException if the taskToCancel doe snot exist in the scheduler.
     */
    public boolean cancel(String taskToCancel) throws FeatureIsDisabledException, TaskDoesNotExistException
    {
        if(config.getBooleanProp(TASK_SCHEDULER_ENABLED)
                        && config.getBooleanProp(SCHEDULER_CANCELLATION_ENABLED))
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


    /**
     * It returns a mapping of taskIDs to ScheduledTask objects
     * @return
     */
    public Map<String, ScheduledTask> getScheduledTasksToRunnablesMapper()
    {
        return scheduledTasksToRunnablesMapper;
    }


    /**
     * It returns the ScheduledTask that corresponds to the provided taskID.
     * @param taskID
     * @return
     */
    public ScheduledTask getScheduledTaskByID(String taskID)
    {
        return scheduledTasksToRunnablesMapper.get(taskID);
    }


    /**
     * It returns the config of this instance of the service.
     * @return
     */
    public ConfigurationService getConfig()
    {
        return config;
    }
}
