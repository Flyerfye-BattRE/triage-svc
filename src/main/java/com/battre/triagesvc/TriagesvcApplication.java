package com.battre.triagesvc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class TriagesvcApplication {

    public static void main(String[] args) {
        SpringApplication.run(TriagesvcApplication.class, args);
    }

}
