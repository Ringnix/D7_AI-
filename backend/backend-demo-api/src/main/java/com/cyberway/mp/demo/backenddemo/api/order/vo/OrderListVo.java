package com.cyberway.mp.demo.backenddemo.api.order.vo;

import com.cyberway.mp.demo.backenddemo.api.order.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.Date;

@Schema(description = "订单列表项")
public class OrderListVo {

    @Schema(title = "订单ID")
    private Long id;

    @Schema(title = "订单号")
    private String orderNo;

    @Schema(title = "客户ID")
    private Long customerId;

    @Schema(title = "订单金额")
    private BigDecimal amount;

    @Schema(title = "订单状态")
    private OrderStatus status;

    @Schema(title = "创建时间")
    private Date createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
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
        this.amount = amount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
