package io.github.orionlibs.task_scheduler.utils;

import java.util.logging.Handler;
import java.util.logging.Logger;

public class Callback implements Runnable
{
    public static Logger log;


    static
    {
        log = Logger.getLogger(Callback.class.getName());
    }


    @Override public void run()
    {
        log.info("callback has been called");
    }


    public static void addLogHandler(Handler handler)
    {
        log.addHandler(handler);
    }


    public static void removeLogHandler(Handler handler)
    {
        log.removeHandler(handler);
    }
}
