package com.npst.watchlist.exception;

import com.npst.watchlist.dto.response.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import jakarta.validation.ConstraintViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/movies/test");
    }

    @Test
    void shouldMapValidationFailureToInvalidInput() throws Exception {
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "title", "must not be blank"));

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(methodParameter(), bindingResult);

        ResponseEntity<ErrorResponse> response =
                handler.handleMethodArgumentNotValid(exception, request);

        assertError(response, 400, "INVALID_INPUT", "Request validation failed.");
    }

    @Test
    void shouldMapMalformedJsonToInvalidInput() {
        ResponseEntity<ErrorResponse> response = handler.handleHttpMessageNotReadable(
                new HttpMessageNotReadableException("invalid json"), request
        );

        assertError(response, 400, "INVALID_INPUT", "Malformed JSON request.");
    }

    @Test
    void shouldMapConstraintViolationToInvalidInput() {
        ResponseEntity<ErrorResponse> response = handler.handleConstraintViolation(
                new ConstraintViolationException("invalid parameter", java.util.Set.of()), request
        );

        assertError(response, 400, "INVALID_INPUT", "Request validation failed.");
    }

    @Test
    void shouldMapTypeMismatchToMalformedParameter() {
        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentTypeMismatch(
                new MethodArgumentTypeMismatchException(
                        "not-a-uuid", java.util.UUID.class, "id", methodParameter(), null
                ), request
        );

        assertError(response, 400, "MALFORMED_PARAMETER", "Request parameter has an invalid format.");
    }

    @Test
    void shouldMapResourceNotFound() {
        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(
                new ResourceNotFoundException("Movie not found."), request
        );

        assertError(response, 404, "RESOURCE_NOT_FOUND", "Movie not found.");
    }

    @Test
    void shouldMapDuplicateResource() {
        ResponseEntity<ErrorResponse> response = handler.handleDuplicateResource(
                new DuplicateResourceException("Movie already exists."), request
        );

        assertError(response, 409, "DUPLICATE_RESOURCE", "Movie already exists.");
    }

    @Test
    void shouldMapInvalidPagination() {
        ResponseEntity<ErrorResponse> response = handler.handleInvalidPagination(
                new InvalidPaginationException("Page must be greater than or equal to 0."), request
        );

        assertError(response, 400, "INVALID_PAGINATION", "Page must be greater than or equal to 0.");
    }

    @Test
    void shouldMapKnownMovieUniquenessViolationToDuplicateResource() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
                "constraint violation", new RuntimeException("uk_movie_title_year_ci")
        );

        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolation(exception, request);

        assertError(
                response,
                409,
                "DUPLICATE_RESOURCE",
                "Movie with the same title and release year already exists."
        );
    }

    @Test
    void shouldMapUnknownDataIntegrityViolationToInternalServerError() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
                "constraint violation", new RuntimeException("unknown constraint")
        );

        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolation(exception, request);

        assertError(response, 500, "INTERNAL_SERVER_ERROR", "An unexpected error occurred.");
    }

    @Test
    void shouldMapUnexpectedExceptionToSafeInternalServerError() {
        ResponseEntity<ErrorResponse> response = handler.handleUnexpectedException(
                new RuntimeException("database password and SQL must never be exposed"), request
        );

        assertError(response, 500, "INTERNAL_SERVER_ERROR", "An unexpected error occurred.");
        assertEquals("/api/v1/movies/test", response.getBody().getPath());
    }

    private MethodParameter methodParameter() throws NoSuchMethodException {
        return new MethodParameter(
                GlobalExceptionHandlerTest.class.getDeclaredMethod("dummyParameter", java.util.UUID.class),
                0
        );
    }

    @SuppressWarnings("unused")
    private void dummyParameter(java.util.UUID id) {
    }

    private void assertError(
            ResponseEntity<ErrorResponse> response,
            int status,
            String code,
            String message
    ) {
        assertEquals(status, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(status, response.getBody().getStatus());
        assertEquals(code, response.getBody().getCode());
        assertEquals(message, response.getBody().getMessage());
        assertEquals("/api/v1/movies/test", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }
}
