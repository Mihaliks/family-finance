package ru.mihaliks.finance.auth.api;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mihaliks.finance.auth.api.AuthDtos.AuthResponse;
import ru.mihaliks.finance.auth.api.AuthDtos.LoginRequest;
import ru.mihaliks.finance.auth.api.AuthDtos.RegisterRequest;
import ru.mihaliks.finance.auth.api.AuthDtos.UserResponse;
import ru.mihaliks.finance.auth.service.AuthService;
import ru.mihaliks.finance.common.exception.ForbiddenException;

@RestController
public class AuthController {
    private final AuthService service;
    private final String internalApiKey;

    public AuthController(AuthService service, @Value("${app.internal-api-key}") String internalApiKey) {
        this.service = service;
        this.internalApiKey = internalApiKey;
    }

    @PostMapping("/api/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return service.register(request);
    }

    @PostMapping("/api/auth/login")
    public Mono<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return service.login(request);
    }

    @GetMapping("/internal/users/by-email")
    public Mono<UserResponse> findByEmail(
            @RequestHeader("X-Internal-Api-Key") String apiKey,
            @RequestParam String email
    ) {
        if (!internalApiKey.equals(apiKey)) {
            throw new ForbiddenException("Некорректный внутренний API-ключ");
        }
        return service.findByEmail(email);
    }

    @GetMapping("/api/admin/users")
    public Flux<UserResponse> users() {
        return service.findAll();
    }
}
