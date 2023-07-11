package io.github.orionlibs.task_scheduler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *  A class that holds information about a task.
 *  The mandatory fields that have to be set in order to schedule a task are:
 *  taskID (String), taskToSchedule (Runnable), delay (long), unit (TimeUnit).
 *  <br>
 *  If you want after the execution of this task to have another Runnable to be called then
 *  set also the callbackAfterTaskCompletes (Runnable) field.
 *  <br>
 *  If you want after the cancellation of this task to have another Runnable to be called then
 *  set also the callbackAfterTaskIsCancelled (Runnable) field.
 *  <br>
 *  If the execution fails and you want it to retry for N times then
 *  set also the numberOfRetriesOnError (int) field.
 */
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


    /**
     * It validates the mandatory inputs.
     * @return
     * @throws InvalidArgumentException
     */
    public boolean validate() throws InvalidArgumentException
    {
        if(taskID == null || taskID.isEmpty())
        {
            throw new InvalidArgumentException("taskID cannot be null/empty.");
        }
        if(taskToSchedule == null)
        {
            throw new InvalidArgumentException("taskToSchedule cannot be null.");
        }
        if(unit == null)
        {
            throw new InvalidArgumentException("unit cannot be null.");
        }
        normalise();
        return true;
    }


    private void normalise()
    {
        if(delay < 0L)
        {
            delay = 0L;
        }
        if(numberOfRetriesOnError < 0)
        {
            numberOfRetriesOnError = 0;
        }
    }
}
