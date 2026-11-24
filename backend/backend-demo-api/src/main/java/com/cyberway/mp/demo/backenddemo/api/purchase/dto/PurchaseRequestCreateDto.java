package com.cyberway.mp.demo.backenddemo.api.purchase.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "新增采购申请单参数")
public class PurchaseRequestCreateDto {

    @NotBlank(message = "申请单标题不能为空")
    @Size(max = 100, message = "申请单标题长度不能超过100")
    @Schema(title = "申请单标题", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 100)
    private String title;

    @NotBlank(message = "申请人不能为空")
    @Size(max = 100, message = "申请人长度不能超过100")
    @Schema(title = "申请人", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 100)
    private String applicantName;

    @NotBlank(message = "申请部门不能为空")
    @Size(max = 100, message = "申请部门长度不能超过100")
    @Schema(title = "申请部门", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 100)
    private String department;

    @Size(max = 500, message = "备注长度不能超过500")
    @Schema(title = "备注", maxLength = 500)
    private String remark;

    @NotEmpty(message = "采购明细不能为空")
    @Size(max = 100, message = "采购明细不能超过100条")
    @Valid
    @Schema(title = "采购明细", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<PurchaseRequestItemDto> items;

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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public List<PurchaseRequestItemDto> getItems() {
        return items;
    }

    public void setItems(List<PurchaseRequestItemDto> items) {
        this.items = items;
    }
}
