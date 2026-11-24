package com.cyberway.mp.demo.backenddemo.api.purchase.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import java.util.List;

@Schema(description = "采购申请单详情")
public class PurchaseRequestVo extends PurchaseRequestListVo {

    @Schema(title = "备注")
    private String remark;

    @Schema(title = "取消原因")
    private String cancelReason;

    @Schema(title = "取消时间")
    private Date cancelledAt;

    @Schema(title = "审批人")
    private String approvalUserName;

    @Schema(title = "审批时间")
    private Date approvalTime;

    @Schema(title = "审批意见")
    private String approvalOpinion;

    @Schema(title = "最后修改时间")
    private Date updatedAt;

    @Schema(title = "采购明细")
    private List<PurchaseRequestItemVo> items;

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public Date getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Date cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getApprovalUserName() {
        return approvalUserName;
    }

    public void setApprovalUserName(String approvalUserName) {
        this.approvalUserName = approvalUserName;
    }

    public Date getApprovalTime() {
        return approvalTime;
    }

    public void setApprovalTime(Date approvalTime) {
        this.approvalTime = approvalTime;
    }

    public String getApprovalOpinion() {
        return approvalOpinion;
    }

    public void setApprovalOpinion(String approvalOpinion) {
        this.approvalOpinion = approvalOpinion;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<PurchaseRequestItemVo> getItems() {
        return items;
    }

    public void setItems(List<PurchaseRequestItemVo> items) {
        this.items = items;
    }
}
