package io.github.orionlibs.project_name.config;

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