package io.github.orionlibs.task_scheduler.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

public class FakeTestingSpringConfiguration
{
    @Configuration
    @Import(
                    {FakeSpringMVCConfiguration.class})
    public static class FakeConfiguration
    {
    }
}