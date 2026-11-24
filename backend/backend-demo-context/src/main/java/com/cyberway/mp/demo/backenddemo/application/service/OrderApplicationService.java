package com.cyberway.mp.demo.backenddemo.application.service;

import com.cyberway.mp.bc.common.api.Page;
import com.cyberway.mp.demo.backenddemo.api.order.OrderApi;
import com.cyberway.mp.demo.backenddemo.api.order.dto.OrderCreateDto;
import com.cyberway.mp.demo.backenddemo.api.order.dto.OrderModifyDto;
import com.cyberway.mp.demo.backenddemo.api.order.dto.OrderPageDto;
import com.cyberway.mp.demo.backenddemo.api.order.vo.OrderListVo;
import com.cyberway.mp.demo.backenddemo.api.order.vo.OrderVo;
import com.cyberway.mp.demo.backenddemo.domain.order.entity.Order;
import com.cyberway.mp.demo.backenddemo.domain.order.service.OrderDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.validation.annotation.Validated;

@Validated
public class OrderApplicationService implements OrderApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderApplicationService.class);

    private final OrderDomainService orderDomainService;

    public OrderApplicationService(OrderDomainService orderDomainService) {
        this.orderDomainService = orderDomainService;
    }

    @Override
    public long createOrder(OrderCreateDto dto) {
        long startedAt = System.nanoTime();
        try {
            Order order = Order.fromCreateDto(dto);
            orderDomainService.createOrder(order);
            logSuccess("create", order.getId(), startedAt);
            return order.getId();
        } catch (RuntimeException exception) {
            logFailure("create", null, startedAt, exception);
            throw exception;
        }
    }

    @Override
    public OrderVo getOrder(long id) {
        return orderDomainService.getOrder(id).toVo();
    }

    @Override
    public Page<OrderListVo> queryOrders(OrderPageDto dto) {
        return orderDomainService.pageQueryOrders(dto);
    }

    @Override
    public void modifyOrder(long id, OrderModifyDto dto) {
        long startedAt = System.nanoTime();
        try {
            Order order = orderDomainService.getOrder(id);
            order.applyModifyDto(dto);
            orderDomainService.modifyOrder(order);
            logSuccess("modify", id, startedAt);
        } catch (RuntimeException exception) {
            logFailure("modify", id, startedAt, exception);
            throw exception;
        }
    }

    @Override
    public void deleteOrder(long id) {
        long startedAt = System.nanoTime();
        try {
            orderDomainService.deleteOrder(id);
            logSuccess("delete", id, startedAt);
        } catch (RuntimeException exception) {
            logFailure("delete", id, startedAt, exception);
            throw exception;
        }
    }

    private void logSuccess(String operation, Long orderId, long startedAt) {
        LOGGER.info(
                "订单操作 requestId={} operation={} orderId={} result=success elapsedMs={}",
                requestId(),
                operation,
                orderId,
                elapsedMillis(startedAt));
    }

    private void logFailure(String operation, Long orderId, long startedAt, RuntimeException exception) {
        LOGGER.warn(
                "订单操作 requestId={} operation={} orderId={} result=failure error={} elapsedMs={}",
                requestId(),
                operation,
                orderId,
                exception.getClass().getSimpleName(),
                elapsedMillis(startedAt));
    }

    private String requestId() {
        String requestId = MDC.get("requestId");
        if (requestId != null && !requestId.isBlank()) {
            return requestId;
        }
        String traceId = MDC.get("traceId");
        return traceId == null || traceId.isBlank() ? "N/A" : traceId;
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000L;
    }
}
