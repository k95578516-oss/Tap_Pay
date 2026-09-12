package com.example.tap_pay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.tap_pay")
public class TapPayApplication {

    public static void main(String[] args) {
        SpringApplication.run(TapPayApplication.class, args);
    }
}