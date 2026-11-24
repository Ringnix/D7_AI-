package com.cyberway.mp.demo.backenddemo.api.purchase.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "采购申请单状态")
public enum PurchaseRequestStatus {

    @Schema(description = "草稿")
    DRAFT,

    @Schema(description = "已提交（待审批）")
    SUBMITTED,

    @Schema(description = "审批通过")
    APPROVED,

    @Schema(description = "审批驳回")
    REJECTED,

    @Schema(description = "已取消")
    CANCELLED
}
