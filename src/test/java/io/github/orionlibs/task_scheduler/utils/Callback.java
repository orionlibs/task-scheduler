package io.github.orionlibs.task_scheduler.utils;

import java.util.logging.Logger;

public class Callback implements Runnable
{
    public static Logger log = Logger.getLogger(Callback.class.getName());


    @Override public void run()
    {
        log.info("callback has been called");
    }
}
