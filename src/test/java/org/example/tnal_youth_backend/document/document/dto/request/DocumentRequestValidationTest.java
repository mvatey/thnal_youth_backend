package org.example.tnal_youth_backend.document.document.dto.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentRequestValidationTest {

    private static jakarta.validation.ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void rejectsTitleLongerThanDatabaseColumn() {
        DocumentRequest request = new DocumentRequest(
                (short) 1,
                1L,
                "x".repeat(256),
                null,
                null,
                1L,
                null
        );

        assertThat(validator.validate(request))
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("title")
                                && violation.getMessage().contains("255")
                );
    }

    @Test
    void acceptsTitleAtDatabaseColumnLimit() {
        DocumentRequest request = new DocumentRequest(
                (short) 1,
                1L,
                "x".repeat(255),
                null,
                null,
                1L,
                null
        );

        assertThat(validator.validate(request)).isEmpty();
    }
}
