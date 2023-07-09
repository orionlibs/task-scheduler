package io.github.orionlibs.task_scheduler.config;

import io.github.orionlibs.task_scheduler.SingleExecutionScheduler;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

public class FakeTestingSpringConfiguration
{
    @Configuration
    @ComponentScan(basePackages =
                    {"io.github.orionlibs"})
    public static class FakeConfiguration
    {
        private Environment springEnv;


        @Autowired
        public FakeConfiguration(final Environment springEnv) throws IOException
        {
            this.springEnv = springEnv;
        }


        @Bean
        public SingleExecutionScheduler singleExecutionScheduler() throws IOException
        {
            return new SingleExecutionScheduler(springEnv);
        }
    }
}