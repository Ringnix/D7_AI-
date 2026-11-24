package com.cyberway.mp.demo.backenddemo.api.purchase.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "修改采购申请单参数")
public class PurchaseRequestModifyDto extends PurchaseRequestCreateDto {

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
