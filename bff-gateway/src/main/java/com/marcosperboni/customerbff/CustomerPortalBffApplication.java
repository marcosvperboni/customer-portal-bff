package com.marcosperboni.customerbff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CustomerPortalBffApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerPortalBffApplication.class, args);
    }
}
