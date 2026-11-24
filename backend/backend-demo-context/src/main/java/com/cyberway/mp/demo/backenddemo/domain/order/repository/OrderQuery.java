package com.cyberway.mp.demo.backenddemo.domain.order.repository;

import com.cyberway.mp.demo.backenddemo.api.order.dto.OrderPageDto;
import com.cyberway.mp.demo.backenddemo.api.order.enums.OrderStatus;

public class OrderQuery {

    private String orderNoLike;

    private Long customerId;

    private OrderStatus status;

    public String getOrderNoLike() {
        return orderNoLike;
    }

    public void setOrderNoLike(String orderNoLike) {
        this.orderNoLike = orderNoLike;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public static OrderQuery fromPageDto(OrderPageDto dto) {
        OrderQuery query = new OrderQuery();
        query.setOrderNoLike(dto.getOrderNoLike());
        query.setCustomerId(dto.getCustomerId());
        query.setStatus(dto.getStatus());
        return query;
    }
}
