package ru.mihaliks.finance.core.api;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mihaliks.finance.common.security.CurrentUser;
import ru.mihaliks.finance.core.api.FinanceDtos.OperationResponse;
import ru.mihaliks.finance.core.api.FinanceDtos.SaveOperationRequest;
import ru.mihaliks.finance.core.service.OperationService;

import java.time.LocalDate;
import java.util.UUID;

@RestController
public class OperationController {
    private final OperationService service;

    public OperationController(OperationService service) {
        this.service = service;
    }

    @PostMapping("/api/operations")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<OperationResponse> create(@Valid @RequestBody SaveOperationRequest request,
                                          @AuthenticationPrincipal Jwt jwt) {
        return service.create(request, CurrentUser.from(jwt).id()).map(OperationResponse::from);
    }

    @PutMapping("/api/operations/{id}")
    public Mono<OperationResponse> update(@PathVariable UUID id, @Valid @RequestBody SaveOperationRequest request,
                                          @AuthenticationPrincipal Jwt jwt) {
        return service.update(id, request, CurrentUser.from(jwt).id()).map(OperationResponse::from);
    }

    @DeleteMapping("/api/operations/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return service.delete(id, CurrentUser.from(jwt).id());
    }

    @GetMapping("/api/operations")
    public Flux<OperationResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return service.listOwn(CurrentUser.from(jwt).id()).map(OperationResponse::from);
    }

    @GetMapping("/internal/reports/operations")
    public Flux<OperationResponse> report(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) UUID familyId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return service.report(CurrentUser.from(jwt).id(), familyId, from, to).map(OperationResponse::from);
    }

    @GetMapping("/internal/reports/access-check")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> accessCheck(@RequestParam UUID familyId, @AuthenticationPrincipal Jwt jwt) {
        return service.report(CurrentUser.from(jwt).id(), familyId, LocalDate.now(), LocalDate.now())
                .then();
    }
}
