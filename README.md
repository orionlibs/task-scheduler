# Orion Task Scheduler
Java library facilitating single-execution task scheduling

Please check the wiki

https://orionlibs.github.io/task-scheduler/

Welcome to the task-scheduler wiki!  
This is a Java 17 library that allows you to schedule tasks that execute once at a future time.

You can import the library by using:  
```xml
<dependency>
    <groupId>io.github.orionlibs</groupId>
    <artifactId>task-scheduler</artifactId>
    <version>1.0.0</version>
</dependency>
```

To use this service in order to schedule 2 tasks, for example, that will execute 30 seconds from now and 8 hours from now then do:
```java
Runnable task1 = new MyRunnable();
SingleExecutionScheduleService taskScheduler = new SingleExecutionScheduleService();
ScheduledTask task1ToSchedule = ScheduledTask.builder()
                                            .taskID("task1")
                                            .taskToSchedule(task1)
                                            .delay(30L)
                                            .unit(TimeUnit.SECONDS)
                                            .build();
taskScheduler.schedule(scheduledTask1);
ScheduledFuture<?> scheduledTask1 = scheduledTask1.getTask();
ScheduledTask scheduledTask2 = ScheduledTask.builder()
                                            .taskID("task2")
                                            .taskToSchedule(task2)
                                            .delay(8L)
                                            .unit(TimeUnit.HOURS)
                                            .build();
taskScheduler.schedule(scheduledTask2);
ScheduledFuture<?> scheduledTask2 = scheduledTask2.getTask();
```

You can use the overloaded schedule method that accepts a Collection<ScheduledTask>.  
If you want to cancel, say, the 2nd task before it executes, then you can do:
```java
taskScheduler.schedule(task1ToSchedule);
taskScheduler.schedule(task2ToSchedule);
...
taskScheduler.cancel(scheduledTask2);
```

The default config for this library is:
```
orionlibs.task-scheduler.enabled=true
orionlibs.task-scheduler.cancellation.enabled=true
```

If you want to change the config (per SingleExecutionScheduleService instance) you can do, for example:
```java
taskScheduler.getConfig().updateProp("orionlibs.task-scheduler.cancellation.enabled", "false");
```

If orionlibs.task-scheduler.enabled=false then calls to the schedule method will throw a FeatureIsDisabledException.  
If orionlibs.task-scheduler.cancellation.enabled=false then calls to the cancel method will throw a FeatureIsDisabledException.