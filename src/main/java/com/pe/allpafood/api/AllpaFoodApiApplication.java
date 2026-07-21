package com.pe.allpafood.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@SpringBootApplication
@EnableScheduling
public class AllpaFoodApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AllpaFoodApiApplication.class, args);
    }

}
