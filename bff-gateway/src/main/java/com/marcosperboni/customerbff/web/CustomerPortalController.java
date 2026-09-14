package com.marcosperboni.customerbff.web;

import com.marcosperboni.customerbff.application.CustomerCommandService;
import com.marcosperboni.customerbff.application.CustomerPortalService;
import com.marcosperboni.customerbff.web.dto.CustomerCommandRequest;
import com.marcosperboni.customerbff.web.dto.CustomerPortalResponse;
import com.marcosperboni.customerbff.web.dto.CustomerResponse;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/portal/customers")
public class CustomerPortalController {

    private final CustomerPortalService portalService;
    private final CustomerCommandService commandService;

    public CustomerPortalController(CustomerPortalService portalService, CustomerCommandService commandService) {
        this.portalService = portalService;
        this.commandService = commandService;
    }

    @GetMapping("/{customerId}")
    public Mono<CustomerPortalResponse> getPortal(@PathVariable String customerId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        return portalService.getPortal(customerId, authorization).map(CustomerPortalResponse::from);
    }

    @PostMapping
    public Mono<ResponseEntity<CustomerResponse>> create(@Valid @RequestBody CustomerCommandRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        return commandService.create(request.toSummary(null), authorization)
                .map(created -> ResponseEntity.created(URI.create("/api/portal/customers/" + created.id()))
                        .body(CustomerResponse.from(created)));
    }

    @PutMapping("/{customerId}")
    public Mono<CustomerResponse> update(@PathVariable String customerId,
            @Valid @RequestBody CustomerCommandRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        return commandService.update(customerId, request.toSummary(customerId), authorization)
                .map(CustomerResponse::from);
    }

    @DeleteMapping("/{customerId}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable String customerId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        return commandService.delete(customerId, authorization).thenReturn(ResponseEntity.noContent().build());
    }
}
