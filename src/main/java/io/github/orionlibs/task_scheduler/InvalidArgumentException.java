package io.github.orionlibs.task_scheduler;

public class InvalidArgumentException extends Exception
{
    private static final String DefaultErrorMessage = "Invalid argument provided.";


    public InvalidArgumentException()
    {
        super(DefaultErrorMessage);
    }


    public InvalidArgumentException(String message)
    {
        super(message);
    }


    public InvalidArgumentException(String errorMessage, Object... arguments)
    {
        super(String.format(errorMessage, arguments));
    }


    public InvalidArgumentException(Throwable cause, String errorMessage, Object... arguments)
    {
        super(String.format(errorMessage, arguments), cause);
    }


    public InvalidArgumentException(Throwable cause)
    {
        super(DefaultErrorMessage, cause);
    }
}