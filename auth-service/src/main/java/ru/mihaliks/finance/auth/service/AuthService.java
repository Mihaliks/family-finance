package ru.mihaliks.finance.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mihaliks.finance.auth.api.AuthDtos.AuthResponse;
import ru.mihaliks.finance.auth.api.AuthDtos.LoginRequest;
import ru.mihaliks.finance.auth.api.AuthDtos.RegisterRequest;
import ru.mihaliks.finance.auth.api.AuthDtos.UserResponse;
import ru.mihaliks.finance.auth.domain.UserAccount;
import ru.mihaliks.finance.auth.repository.UserRepository;
import ru.mihaliks.finance.common.exception.ConflictException;
import ru.mihaliks.finance.common.exception.InvalidCredentialsException;
import ru.mihaliks.finance.common.exception.NotFoundException;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public Mono<AuthResponse> register(RegisterRequest request) {
        String email = normalize(request.email());
        return repository.existsByEmail(email)
                .flatMap(exists -> exists
                        ? Mono.error(new ConflictException("Пользователь с таким email уже существует"))
                        : repository.save(new UserAccount(UUID.randomUUID(), email,
                                passwordEncoder.encode(request.password()), "USER", Instant.now(), null)))
                .map(this::response);
    }

    public Mono<AuthResponse> login(LoginRequest request) {
        return repository.findByEmail(normalize(request.email()))
                .filter(user -> passwordEncoder.matches(request.password(), user.passwordHash()))
                .switchIfEmpty(Mono.error(new InvalidCredentialsException()))
                .map(this::response);
    }

    public Mono<UserResponse> findByEmail(String email) {
        return repository.findByEmail(normalize(email))
                .switchIfEmpty(Mono.error(new NotFoundException("Пользователь не найден")))
                .map(this::userResponse);
    }

    public Flux<UserResponse> findAll() {
        return repository.findAll().map(this::userResponse);
    }

    private AuthResponse response(UserAccount user) {
        return new AuthResponse(jwtService.create(user), user.id(), user.email(), user.role());
    }

    private UserResponse userResponse(UserAccount user) {
        return new UserResponse(user.id(), user.email(), user.role());
    }

    private String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
