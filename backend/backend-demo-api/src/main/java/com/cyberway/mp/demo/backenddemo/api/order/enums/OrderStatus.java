package com.cyberway.mp.demo.backenddemo.api.order.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "订单状态")
public enum OrderStatus {

    @Schema(description = "已创建")
    CREATED,

    @Schema(description = "已支付")
    PAID,

    @Schema(description = "已取消")
    CANCELLED
}
