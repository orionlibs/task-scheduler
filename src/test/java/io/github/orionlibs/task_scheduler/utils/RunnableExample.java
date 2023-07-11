package io.github.orionlibs.task_scheduler.utils;

import java.util.logging.Handler;
import java.util.logging.Logger;

public class RunnableExample implements Runnable
{
    private static Logger log;
    private String logMessage;
    private long delayUntilItThrowsException;

    static
    {
        log = Logger.getLogger(RunnableExample.class.getName());
    }

    public RunnableExample()
    {
    }


    public void addLogMessage(String logMessage)
    {
        this.logMessage = logMessage;
    }


    public void addLogMessageAndDelay(String logMessage, long delayUntilItThrowsException)
    {
        addLogMessage(logMessage);
        this.delayUntilItThrowsException = delayUntilItThrowsException;
    }


    public static void addLogHandler(Handler handler)
    {
        log.addHandler(handler);
    }


    public static void removeLogHandler(Handler handler)
    {
        log.removeHandler(handler);
    }


    @Override
    public void run()
    {
        log.info(logMessage);
        if(delayUntilItThrowsException > 0L)
        {
            try
            {
                Thread.sleep(delayUntilItThrowsException);
                throw new RuntimeException("planned exception thrown");
            }
            catch(InterruptedException e)
            {
                throw new RuntimeException(e);
            }
        }
    }
}