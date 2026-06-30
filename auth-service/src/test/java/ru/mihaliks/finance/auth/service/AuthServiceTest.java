package ru.mihaliks.finance.auth.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.mihaliks.finance.auth.api.AuthDtos.LoginRequest;
import ru.mihaliks.finance.auth.api.AuthDtos.RegisterRequest;
import ru.mihaliks.finance.auth.domain.UserAccount;
import ru.mihaliks.finance.auth.repository.UserRepository;
import ru.mihaliks.finance.common.exception.ConflictException;
import ru.mihaliks.finance.common.exception.InvalidCredentialsException;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

class AuthServiceTest {
    private final UserRepository repository = Mockito.mock(UserRepository.class);
    private final PasswordEncoder encoder = Mockito.mock(PasswordEncoder.class);
    private final JwtService jwtService = Mockito.mock(JwtService.class);
    private final AuthService service = new AuthService(repository, encoder, jwtService);

    @Test
    void registersNewUser() {
        when(repository.existsByEmail("user@example.com")).thenReturn(Mono.just(false));
        when(encoder.encode("password")).thenReturn("hash");
        when(repository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(jwtService.create(any())).thenReturn("token");

        StepVerifier.create(service.register(new RegisterRequest("USER@example.com", "password")))
                .expectNextMatches(response -> response.email().equals("user@example.com")
                        && response.token().equals("token"))
                .verifyComplete();
    }

    @Test
    void rejectsDuplicateEmail() {
        when(repository.existsByEmail("user@example.com")).thenReturn(Mono.just(true));

        StepVerifier.create(service.register(new RegisterRequest("user@example.com", "password")))
                .expectError(ConflictException.class)
                .verify();
    }

    @Test
    void rejectsWrongPassword() {
        UserAccount user = new UserAccount(UUID.randomUUID(), "user@example.com", "hash", "USER", Instant.now(), 0L);
        when(repository.findByEmail("user@example.com")).thenReturn(Mono.just(user));
        when(encoder.matches("wrong", "hash")).thenReturn(false);

        StepVerifier.create(service.login(new LoginRequest("user@example.com", "wrong")))
                .expectError(InvalidCredentialsException.class)
                .verify();
    }

    @Test
    void demoAdminPasswordMatchesMigrationHash() {
        assertThat(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().matches(
                "password",
                "$2a$10$uzNtMTWMS8UDRHLRo0BbcuA1Dl.JvfGom3MRw8nGPsi4OY9pAQ4xy"
        )).isTrue();
    }
}
