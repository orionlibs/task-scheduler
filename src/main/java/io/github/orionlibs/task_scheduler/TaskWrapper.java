package io.github.orionlibs.task_scheduler;

import java.util.concurrent.ConcurrentMap;

/**
 * A class that wraps a Runnable in a way that can handle task retries.
 */
class TaskWrapper
{
    private TaskWrapper()
    {
    }


    static Runnable buildTaskWrapper(ScheduledTask taskToSchedule, ConcurrentMap<String, ScheduledTask> scheduledTasksToRunnablesMapper, SingleExecutionScheduleService singleExecutionScheduleService)
    {
        return new ScheduledRunnable(taskToSchedule, scheduledTasksToRunnablesMapper, singleExecutionScheduleService);
    }


    static class ScheduledRunnable implements Runnable
    {
        private final ScheduledTask taskToSchedule;
        private final ConcurrentMap<String, ScheduledTask> scheduledTasksToRunnablesMapper;
        private final SingleExecutionScheduleService singleExecutionScheduleService;
        private int remainingRetries;


        public ScheduledRunnable(ScheduledTask taskToSchedule, ConcurrentMap<String, ScheduledTask> scheduledTasksToRunnablesMapper, SingleExecutionScheduleService singleExecutionScheduleService)
        {
            this.taskToSchedule = taskToSchedule;
            this.scheduledTasksToRunnablesMapper = scheduledTasksToRunnablesMapper;
            this.singleExecutionScheduleService = singleExecutionScheduleService;
            this.remainingRetries = taskToSchedule.getNumberOfRetriesOnError() >= 0 ? taskToSchedule.getNumberOfRetriesOnError() : 0;
        }


        @Override
        public void run()
        {
            try
            {
                taskToSchedule.getTaskToSchedule().run();
            }
            catch(Exception e)
            {
                if(remainingRetries > 0)
                {
                    remainingRetries--;
                    rescheduleTask();
                }
                else
                {
                    handleTaskCompletion();
                }
            }
            finally
            {
                if(remainingRetries <= 0)
                {
                    handleTaskCompletion();
                }
            }
        }


        private void rescheduleTask()
        {
            try
            {
                taskToSchedule.setNumberOfRetriesOnError(remainingRetries);
                singleExecutionScheduleService.cancel(taskToSchedule.getTaskID());
                singleExecutionScheduleService.schedule(taskToSchedule);
            }
            catch(FeatureIsDisabledException | TaskDoesNotExistException | InvalidArgumentException ex)
            {
                throw new RuntimeException(ex);
            }
        }


        private void handleTaskCompletion()
        {
            scheduledTasksToRunnablesMapper.remove(taskToSchedule.getTaskID());
            if(taskToSchedule.getCallbackAfterTaskCompletes() != null)
            {
                taskToSchedule.getCallbackAfterTaskCompletes().run();
            }
        }
    }
}
