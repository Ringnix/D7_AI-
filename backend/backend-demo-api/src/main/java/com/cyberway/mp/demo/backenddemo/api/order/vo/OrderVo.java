package com.cyberway.mp.demo.backenddemo.api.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;

@Schema(description = "订单详情")
public class OrderVo extends OrderListVo {

    @Schema(title = "最后修改时间")
    private Date updatedAt;

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
