package io.github.orionlibs.task_scheduler.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages =
                {"io.github.orionlibs"})
public class FakeSpringMVCConfiguration
{
}
