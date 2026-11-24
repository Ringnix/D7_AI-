package com.cyberway.mp.demo.backenddemo.api.purchase.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "采购申请单审批参数")
public class PurchaseRequestApproveDto extends PurchaseRequestVersionDto {

    @Size(max = 100, message = "审批人长度不能超过100")
    @Schema(title = "审批人", maxLength = 100)
    private String approverName;

    @Size(max = 500, message = "审批意见长度不能超过500")
    @Schema(title = "审批意见", maxLength = 500)
    private String opinion;

    public String getApproverName() {
        return approverName;
    }

    public void setApproverName(String approverName) {
        this.approverName = approverName;
    }

    public String getOpinion() {
        return opinion;
    }

    public void setOpinion(String opinion) {
        this.opinion = opinion;
    }
}
