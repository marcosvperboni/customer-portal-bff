package com.marcosperboni.customerbff.web;

import com.marcosperboni.customerbff.config.properties.JwtProperties;
import com.marcosperboni.customerbff.infrastructure.security.JwtService;
import com.marcosperboni.customerbff.web.dto.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.security.autoconfigure.web.reactive.ReactiveWebSecurityAutoConfiguration;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(controllers = AuthController.class, excludeAutoConfiguration = ReactiveWebSecurityAutoConfiguration.class)
@Import(AuthControllerTest.Config.class)
class AuthControllerTest {

    @org.springframework.beans.factory.annotation.Autowired
    private WebTestClient webTestClient;

    @org.springframework.boot.test.context.TestConfiguration
    static class Config {
        @org.springframework.context.annotation.Bean
        JwtProperties jwtProperties() {
            return new JwtProperties("test-secret-test-secret-test-secret-32b", 30);
        }

        @org.springframework.context.annotation.Bean
        JwtService jwtService(JwtProperties jwtProperties) {
            return new JwtService(jwtProperties);
        }
    }

    @Test
    void issuesTokenForValidDemoCredentials() {
        webTestClient.post().uri("/api/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new LoginRequest("demo", "demo123"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").isNotEmpty()
                .jsonPath("$.tokenType").isEqualTo("Bearer");
    }

    @Test
    void rejectsInvalidCredentials() {
        webTestClient.post().uri("/api/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new LoginRequest("demo", "wrong-password"))
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.error").isEqualTo("UNAUTHORIZED");
    }
}
