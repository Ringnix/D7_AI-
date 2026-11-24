package com.cyberway.mp.demo.backenddemo.api.purchase.dto;

import com.cyberway.mp.bc.common.api.OrderPageParam;
import com.cyberway.mp.demo.backenddemo.api.purchase.enums.PurchaseRequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "采购申请单分页查询参数")
public class PurchaseRequestPageDto extends OrderPageParam {

    public PurchaseRequestPageDto() {
        setCurrent(1);
        setPageSize(20);
    }

    @Size(max = 32, message = "申请单号查询条件长度不能超过32")
    @Schema(title = "申请单号，模糊查询")
    private String requestNoLike;

    @Size(max = 100, message = "标题查询条件长度不能超过100")
    @Schema(title = "标题，模糊查询")
    private String titleLike;

    @Schema(title = "申请单状态", allowableValues = {"DRAFT", "SUBMITTED", "APPROVED", "REJECTED", "CANCELLED"})
    private PurchaseRequestStatus status;

    @Min(value = 1, message = "当前页必须大于等于1")
    @Override
    public int getCurrent() {
        return super.getCurrent();
    }

    @Min(value = 1, message = "每页条数必须大于等于1")
    @Max(value = 100, message = "每页条数不能超过100")
    @Override
    public int getPageSize() {
        return super.getPageSize();
    }

    @AssertTrue(message = "不支持自定义排序字段")
    @Schema(hidden = true)
    public boolean isOrderOptionValid() {
        String orderField = getOrderField();
        String orderDirection = getOrderDirection();
        List<OrderPageParam> orderList = getOrderList();
        return (orderField == null || orderField.isBlank())
                && (orderDirection == null || orderDirection.isBlank())
                && (orderList == null || orderList.isEmpty());
    }

    public String getRequestNoLike() {
        return requestNoLike;
    }

    public void setRequestNoLike(String requestNoLike) {
        this.requestNoLike = requestNoLike;
    }

    public String getTitleLike() {
        return titleLike;
    }

    public void setTitleLike(String titleLike) {
        this.titleLike = titleLike;
    }

    public PurchaseRequestStatus getStatus() {
        return status;
    }

    public void setStatus(PurchaseRequestStatus status) {
        this.status = status;
    }
}
