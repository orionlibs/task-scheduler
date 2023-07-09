package io.github.orionlibs.task_scheduler.config;

import io.github.orionlibs.task_scheduler.NewClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages =
                {"io.github.orionlibs"})
public class FakeSpringMVCConfiguration
{
    @Bean
    public NewClass newClass()
    {
        return new NewClass();
    }
}
