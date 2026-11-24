package com.cyberway.mp.demo.backenddemo.api.purchase.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Schema(description = "采购申请单明细项")
public class PurchaseRequestItemDto {

    @NotBlank(message = "物品名称不能为空")
    @Size(max = 100, message = "物品名称长度不能超过100")
    @Schema(title = "物品名称", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 100)
    private String itemName;

    @Size(max = 100, message = "规格型号长度不能超过100")
    @Schema(title = "规格型号", maxLength = 100)
    private String spec;

    @Size(max = 20, message = "单位长度不能超过20")
    @Schema(title = "单位", maxLength = 20)
    private String unit;

    @NotNull(message = "数量不能为空")
    @DecimalMin(value = "0.01", message = "数量必须大于0")
    @Digits(integer = 10, fraction = 2, message = "数量最多两位小数")
    @Schema(title = "数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "2.00")
    private BigDecimal quantity;

    @NotNull(message = "单价不能为空")
    @DecimalMin(value = "0.01", message = "单价必须大于0")
    @Digits(integer = 10, fraction = 2, message = "单价最多两位小数")
    @Schema(title = "单价", requiredMode = Schema.RequiredMode.REQUIRED, example = "99.90")
    private BigDecimal unitPrice;

    @Schema(title = "排序号")
    private Integer sortNo;

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }
}
