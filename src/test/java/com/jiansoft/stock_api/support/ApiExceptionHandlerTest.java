package com.jiansoft.stock_api.support;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void handleBadRequestShouldIncludeParameterNameForTypeMismatch() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
            "abc",
            Integer.class,
            "requestedPage",
            null,
            null
        );

        var response = handler.handleBadRequest(ex);
        var body = response.getBody();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(body);
        assertEquals(
            "Invalid request parameter 'requestedPage'.",
            body.message()
        );
    }

    @Test
    void handleBadRequestShouldIncludeParameterNameForMissingParameter() {
        MissingServletRequestParameterException ex = new MissingServletRequestParameterException(
            "recordsPerPage",
            "Integer"
        );

        var response = handler.handleBadRequest(ex);
        var body = response.getBody();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(body);
        assertEquals(
            "Missing required request parameter 'recordsPerPage'.",
            body.message()
        );
    }

    @Test
    void handleBadRequestShouldFallbackToGenericMessage() {
        var ex = new RuntimeException("bad");

        var response = handler.handleBadRequest(ex);
        var body = response.getBody();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(body);
        assertEquals("Invalid request parameter.", body.message());
    }

    @Test
    void handleBadRequestShouldUseIllegalArgumentExceptionMessage() {
        var ex = new IllegalArgumentException("year must be within 1900-2100.");

        var response = handler.handleBadRequest(ex);
        var body = response.getBody();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(body);
        assertEquals("year must be within 1900-2100.", body.message());
    }
}
