package com.cyberway.mp.demo.backenddemo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cyberway.mp.demo.backenddemo.api.order.dto.OrderCreateDto;
import com.cyberway.mp.demo.backenddemo.api.order.dto.OrderPageDto;
import com.cyberway.mp.demo.backenddemo.api.order.enums.OrderStatus;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class OrderDtoValidationTest {

    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void acceptsValidCreateRequest() {
        OrderCreateDto dto = createDto();

        assertTrue(validator.validate(dto).isEmpty());
    }

    @Test
    void rejectsInvalidCreateFields() {
        OrderCreateDto dto = new OrderCreateDto();
        dto.setOrderNo(" ");
        dto.setCustomerId(0L);
        dto.setAmount(new BigDecimal("0.001"));
        dto.setStatus(null);

        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    void enforcesPageBoundsAndRejectsCustomSorting() {
        OrderPageDto dto = new OrderPageDto();
        dto.setCurrent(0);
        dto.setPageSize(101);
        dto.setOrderField("amount");

        assertFalse(validator.validate(dto).isEmpty());
    }

    private OrderCreateDto createDto() {
        OrderCreateDto dto = new OrderCreateDto();
        dto.setOrderNo("ORD-200");
        dto.setCustomerId(200L);
        dto.setAmount(new BigDecimal("45.60"));
        dto.setStatus(OrderStatus.CREATED);
        return dto;
    }
}
