package com.jiansoft.stock_api.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaginationRequestTest {

    @Test
    void ofShouldUseDefaultsForNullValues() {
        PaginationRequest request = PaginationRequest.of(null, null);

        assertEquals(1, request.requestedPage());
        assertEquals(30, request.recordsPerPage());
    }

    @Test
    void ofShouldUseDefaultPageSizeForNonPositiveInput() {
        PaginationRequest request = PaginationRequest.of(5, 0);

        assertEquals(5, request.requestedPage());
        assertEquals(30, request.recordsPerPage());
    }

    @Test
    void ofShouldClampPageSizeToMaximum() {
        PaginationRequest request = PaginationRequest.of(1, 2000);

        assertEquals(1, request.requestedPage());
        assertEquals(1000, request.recordsPerPage());
    }
}
