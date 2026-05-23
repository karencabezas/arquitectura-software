package com.aerorescue.mission;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MissionServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MissionServiceApplication.class, args);
    }
}
