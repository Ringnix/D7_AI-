package com.cyberway.mp.demo.backenddemo.domain.order.entity;

import com.cyberway.mp.bc.dal.annotation.Table;
import com.cyberway.mp.bc.dal.model.BusinessEntityWithAppInstance;
import com.cyberway.mp.demo.backenddemo.api.order.dto.OrderCreateDto;
import com.cyberway.mp.demo.backenddemo.api.order.dto.OrderModifyDto;
import com.cyberway.mp.demo.backenddemo.api.order.enums.OrderStatus;
import com.cyberway.mp.demo.backenddemo.api.order.vo.OrderListVo;
import com.cyberway.mp.demo.backenddemo.api.order.vo.OrderVo;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Table(name = "backend_demo_order")
public class Order extends BusinessEntityWithAppInstance {

    private String orderNo;

    private Long customerId;

    private BigDecimal amount;

    private OrderStatus status;

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo == null ? null : orderNo.trim();
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount == null ? null : amount.setScale(2, RoundingMode.UNNECESSARY);
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public void applyModifyDto(OrderModifyDto dto) {
        setOrderNo(dto.getOrderNo());
        setCustomerId(dto.getCustomerId());
        setAmount(dto.getAmount());
        setStatus(dto.getStatus());
    }

    public OrderListVo toListVo() {
        OrderListVo vo = new OrderListVo();
        vo.setId(getId());
        vo.setOrderNo(getOrderNo());
        vo.setCustomerId(getCustomerId());
        vo.setAmount(getAmount());
        vo.setStatus(getStatus());
        vo.setCreatedAt(getCreatedTime());
        return vo;
    }

    public OrderVo toVo() {
        OrderVo vo = new OrderVo();
        vo.setId(getId());
        vo.setOrderNo(getOrderNo());
        vo.setCustomerId(getCustomerId());
        vo.setAmount(getAmount());
        vo.setStatus(getStatus());
        vo.setCreatedAt(getCreatedTime());
        vo.setUpdatedAt(getLastModifiedTime());
        return vo;
    }

    public static Order fromCreateDto(OrderCreateDto dto) {
        Order order = new Order();
        order.setOrderNo(dto.getOrderNo());
        order.setCustomerId(dto.getCustomerId());
        order.setAmount(dto.getAmount());
        order.setStatus(dto.getStatus());
        return order;
    }
}
