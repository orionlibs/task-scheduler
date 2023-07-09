package io.github.orionlibs.task_scheduler;

public class FeatureIsDisabledException extends Exception
{
    private static final String DefaultErrorMessage = "The Orion single task scheduler is disabled by configuration.";


    public FeatureIsDisabledException()
    {
        super(DefaultErrorMessage);
    }


    public FeatureIsDisabledException(String message)
    {
        super(message);
    }


    public FeatureIsDisabledException(String errorMessage, Object... arguments)
    {
        super(String.format(errorMessage, arguments));
    }


    public FeatureIsDisabledException(Throwable cause, String errorMessage, Object... arguments)
    {
        super(String.format(errorMessage, arguments), cause);
    }


    public FeatureIsDisabledException(Throwable cause)
    {
        super(DefaultErrorMessage, cause);
    }
}