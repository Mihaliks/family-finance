package ru.mihaliks.finance.core.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mihaliks.finance.common.security.CurrentUser;
import ru.mihaliks.finance.core.api.FinanceDtos.CategoryResponse;
import ru.mihaliks.finance.core.api.FinanceDtos.CreateCategoryRequest;
import ru.mihaliks.finance.core.service.CategoryService;

import java.util.UUID;

@RestController
public class CategoryController {
    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @GetMapping("/api/categories")
    public Flux<CategoryResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return service.list(CurrentUser.from(jwt).id()).map(CategoryResponse::from);
    }

    @PostMapping("/api/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CategoryResponse> create(@Valid @RequestBody CreateCategoryRequest request,
                                         @AuthenticationPrincipal Jwt jwt) {
        return service.create(request, CurrentUser.from(jwt).id(), false).map(CategoryResponse::from);
    }

    @PostMapping("/api/admin/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CategoryResponse> createSystem(@Valid @RequestBody CreateCategoryRequest request,
                                               @AuthenticationPrincipal Jwt jwt) {
        return service.create(request, CurrentUser.from(jwt).id(), true).map(CategoryResponse::from);
    }

    @DeleteMapping("/api/categories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        CurrentUser user = CurrentUser.from(jwt);
        return service.delete(id, user.id(), user.isAdmin());
    }
}
