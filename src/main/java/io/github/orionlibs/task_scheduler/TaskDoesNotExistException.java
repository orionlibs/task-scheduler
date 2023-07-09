package io.github.orionlibs.task_scheduler;

public class TaskDoesNotExistException extends Exception
{
    private static final String DefaultErrorMessage = "The given task does not exist in the scheduler or it has already terminated.";


    public TaskDoesNotExistException()
    {
        super(DefaultErrorMessage);
    }


    public TaskDoesNotExistException(String message)
    {
        super(message);
    }


    public TaskDoesNotExistException(String errorMessage, Object... arguments)
    {
        super(String.format(errorMessage, arguments));
    }


    public TaskDoesNotExistException(Throwable cause, String errorMessage, Object... arguments)
    {
        super(String.format(errorMessage, arguments), cause);
    }


    public TaskDoesNotExistException(Throwable cause)
    {
        super(DefaultErrorMessage, cause);
    }
}