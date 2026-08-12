package com.rvy.scanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import com.rvy.scanner.config.DotenvLoader;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AlpacaOptionsScannerApplication {

    public static void main(String[] args) {
        DotenvLoader.load();
        SpringApplication.run(AlpacaOptionsScannerApplication.class, args);
    }
}
