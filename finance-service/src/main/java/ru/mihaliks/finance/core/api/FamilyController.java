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
import ru.mihaliks.finance.core.api.FinanceDtos.AddMemberRequest;
import ru.mihaliks.finance.core.api.FinanceDtos.CreateFamilyRequest;
import ru.mihaliks.finance.core.api.FinanceDtos.FamilyResponse;
import ru.mihaliks.finance.core.api.FinanceDtos.MemberResponse;
import ru.mihaliks.finance.core.service.FamilyService;

import java.util.UUID;

@RestController
@RequestMapping("/api/families")
public class FamilyController {
    private final FamilyService service;

    public FamilyController(FamilyService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<FamilyResponse> create(@Valid @RequestBody CreateFamilyRequest request,
                                       @AuthenticationPrincipal Jwt jwt) {
        return service.create(request.name(), CurrentUser.from(jwt).id()).map(FamilyResponse::from);
    }

    @GetMapping
    public Flux<FamilyResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return service.list(CurrentUser.from(jwt).id()).map(FamilyResponse::from);
    }

    @GetMapping("/{familyId}/members")
    public Flux<MemberResponse> members(@PathVariable UUID familyId, @AuthenticationPrincipal Jwt jwt) {
        return service.members(familyId, CurrentUser.from(jwt).id()).map(MemberResponse::from);
    }

    @PostMapping("/{familyId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MemberResponse> addMember(@PathVariable UUID familyId,
                                          @Valid @RequestBody AddMemberRequest request,
                                          @AuthenticationPrincipal Jwt jwt) {
        return service.addMember(familyId, CurrentUser.from(jwt).id(), request.email()).map(MemberResponse::from);
    }

    @DeleteMapping("/{familyId}/members/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> removeMember(@PathVariable UUID familyId, @PathVariable UUID memberId,
                                   @AuthenticationPrincipal Jwt jwt) {
        return service.removeMember(familyId, CurrentUser.from(jwt).id(), memberId);
    }
}
