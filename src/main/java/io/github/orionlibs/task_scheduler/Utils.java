package io.github.orionlibs.task_scheduler;

import java.util.concurrent.ConcurrentMap;

public class Utils
{
    public static Runnable buildTaskWrappr(ScheduledTask taskToSchedule, ConcurrentMap<String, ScheduledTask> scheduledTasksToRunnablesMapper)
    {
        return () -> {
            try
            {
                taskToSchedule.getCommand().run();
            }
            finally
            {
                scheduledTasksToRunnablesMapper.remove(taskToSchedule.getTaskID());
            }
        };
    }
}
