package com.cyberway.mp.demo.backenddemo.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cyberway.mp.bc.i18n.api.exception.BaseI18nException;
import com.cyberway.mp.demo.backenddemo.api.order.dto.OrderCreateDto;
import com.cyberway.mp.demo.backenddemo.api.order.dto.OrderModifyDto;
import com.cyberway.mp.demo.backenddemo.api.order.enums.OrderStatus;
import com.cyberway.mp.demo.backenddemo.domain.order.entity.Order;
import com.cyberway.mp.demo.backenddemo.domain.order.service.OrderDomainService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderApplicationServiceTest {

    @Mock
    private OrderDomainService orderDomainService;

    @InjectMocks
    private OrderApplicationService orderApplicationService;

    @Test
    void returnsGeneratedIdentifierAfterCreate() {
        OrderCreateDto dto = new OrderCreateDto();
        dto.setOrderNo("ORD-100");
        dto.setCustomerId(100L);
        dto.setAmount(new BigDecimal("20.10"));
        dto.setStatus(OrderStatus.CREATED);
        doAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(100L);
            return null;
        }).when(orderDomainService).createOrder(any(Order.class));

        long id = orderApplicationService.createOrder(dto);

        assertEquals(100L, id);
        verify(orderDomainService).createOrder(any(Order.class));
    }

    @Test
    void doesNotUpdateWhenModifyTargetDoesNotExist() {
        OrderModifyDto dto = new OrderModifyDto();
        dto.setOrderNo("ORD-101");
        dto.setCustomerId(101L);
        dto.setAmount(new BigDecimal("30.20"));
        dto.setStatus(OrderStatus.PAID);
        when(orderDomainService.getOrder(101L))
                .thenThrow(new BaseI18nException("90000102", "订单不存在"));

        assertThrows(BaseI18nException.class, () -> orderApplicationService.modifyOrder(101L, dto));
        verify(orderDomainService, never()).modifyOrder(any());
    }
}
