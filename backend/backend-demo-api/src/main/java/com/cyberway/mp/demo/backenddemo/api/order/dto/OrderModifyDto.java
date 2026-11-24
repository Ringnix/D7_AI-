package com.cyberway.mp.demo.backenddemo.api.order.dto;

import com.cyberway.mp.demo.backenddemo.api.order.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Schema(description = "完整修改订单参数")
public class OrderModifyDto {

    @NotBlank(message = "订单号不能为空")
    @Size(max = 32, message = "订单号长度不能超过32")
    @Schema(title = "订单号", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 32)
    private String orderNo;

    @NotNull(message = "客户ID不能为空")
    @Positive(message = "客户ID必须大于0")
    @Schema(title = "客户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long customerId;

    @NotNull(message = "订单金额不能为空")
    @DecimalMin(value = "0.01", message = "订单金额必须大于0")
    @Digits(integer = 10, fraction = 2, message = "订单金额最多两位小数")
    @Schema(title = "订单金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "199.90")
    private BigDecimal amount;

    @NotNull(message = "订单状态不能为空")
    @Schema(
            title = "订单状态",
            requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"CREATED", "PAID", "CANCELLED"})
    private OrderStatus status;

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
}
