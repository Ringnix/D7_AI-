package com.cyberway.mp.demo.backenddemo.domain.purchase.entity;

import com.cyberway.mp.bc.dal.annotation.Table;
import com.cyberway.mp.bc.dal.model.Entity;
import com.cyberway.mp.demo.backenddemo.api.purchase.dto.PurchaseRequestItemDto;
import com.cyberway.mp.demo.backenddemo.api.purchase.vo.PurchaseRequestItemVo;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 采购申请单子表实体（申请明细）。
 */
@Table(name = "backend_demo_purchase_request_item")
public class PurchaseRequestItem extends Entity<Long> {

    private Long requestId;

    private String itemName;

    private String spec;

    private String unit;

    private BigDecimal quantity;

    private BigDecimal unitPrice;

    private BigDecimal amount;

    private Integer sortNo;

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName == null ? null : itemName.trim();
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
        this.quantity = quantity == null ? null : quantity.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice == null ? null : unitPrice.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount == null ? null : amount.setScale(2, RoundingMode.HALF_UP);
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }

    /**
     * 由提交参数构建明细实体，金额 = 数量 × 单价。
     * @param dto 明细提交参数
     * @param requestId 主表ID
     * @param sortNo 排序号
     */
    public static PurchaseRequestItem fromDto(
            PurchaseRequestItemDto dto, Long requestId, int sortNo) {
        PurchaseRequestItem item = new PurchaseRequestItem();
        item.setRequestId(requestId);
        item.setItemName(dto.getItemName());
        item.setSpec(dto.getSpec());
        item.setUnit(dto.getUnit());
        item.setQuantity(dto.getQuantity());
        item.setUnitPrice(dto.getUnitPrice());
        item.setAmount(dto.getQuantity().multiply(dto.getUnitPrice())
                .setScale(2, RoundingMode.HALF_UP));
        item.setSortNo(dto.getSortNo() == null ? sortNo : dto.getSortNo());
        return item;
    }

    /**
     * 转为明细展示对象。
     */
    public PurchaseRequestItemVo toVo() {
        PurchaseRequestItemVo vo = new PurchaseRequestItemVo();
        vo.setId(getId());
        vo.setItemName(getItemName());
        vo.setSpec(getSpec());
        vo.setUnit(getUnit());
        vo.setQuantity(getQuantity());
        vo.setUnitPrice(getUnitPrice());
        vo.setAmount(getAmount());
        vo.setSortNo(getSortNo());
        return vo;
    }
}
