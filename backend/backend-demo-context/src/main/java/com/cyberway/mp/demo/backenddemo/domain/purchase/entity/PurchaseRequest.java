package com.cyberway.mp.demo.backenddemo.domain.purchase.entity;

import com.cyberway.mp.bc.dal.annotation.Table;
import com.cyberway.mp.bc.dal.model.BusinessEntityWithAppInstance;
import com.cyberway.mp.demo.backenddemo.api.purchase.enums.PurchaseRequestStatus;
import com.cyberway.mp.demo.backenddemo.api.purchase.vo.PurchaseRequestListVo;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

/**
 * 采购申请单主表实体。
 */
@Table(name = "backend_demo_purchase_request")
public class PurchaseRequest extends BusinessEntityWithAppInstance {

    private String requestNo;

    private String title;

    private String applicantName;

    private String department;

    private BigDecimal totalAmount;

    private PurchaseRequestStatus status;

    private String remark;

    /** 乐观锁版本号，随每次成功更新加 1。 */
    private Integer version;

    private String cancelReason;

    private Date cancelledTime;

    private Long approvalUserId;

    private String approvalUserName;

    private Date approvalTime;

    private String approvalOpinion;

    public String getRequestNo() {
        return requestNo;
    }

    public void setRequestNo(String requestNo) {
        this.requestNo = requestNo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? null : title.trim();
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName == null ? null : applicantName.trim();
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department == null ? null : department.trim();
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount == null
                ? null
                : totalAmount.setScale(2, RoundingMode.HALF_UP);
    }

    public PurchaseRequestStatus getStatus() {
        return status;
    }

    public void setStatus(PurchaseRequestStatus status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public Date getCancelledTime() {
        return cancelledTime;
    }

    public void setCancelledTime(Date cancelledTime) {
        this.cancelledTime = cancelledTime;
    }

    public Long getApprovalUserId() {
        return approvalUserId;
    }

    public void setApprovalUserId(Long approvalUserId) {
        this.approvalUserId = approvalUserId;
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

    /**
     * 转为列表展示对象。
     */
    public PurchaseRequestListVo toListVo() {
        PurchaseRequestListVo vo = new PurchaseRequestListVo();
        vo.setId(getId());
        vo.setRequestNo(getRequestNo());
        vo.setTitle(getTitle());
        vo.setApplicantName(getApplicantName());
        vo.setDepartment(getDepartment());
        vo.setTotalAmount(getTotalAmount());
        vo.setStatus(getStatus());
        vo.setVersion(getVersion());
        vo.setCreatedAt(getCreatedTime());
        return vo;
    }
}
