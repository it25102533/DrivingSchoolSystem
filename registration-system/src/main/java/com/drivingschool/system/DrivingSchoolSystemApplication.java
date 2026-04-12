package com.drivingschool.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.drivingschool.system.backend")
@EnableJpaRepositories(basePackages = "com.drivingschool.system.backend")
public class DrivingSchoolSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(DrivingSchoolSystemApplication.class, args);
    }
}