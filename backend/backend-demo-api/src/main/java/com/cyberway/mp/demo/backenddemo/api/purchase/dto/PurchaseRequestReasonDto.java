package com.cyberway.mp.demo.backenddemo.api.purchase.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "采购申请单带原因操作参数")
public class PurchaseRequestReasonDto extends PurchaseRequestVersionDto {

    @Size(max = 500, message = "原因长度不能超过500")
    @Schema(title = "取消/驳回原因", maxLength = 500)
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
