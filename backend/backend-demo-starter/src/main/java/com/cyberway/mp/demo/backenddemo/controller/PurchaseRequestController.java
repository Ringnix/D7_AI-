package com.cyberway.mp.demo.backenddemo.controller;

import com.cyberway.mp.bc.common.api.Page;
import com.cyberway.mp.bc.common.controller.ResponseResult;
import com.cyberway.mp.demo.backenddemo.api.purchase.PurchaseRequestApi;
import com.cyberway.mp.demo.backenddemo.api.purchase.dto.PurchaseRequestApproveDto;
import com.cyberway.mp.demo.backenddemo.api.purchase.dto.PurchaseRequestCreateDto;
import com.cyberway.mp.demo.backenddemo.api.purchase.dto.PurchaseRequestModifyDto;
import com.cyberway.mp.demo.backenddemo.api.purchase.dto.PurchaseRequestPageDto;
import com.cyberway.mp.demo.backenddemo.api.purchase.dto.PurchaseRequestReasonDto;
import com.cyberway.mp.demo.backenddemo.api.purchase.dto.PurchaseRequestVersionDto;
import com.cyberway.mp.demo.backenddemo.api.purchase.vo.PurchaseRequestListVo;
import com.cyberway.mp.demo.backenddemo.api.purchase.vo.PurchaseRequestVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import java.util.function.Supplier;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/purchase-requests")
@Tag(name = "采购申请单", description = "采购申请单主子表、状态流转与乐观锁控制")
public class PurchaseRequestController {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    private final PurchaseRequestApi purchaseRequestApi;

    public PurchaseRequestController(PurchaseRequestApi purchaseRequestApi) {
        this.purchaseRequestApi = purchaseRequestApi;
    }

    @PostMapping
    @Operation(summary = "新增采购申请单（保存草稿）")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "400", description = "参数校验失败")
    })
    public ResponseResult<Long> createPurchaseRequest(
            @RequestHeader(value = REQUEST_ID_HEADER, required = false) String requestId,
            @Valid @RequestBody PurchaseRequestCreateDto dto) {
        return execute(requestId, () -> ResponseResult.successResult(
                purchaseRequestApi.createPurchaseRequest(dto)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询采购申请单详情（含明细）")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "404", description = "采购申请单不存在")
    })
    public ResponseResult<PurchaseRequestVo> getPurchaseRequest(
            @RequestHeader(value = REQUEST_ID_HEADER, required = false) String requestId,
            @Min(value = 1, message = "采购申请单ID必须大于等于1") @PathVariable("id") long id) {
        return execute(requestId, () -> ResponseResult.successResult(
                purchaseRequestApi.getPurchaseRequest(id)));
    }

    @GetMapping
    @Operation(summary = "分页查询采购申请单")
    public ResponseResult<Page<PurchaseRequestListVo>> queryPurchaseRequests(
            @RequestHeader(value = REQUEST_ID_HEADER, required = false) String requestId,
            @Valid @ModelAttribute PurchaseRequestPageDto dto) {
        return execute(requestId, () -> ResponseResult.successResult(
                purchaseRequestApi.queryPurchaseRequests(dto)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改采购申请单（仅草稿）")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "修改成功"),
            @ApiResponse(responseCode = "400", description = "参数校验失败"),
            @ApiResponse(responseCode = "404", description = "采购申请单不存在"),
            @ApiResponse(responseCode = "409", description = "状态不允许或版本冲突")
    })
    public ResponseResult<Void> modifyPurchaseRequest(
            @RequestHeader(value = REQUEST_ID_HEADER, required = false) String requestId,
            @Min(value = 1, message = "采购申请单ID必须大于等于1") @PathVariable("id") long id,
            @Valid @RequestBody PurchaseRequestModifyDto dto) {
        return execute(requestId, () -> {
            purchaseRequestApi.modifyPurchaseRequest(id, dto);
            return ResponseResult.successResult();
        });
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除采购申请单（仅草稿，物理删除）")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "404", description = "采购申请单不存在"),
            @ApiResponse(responseCode = "409", description = "当前状态不允许删除")
    })
    public ResponseResult<Void> deletePurchaseRequest(
            @RequestHeader(value = REQUEST_ID_HEADER, required = false) String requestId,
            @Min(value = 1, message = "采购申请单ID必须大于等于1") @PathVariable("id") long id) {
        return execute(requestId, () -> {
            purchaseRequestApi.deletePurchaseRequest(id);
            return ResponseResult.successResult();
        });
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "提交采购申请单")
    public ResponseResult<Void> submitPurchaseRequest(
            @RequestHeader(value = REQUEST_ID_HEADER, required = false) String requestId,
            @Min(value = 1, message = "采购申请单ID必须大于等于1") @PathVariable("id") long id,
            @Valid @RequestBody PurchaseRequestVersionDto dto) {
        return execute(requestId, () -> {
            purchaseRequestApi.submitPurchaseRequest(id, dto);
            return ResponseResult.successResult();
        });
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "取消采购申请单（取消原因必填）")
    public ResponseResult<Void> cancelPurchaseRequest(
            @RequestHeader(value = REQUEST_ID_HEADER, required = false) String requestId,
            @Min(value = 1, message = "采购申请单ID必须大于等于1") @PathVariable("id") long id,
            @Valid @RequestBody PurchaseRequestReasonDto dto) {
        return execute(requestId, () -> {
            purchaseRequestApi.cancelPurchaseRequest(id, dto);
            return ResponseResult.successResult();
        });
    }

    @PostMapping("/{id}/withdraw")
    @Operation(summary = "撤回采购申请单（待审批 -> 草稿）")
    public ResponseResult<Void> withdrawPurchaseRequest(
            @RequestHeader(value = REQUEST_ID_HEADER, required = false) String requestId,
            @Min(value = 1, message = "采购申请单ID必须大于等于1") @PathVariable("id") long id,
            @Valid @RequestBody PurchaseRequestVersionDto dto) {
        return execute(requestId, () -> {
            purchaseRequestApi.withdrawPurchaseRequest(id, dto);
            return ResponseResult.successResult();
        });
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "审批通过采购申请单")
    public ResponseResult<Void> approvePurchaseRequest(
            @RequestHeader(value = REQUEST_ID_HEADER, required = false) String requestId,
            @Min(value = 1, message = "采购申请单ID必须大于等于1") @PathVariable("id") long id,
            @Valid @RequestBody PurchaseRequestApproveDto dto) {
        return execute(requestId, () -> {
            purchaseRequestApi.approvePurchaseRequest(id, dto);
            return ResponseResult.successResult();
        });
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "审批驳回采购申请单（驳回原因必填）")
    public ResponseResult<Void> rejectPurchaseRequest(
            @RequestHeader(value = REQUEST_ID_HEADER, required = false) String requestId,
            @Min(value = 1, message = "采购申请单ID必须大于等于1") @PathVariable("id") long id,
            @Valid @RequestBody PurchaseRequestReasonDto dto) {
        return execute(requestId, () -> {
            purchaseRequestApi.rejectPurchaseRequest(id, dto);
            return ResponseResult.successResult();
        });
    }

    private <T> T execute(String requestId, Supplier<T> action) {
        String currentRequestId = StringUtils.hasText(requestId)
                ? requestId
                : UUID.randomUUID().toString();
        MDC.put("requestId", currentRequestId);
        try {
            return action.get();
        } finally {
            MDC.remove("requestId");
        }
    }
}
