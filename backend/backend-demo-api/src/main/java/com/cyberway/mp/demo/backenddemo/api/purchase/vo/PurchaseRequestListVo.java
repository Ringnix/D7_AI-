package com.cyberway.mp.demo.backenddemo.api.purchase.vo;

import com.cyberway.mp.demo.backenddemo.api.purchase.enums.PurchaseRequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.Date;

@Schema(description = "采购申请单列表项")
public class PurchaseRequestListVo {

    @Schema(title = "申请单ID")
    private Long id;

    @Schema(title = "申请单号")
    private String requestNo;

    @Schema(title = "标题")
    private String title;

    @Schema(title = "申请人")
    private String applicantName;

    @Schema(title = "申请部门")
    private String department;

    @Schema(title = "申请总金额")
    private BigDecimal totalAmount;

    @Schema(title = "申请单状态")
    private PurchaseRequestStatus status;

    @Schema(title = "版本号（乐观锁）")
    private Integer version;

    @Schema(title = "创建时间")
    private Date createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
        this.title = title;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public PurchaseRequestStatus getStatus() {
        return status;
    }

    public void setStatus(PurchaseRequestStatus status) {
        this.status = status;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
