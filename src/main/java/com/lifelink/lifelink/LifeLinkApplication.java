package com.lifelink.lifelink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(
    scanBasePackages = {
        "com.lifelink.lifelink",
        "controller.model"
    }
)
@EnableJpaRepositories(
    basePackages = "controller.model.repository"
)
@EntityScan(
    basePackages = "controller.model.repository"
)
public class LifeLinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(LifeLinkApplication.class, args);
    }

}