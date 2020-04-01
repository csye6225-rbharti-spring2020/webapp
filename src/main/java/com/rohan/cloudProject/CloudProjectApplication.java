package com.rohan.cloudProject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
@EnableJpaRepositories("com.rohan.cloudProject.repository")
@EnableScheduling
public class CloudProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudProjectApplication.class, args);
    }

}
