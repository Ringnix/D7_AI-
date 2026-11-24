package com.cyberway.mp.demo.backenddemo.api.purchase.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "采购申请单明细项")
public class PurchaseRequestItemVo {

    @Schema(title = "明细ID")
    private Long id;

    @Schema(title = "物品名称")
    private String itemName;

    @Schema(title = "规格型号")
    private String spec;

    @Schema(title = "单位")
    private String unit;

    @Schema(title = "数量")
    private BigDecimal quantity;

    @Schema(title = "单价")
    private BigDecimal unitPrice;

    @Schema(title = "金额")
    private BigDecimal amount;

    @Schema(title = "排序号")
    private Integer sortNo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }
}
