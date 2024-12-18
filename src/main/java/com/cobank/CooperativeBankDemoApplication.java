package com.cobank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.retry.annotation.EnableRetry;

@EnableCaching
@EnableRetry
@SpringBootApplication
public class CooperativeBankDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CooperativeBankDemoApplication.class, args);
    }

}
