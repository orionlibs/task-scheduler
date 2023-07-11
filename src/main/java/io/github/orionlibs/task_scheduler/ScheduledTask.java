package io.github.orionlibs.task_scheduler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ScheduledTask
{
    private String taskID;
    private Runnable taskToSchedule;
    private long delay;
    private TimeUnit unit;
    private ScheduledFuture<?> task;
    private Runnable callbackAfterTaskCompletes;
    private Runnable callbackAfterTaskIsCancelled;
    private int numberOfRetriesOnError;
}
