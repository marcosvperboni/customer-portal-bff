package com.marcosperboni.customerbff.web.dto;

public record TokenResponse(String accessToken, String tokenType, long expiresInSeconds) {
}
