package com.marcosperboni.customerbff.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.clients")
public record ClientsProperties(Service customerService, Service orderService, Service paymentService) {

    public record Service(String baseUrl, long timeoutMillis) {
    }
}
