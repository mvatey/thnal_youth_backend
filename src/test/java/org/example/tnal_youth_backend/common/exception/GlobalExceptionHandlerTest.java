package org.example.tnal_youth_backend.common.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void businessException_preservesExplicitHttpStatusAndErrorCode() {
        var response = handler.handleBusinessException(
                new BusinessException(
                        "RATE_LIMITED",
                        "Please try again later",
                        HttpStatus.TOO_MANY_REQUESTS
                )
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getErrorCode()).isEqualTo("RATE_LIMITED");
        assertThat(response.getBody().getMessage()).isEqualTo("Please try again later");
    }

    @Test
    void resourceNotFound_usesSharedNotFoundEnvelope() {
        var response = handler.handleResourceNotFoundException(
                new ResourceNotFoundException("Member was not found")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(response.getBody().getMessage()).isEqualTo("Member was not found");
    }
}
