package com.marcosperboni.customerbff.domain.exception;

public class DownstreamUnavailableException extends RuntimeException {

    public DownstreamUnavailableException(String serviceName, Throwable cause) {
        super("Downstream service unavailable: " + serviceName, cause);
    }
}
