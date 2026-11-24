package com.cyberway.mp.demo.backenddemo.api.purchase.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

@Schema(description = "采购申请单操作版本参数")
public class PurchaseRequestVersionDto {

    @NotNull(message = "版本号不能为空")
    @Min(value = 0, message = "版本号不能小于0")
    @Schema(title = "版本号（乐观锁）", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer version;

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
