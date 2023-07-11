package io.github.orionlibs.task_scheduler;

import java.util.concurrent.ConcurrentMap;

public class Utils
{
    public static Runnable buildTaskWrapper(ScheduledTask taskToSchedule, ConcurrentMap<String, ScheduledTask> scheduledTasksToRunnablesMapper)
    {
        return () -> {
            try
            {
                taskToSchedule.getTaskToSchedule().run();
            }
            finally
            {
                scheduledTasksToRunnablesMapper.remove(taskToSchedule.getTaskID());
                if(taskToSchedule.getCallbackAfterTaskCompletes() != null)
                {
                    taskToSchedule.getCallbackAfterTaskCompletes().run();
                }
            }
        };
    }
}
