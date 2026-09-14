package com.marcosperboni.customerbff.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marcosperboni.customerbff.domain.model.CustomerSummary;
import com.marcosperboni.customerbff.domain.port.CustomerClientPort;
import com.marcosperboni.customerbff.infrastructure.cache.PortalCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class CustomerCommandServiceTest {

    @Mock
    private CustomerClientPort customerClient;
    @Mock
    private PortalCacheService cacheService;

    private CustomerCommandService service;

    private static final CustomerSummary CUSTOMER = new CustomerSummary("cust-1", "Ada", "ada@example.com", "doc", "phone");

    @BeforeEach
    void setUp() {
        service = new CustomerCommandService(customerClient, cacheService);
    }

    @Test
    void createDoesNotTouchCache() {
        when(customerClient.createCustomer(any(), any())).thenReturn(Mono.just(CUSTOMER));

        StepVerifier.create(service.create(CUSTOMER, null)).expectNext(CUSTOMER).verifyComplete();

        org.mockito.Mockito.verifyNoInteractions(cacheService);
    }

    @Test
    void updateEvictsCacheAfterSuccess() {
        when(customerClient.updateCustomer(eq("cust-1"), any(), any())).thenReturn(Mono.just(CUSTOMER));
        when(cacheService.evict("cust-1")).thenReturn(Mono.just(true));

        StepVerifier.create(service.update("cust-1", CUSTOMER, null)).expectNext(CUSTOMER).verifyComplete();

        verify(cacheService).evict("cust-1");
    }

    @Test
    void deleteEvictsCacheAfterSuccess() {
        when(customerClient.deleteCustomer("cust-1", null)).thenReturn(Mono.empty());
        when(cacheService.evict("cust-1")).thenReturn(Mono.just(true));

        StepVerifier.create(service.delete("cust-1", null)).verifyComplete();

        verify(cacheService).evict("cust-1");
    }
}
