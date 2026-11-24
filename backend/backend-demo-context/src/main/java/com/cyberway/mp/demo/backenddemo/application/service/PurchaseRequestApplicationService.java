package com.cyberway.mp.demo.backenddemo.application.service;

import com.cyberway.mp.bc.common.api.Page;
import com.cyberway.mp.demo.backenddemo.api.purchase.PurchaseRequestApi;
import com.cyberway.mp.demo.backenddemo.api.purchase.dto.PurchaseRequestApproveDto;
import com.cyberway.mp.demo.backenddemo.api.purchase.dto.PurchaseRequestCreateDto;
import com.cyberway.mp.demo.backenddemo.api.purchase.dto.PurchaseRequestModifyDto;
import com.cyberway.mp.demo.backenddemo.api.purchase.dto.PurchaseRequestPageDto;
import com.cyberway.mp.demo.backenddemo.api.purchase.dto.PurchaseRequestReasonDto;
import com.cyberway.mp.demo.backenddemo.api.purchase.dto.PurchaseRequestVersionDto;
import com.cyberway.mp.demo.backenddemo.api.purchase.vo.PurchaseRequestListVo;
import com.cyberway.mp.demo.backenddemo.api.purchase.vo.PurchaseRequestVo;
import com.cyberway.mp.demo.backenddemo.domain.purchase.service.PurchaseRequestDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.validation.annotation.Validated;

/**
 * 采购申请单应用服务：负责参数接收、调用领域服务并记录关键操作日志。
 */
@Validated
public class PurchaseRequestApplicationService implements PurchaseRequestApi {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(PurchaseRequestApplicationService.class);

    private final PurchaseRequestDomainService purchaseRequestDomainService;

    public PurchaseRequestApplicationService(
            PurchaseRequestDomainService purchaseRequestDomainService) {
        this.purchaseRequestDomainService = purchaseRequestDomainService;
    }

    @Override
    public long createPurchaseRequest(PurchaseRequestCreateDto dto) {
        long startedAt = System.nanoTime();
        try {
            long id = purchaseRequestDomainService.createPurchaseRequest(dto);
            logSuccess("create", id, startedAt);
            return id;
        } catch (RuntimeException exception) {
            logFailure("create", null, startedAt, exception);
            throw exception;
        }
    }

    @Override
    public PurchaseRequestVo getPurchaseRequest(long id) {
        return purchaseRequestDomainService.getPurchaseRequest(id);
    }

    @Override
    public Page<PurchaseRequestListVo> queryPurchaseRequests(PurchaseRequestPageDto dto) {
        return purchaseRequestDomainService.pageQueryPurchaseRequests(dto);
    }

    @Override
    public void modifyPurchaseRequest(long id, PurchaseRequestModifyDto dto) {
        execute("modify", id, () -> purchaseRequestDomainService.modifyPurchaseRequest(id, dto));
    }

    @Override
    public void deletePurchaseRequest(long id) {
        execute("delete", id, () -> purchaseRequestDomainService.deletePurchaseRequest(id));
    }

    @Override
    public void submitPurchaseRequest(long id, PurchaseRequestVersionDto dto) {
        execute("submit", id, () -> purchaseRequestDomainService.submitPurchaseRequest(id, dto));
    }

    @Override
    public void cancelPurchaseRequest(long id, PurchaseRequestReasonDto dto) {
        execute("cancel", id, () -> purchaseRequestDomainService.cancelPurchaseRequest(id, dto));
    }

    @Override
    public void withdrawPurchaseRequest(long id, PurchaseRequestVersionDto dto) {
        execute("withdraw", id, () -> purchaseRequestDomainService.withdrawPurchaseRequest(id, dto));
    }

    @Override
    public void approvePurchaseRequest(long id, PurchaseRequestApproveDto dto) {
        execute("approve", id, () -> purchaseRequestDomainService.approvePurchaseRequest(id, dto));
    }

    @Override
    public void rejectPurchaseRequest(long id, PurchaseRequestReasonDto dto) {
        execute("reject", id, () -> purchaseRequestDomainService.rejectPurchaseRequest(id, dto));
    }

    private void execute(String operation, long id, Runnable action) {
        long startedAt = System.nanoTime();
        try {
            action.run();
            logSuccess(operation, id, startedAt);
        } catch (RuntimeException exception) {
            logFailure(operation, id, startedAt, exception);
            throw exception;
        }
    }

    private void logSuccess(String operation, Long purchaseRequestId, long startedAt) {
        LOGGER.info(
                "采购申请单操作 requestId={} operation={} purchaseRequestId={} result=success elapsedMs={}",
                requestId(),
                operation,
                purchaseRequestId,
                elapsedMillis(startedAt));
    }

    private void logFailure(
            String operation, Long purchaseRequestId, long startedAt, RuntimeException exception) {
        LOGGER.warn(
                "采购申请单操作 requestId={} operation={} purchaseRequestId={} result=failure error={} elapsedMs={}",
                requestId(),
                operation,
                purchaseRequestId,
                exception.getClass().getSimpleName(),
                elapsedMillis(startedAt));
    }

    private String requestId() {
        String requestId = MDC.get("requestId");
        if (requestId != null && !requestId.isBlank()) {
            return requestId;
        }
        String traceId = MDC.get("traceId");
        return traceId == null || traceId.isBlank() ? "N/A" : traceId;
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000L;
    }
}
