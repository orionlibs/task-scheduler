package io.github.orionlibs.task_scheduler;

import java.util.concurrent.ConcurrentMap;

class TaskWrapper
{
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
            catch(Throwable e)
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
            catch(FeatureIsDisabledException | TaskDoesNotExistException ex)
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
    /*static Runnable buildTaskWrapper(ScheduledTask taskToSchedule, ConcurrentMap<String, ScheduledTask> scheduledTasksToRunnablesMapper, SingleExecutionScheduleService singleExecutionScheduleService)
    {
        return new Runnable()
        {
            private boolean removeTaskFromMapper = false;
            private int remainingRetries = taskToSchedule.getNumberOfRetriesOnError() >= 0 ? taskToSchedule.getNumberOfRetriesOnError() : 0;


            @Override
            public void run()
            {
                try
                {
                    taskToSchedule.getTaskToSchedule().run();
                }
                catch(Throwable e)
                {
                    if(remainingRetries > 0)
                    {
                        remainingRetries--;
                        try
                        {
                            taskToSchedule.setNumberOfRetriesOnError(remainingRetries);
                            singleExecutionScheduleService.cancel(taskToSchedule.getTaskID());
                            singleExecutionScheduleService.schedule(taskToSchedule);
                            removeTaskFromMapper = remainingRetries <= 0;
                        }
                        catch(FeatureIsDisabledException ex)
                        {
                            throw new RuntimeException(ex);
                        }
                        catch(TaskDoesNotExistException ex)
                        {
                            throw new RuntimeException(ex);
                        }
                        finally
                        {
                            removeTaskFromMapper = remainingRetries <= 0;
                        }
                    }
                    else
                    {
                        scheduledTasksToRunnablesMapper.remove(taskToSchedule.getTaskID());
                        if(taskToSchedule.getCallbackAfterTaskCompletes() != null)
                        {
                            taskToSchedule.getCallbackAfterTaskCompletes().run();
                        }
                    }
                }
                finally
                {
                    if(remainingRetries <= 0)
                    {
                        if(removeTaskFromMapper)
                        {
                            scheduledTasksToRunnablesMapper.remove(taskToSchedule.getTaskID());
                        }
                        if(taskToSchedule.getCallbackAfterTaskCompletes() != null)
                        {
                            taskToSchedule.getCallbackAfterTaskCompletes().run();
                        }
                    }
                }
            }
        };
    }*/
}
