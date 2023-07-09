package io.github.orionlibs.task_scheduler.utils;

import java.util.logging.Handler;
import java.util.logging.Logger;

public class RunnableExample implements Runnable
{
    private final static Logger log;

    static
    {
        log = Logger.getLogger(RunnableExample.class.getName());
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
        log.info("Runnable is running");
    }
}