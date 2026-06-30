package ru.mihaliks.finance.core.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import ru.mihaliks.finance.core.domain.Category;
import ru.mihaliks.finance.core.domain.Family;
import ru.mihaliks.finance.core.domain.FamilyMember;
import ru.mihaliks.finance.core.domain.Operation;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class FinanceDtos {
    private FinanceDtos() {
    }

    public record CreateFamilyRequest(@NotBlank @Size(max = 100) String name) {
    }

    public record AddMemberRequest(@NotBlank @Email String email) {
    }

    public record CreateCategoryRequest(
            @NotBlank @Size(max = 100) String name,
            @NotNull OperationType type
    ) {
    }

    public record SaveOperationRequest(
            @NotNull UUID categoryId,
            UUID familyId,
            @NotNull OperationType type,
            @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
            @NotNull @PastOrPresent LocalDate operationDate,
            @Size(max = 500) String description
    ) {
    }

    public enum OperationType { INCOME, EXPENSE }

    public record FamilyResponse(UUID id, String name, UUID ownerId, Instant createdAt) {
        public static FamilyResponse from(Family family) {
            return new FamilyResponse(family.id(), family.name(), family.ownerId(), family.createdAt());
        }
    }

    public record MemberResponse(UUID id, UUID familyId, UUID userId, String role, Instant joinedAt) {
        public static MemberResponse from(FamilyMember member) {
            return new MemberResponse(member.id(), member.familyId(), member.userId(), member.role(), member.joinedAt());
        }
    }

    public record CategoryResponse(UUID id, String name, String type, boolean system, UUID ownerId) {
        public static CategoryResponse from(Category category) {
            return new CategoryResponse(category.id(), category.name(), category.type(), category.system(), category.ownerId());
        }
    }

    public record OperationResponse(
            UUID id, UUID userId, UUID familyId, UUID categoryId, String type, BigDecimal amount,
            LocalDate operationDate, String description, Instant createdAt
    ) {
        public static OperationResponse from(Operation operation) {
            return new OperationResponse(operation.id(), operation.userId(), operation.familyId(),
                    operation.categoryId(), operation.type(), operation.amount(), operation.operationDate(),
                    operation.description(), operation.createdAt());
        }
    }

    public record UserLookupResponse(UUID id, String email, String role) {
    }
}
