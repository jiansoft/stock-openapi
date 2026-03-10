package com.jiansoft.stock_api.support;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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

    @Test
    void handleNotFoundShouldReturnJsonForApiRequestsThatAcceptJson() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/stock/missing");
        request.addHeader("Accept", "application/json");
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/api/stock/missing", "");

        var response = handler.handleNotFound(ex, request);
        var body = response.getBody();

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(body);
        assertEquals("application/json", response.getHeaders().getContentType().toString());
    }

    @Test
    void handleNotFoundShouldReturnEmptyBodyForNonJsonRequests() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/favicon.ico");
        request.addHeader("Accept", "image/avif,image/webp,image/apng,image/svg+xml");
        NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/favicon.ico", "");

        var response = handler.handleNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void handleUnhandledExceptionShouldReturnInternalServerError() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/stock/last_daily_quote");

        var response = handler.handleUnhandledException(new RuntimeException("boom"), request);
        var body = response.getBody();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(body);
        assertEquals("Internal Server Error. Please try again later.", body.message());
    }
}
