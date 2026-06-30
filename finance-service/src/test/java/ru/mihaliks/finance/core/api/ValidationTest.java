package ru.mihaliks.finance.core.api;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import ru.mihaliks.finance.core.api.FinanceDtos.OperationType;
import ru.mihaliks.finance.core.api.FinanceDtos.SaveOperationRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsNonPositiveAmountAndFutureDate() {
        SaveOperationRequest request = new SaveOperationRequest(UUID.randomUUID(), null, OperationType.EXPENSE,
                BigDecimal.ZERO, LocalDate.now().plusDays(1), "");

        assertThat(validator.validate(request))
                .extracting(error -> error.getPropertyPath().toString())
                .contains("amount", "operationDate");
    }
}
