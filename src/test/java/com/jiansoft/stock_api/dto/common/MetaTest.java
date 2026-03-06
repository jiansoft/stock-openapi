package com.jiansoft.stock_api.dto.common;

import com.jiansoft.stock_api.support.PaginationRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MetaTest {

    @Test
    void fromShouldUseCeilingDivisionForTotalPages() {
        Meta meta = Meta.from(31, PaginationRequest.of(1, 30));

        assertEquals(2, meta.totalPages());
        assertEquals(1, meta.currentPage());
    }

    @Test
    void fromShouldClampCurrentPageToTotalPages() {
        Meta meta = Meta.from(31, PaginationRequest.of(10, 30));

        assertEquals(2, meta.totalPages());
        assertEquals(2, meta.currentPage());
        assertEquals(30, meta.offset());
    }

    @Test
    void fromShouldClampHugeTotalPagesToIntegerMaxValue() {
        Meta meta = Meta.from(Long.MAX_VALUE, PaginationRequest.of(1, 1));

        assertEquals(Integer.MAX_VALUE, meta.totalPages());
    }

    @Test
    void offsetShouldClampToIntegerMaxValueWhenOverflowWouldHappen() {
        Meta meta = new Meta(Integer.MAX_VALUE, 1000, Integer.MAX_VALUE, Long.MAX_VALUE);

        assertEquals(Integer.MAX_VALUE, meta.offset());
    }
}
