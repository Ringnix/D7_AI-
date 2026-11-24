package com.cyberway.mp.demo.backenddemo.api.order.dto;

import com.cyberway.mp.bc.common.api.OrderPageParam;
import com.cyberway.mp.demo.backenddemo.api.order.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "订单分页查询参数")
public class OrderPageDto extends OrderPageParam {

    public OrderPageDto() {
        setCurrent(1);
        setPageSize(20);
    }

    @Size(max = 32, message = "订单号查询条件长度不能超过32")
    @Schema(title = "订单号，模糊查询")
    private String orderNoLike;

    @Positive(message = "客户ID必须大于0")
    @Schema(title = "客户ID")
    private Long customerId;

    @Schema(title = "订单状态", allowableValues = {"CREATED", "PAID", "CANCELLED"})
    private OrderStatus status;

    @Min(value = 1, message = "当前页必须大于等于1")
    @Override
    public int getCurrent() {
        return super.getCurrent();
    }

    @Min(value = 1, message = "每页条数必须大于等于1")
    @Max(value = 100, message = "每页条数不能超过100")
    @Override
    public int getPageSize() {
        return super.getPageSize();
    }

    @AssertTrue(message = "不支持自定义排序字段")
    @Schema(hidden = true)
    public boolean isOrderOptionValid() {
        String orderField = getOrderField();
        String orderDirection = getOrderDirection();
        List<OrderPageParam> orderList = getOrderList();
        return (orderField == null || orderField.isBlank())
                && (orderDirection == null || orderDirection.isBlank())
                && (orderList == null || orderList.isEmpty());
    }

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
}
