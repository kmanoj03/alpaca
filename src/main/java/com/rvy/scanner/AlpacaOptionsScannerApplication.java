package com.rvy.scanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AlpacaOptionsScannerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlpacaOptionsScannerApplication.class, args);
    }
}
