package com.cyberway.mp.demo.backenddemo.domain.purchase.service;

import com.cyberway.mp.bc.common.api.Page;
import com.cyberway.mp.bc.common.api.PageParam;
import com.cyberway.mp.bc.i18n.api.exception.BaseI18nException;
import com.cyberway.mp.demo.backenddemo.api.constant.BackendDemoErrorCode;
import com.cyberway.mp.demo.backenddemo.api.purchase.dto.PurchaseRequestApproveDto;
import com.cyberway.mp.demo.backenddemo.api.purchase.dto.PurchaseRequestCreateDto;
import com.cyberway.mp.demo.backenddemo.api.purchase.dto.PurchaseRequestItemDto;
import com.cyberway.mp.demo.backenddemo.api.purchase.dto.PurchaseRequestModifyDto;
import com.cyberway.mp.demo.backenddemo.api.purchase.dto.PurchaseRequestPageDto;
import com.cyberway.mp.demo.backenddemo.api.purchase.dto.PurchaseRequestReasonDto;
import com.cyberway.mp.demo.backenddemo.api.purchase.dto.PurchaseRequestVersionDto;
import com.cyberway.mp.demo.backenddemo.api.purchase.enums.PurchaseRequestStatus;
import com.cyberway.mp.demo.backenddemo.api.purchase.vo.PurchaseRequestItemVo;
import com.cyberway.mp.demo.backenddemo.api.purchase.vo.PurchaseRequestListVo;
import com.cyberway.mp.demo.backenddemo.api.purchase.vo.PurchaseRequestVo;
import com.cyberway.mp.demo.backenddemo.domain.purchase.entity.PurchaseRequest;
import com.cyberway.mp.demo.backenddemo.domain.purchase.entity.PurchaseRequestItem;
import com.cyberway.mp.demo.backenddemo.domain.purchase.repository.PurchaseRequestItemRepository;
import com.cyberway.mp.demo.backenddemo.domain.purchase.repository.PurchaseRequestQuery;
import com.cyberway.mp.demo.backenddemo.domain.purchase.repository.PurchaseRequestRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

/**
 * 采购申请单领域服务：负责主子表持久化、状态机、原因校验与乐观锁控制。
 */
public class PurchaseRequestDomainService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PurchaseRequestDomainService.class);

    private static final String DEFAULT_APPROVER_NAME = "审批人";

    private final PurchaseRequestRepository purchaseRequestRepository;

    private final PurchaseRequestItemRepository purchaseRequestItemRepository;

    public PurchaseRequestDomainService(
            PurchaseRequestRepository purchaseRequestRepository,
            PurchaseRequestItemRepository purchaseRequestItemRepository) {
        this.purchaseRequestRepository = purchaseRequestRepository;
        this.purchaseRequestItemRepository = purchaseRequestItemRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public long createPurchaseRequest(PurchaseRequestCreateDto dto) {
        PurchaseRequest request = new PurchaseRequest();
        request.setRequestNo(generateRequestNo());
        request.setTitle(dto.getTitle());
        request.setApplicantName(dto.getApplicantName());
        request.setDepartment(dto.getDepartment());
        request.setRemark(dto.getRemark());
        request.setStatus(PurchaseRequestStatus.DRAFT);
        request.setVersion(0);
        request.setTotalAmount(sumItems(dto.getItems()));
        try {
            purchaseRequestRepository.save(request);
        } catch (DuplicateKeyException exception) {
            throw new BaseI18nException(BackendDemoErrorCode.PURCHASE_REQUEST_NO_EXISTED, exception);
        }
        saveItems(request.getId(), dto.getItems());
        return request.getId();
    }

    public PurchaseRequestVo getPurchaseRequest(long id) {
        PurchaseRequest request = loadRequest(id);
        List<PurchaseRequestItem> items = purchaseRequestItemRepository.findByRequestId(id);
        return toVo(request, items);
    }

    public Page<PurchaseRequestListVo> pageQueryPurchaseRequests(PurchaseRequestPageDto dto) {
        PurchaseRequestQuery query = PurchaseRequestQuery.fromPageDto(dto);
        long total = purchaseRequestRepository.countPurchaseRequest(query);
        if (total < 1) {
            return new Page<>(dto.getCurrent(), dto.getPageSize(), 0, Collections.emptyList());
        }
        PageParam pageParam = new PageParam();
        pageParam.setCurrent(dto.getCurrent() - 1);
        pageParam.setPageSize(dto.getPageSize());
        List<PurchaseRequest> requests = purchaseRequestRepository.searchPurchaseRequest(query, pageParam);
        return new Page<>(
                dto.getCurrent(),
                dto.getPageSize(),
                total,
                requests.stream().map(PurchaseRequest::toListVo).collect(Collectors.toList()));
    }

    @Transactional(rollbackFor = Exception.class)
    public void modifyPurchaseRequest(long id, PurchaseRequestModifyDto dto) {
        PurchaseRequest request = loadRequest(id);
        ensureStatus(request, PurchaseRequestStatus.DRAFT);
        ensureVersion(request, dto.getVersion());
        request.setTitle(dto.getTitle());
        request.setApplicantName(dto.getApplicantName());
        request.setDepartment(dto.getDepartment());
        request.setRemark(dto.getRemark());
        request.setTotalAmount(sumItems(dto.getItems()));
        updateWithVersion(request, dto.getVersion());
        purchaseRequestItemRepository.deleteByRequestId(id);
        saveItems(id, dto.getItems());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deletePurchaseRequest(long id) {
        PurchaseRequest request = loadRequest(id);
        ensureStatus(request, PurchaseRequestStatus.DRAFT);
        purchaseRequestItemRepository.deleteByRequestId(id);
        purchaseRequestRepository.delete(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void submitPurchaseRequest(long id, PurchaseRequestVersionDto dto) {
        PurchaseRequest request = loadRequest(id);
        ensureStatus(request, PurchaseRequestStatus.DRAFT);
        ensureVersion(request, dto.getVersion());
        if (purchaseRequestItemRepository.findByRequestId(id).isEmpty()) {
            throw new BaseI18nException(
                    BackendDemoErrorCode.PURCHASE_REQUEST_STATUS_INVALID, "采购明细为空，不能提交");
        }
        request.setStatus(PurchaseRequestStatus.SUBMITTED);
        updateWithVersion(request, dto.getVersion());
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelPurchaseRequest(long id, PurchaseRequestReasonDto dto) {
        PurchaseRequest request = loadRequest(id);
        if (request.getStatus() != PurchaseRequestStatus.DRAFT
                && request.getStatus() != PurchaseRequestStatus.SUBMITTED) {
            throw new BaseI18nException(BackendDemoErrorCode.PURCHASE_REQUEST_STATUS_INVALID);
        }
        ensureVersion(request, dto.getVersion());
        String reason = trimToNull(dto.getReason());
        if (reason == null) {
            throw new BaseI18nException(BackendDemoErrorCode.PURCHASE_REQUEST_CANCEL_REASON_REQUIRED);
        }
        request.setStatus(PurchaseRequestStatus.CANCELLED);
        request.setCancelReason(reason);
        request.setCancelledTime(new Date());
        updateWithVersion(request, dto.getVersion());
    }

    @Transactional(rollbackFor = Exception.class)
    public void withdrawPurchaseRequest(long id, PurchaseRequestVersionDto dto) {
        PurchaseRequest request = loadRequest(id);
        ensureStatus(request, PurchaseRequestStatus.SUBMITTED);
        ensureVersion(request, dto.getVersion());
        request.setStatus(PurchaseRequestStatus.DRAFT);
        updateWithVersion(request, dto.getVersion());
    }

    @Transactional(rollbackFor = Exception.class)
    public void approvePurchaseRequest(long id, PurchaseRequestApproveDto dto) {
        PurchaseRequest request = loadRequest(id);
        ensureStatus(request, PurchaseRequestStatus.SUBMITTED);
        ensureVersion(request, dto.getVersion());
        request.setStatus(PurchaseRequestStatus.APPROVED);
        request.setApprovalUserId(0L);
        request.setApprovalUserName(
                trimToNull(dto.getApproverName()) == null
                        ? DEFAULT_APPROVER_NAME
                        : trimToNull(dto.getApproverName()));
        request.setApprovalTime(new Date());
        request.setApprovalOpinion(dto.getOpinion());
        updateWithVersion(request, dto.getVersion());
    }

    @Transactional(rollbackFor = Exception.class)
    public void rejectPurchaseRequest(long id, PurchaseRequestReasonDto dto) {
        PurchaseRequest request = loadRequest(id);
        ensureStatus(request, PurchaseRequestStatus.SUBMITTED);
        ensureVersion(request, dto.getVersion());
        String reason = trimToNull(dto.getReason());
        if (reason == null) {
            throw new BaseI18nException(BackendDemoErrorCode.PURCHASE_REQUEST_REJECT_REASON_REQUIRED);
        }
        request.setStatus(PurchaseRequestStatus.REJECTED);
        request.setApprovalUserId(0L);
        request.setApprovalUserName(DEFAULT_APPROVER_NAME);
        request.setApprovalTime(new Date());
        request.setApprovalOpinion(reason);
        updateWithVersion(request, dto.getVersion());
    }

    private PurchaseRequest loadRequest(long id) {
        PurchaseRequest request = purchaseRequestRepository.findOne(id);
        if (request == null) {
            throw new BaseI18nException(BackendDemoErrorCode.PURCHASE_REQUEST_ID_NOT_EXISTED);
        }
        return request;
    }

    private void ensureStatus(PurchaseRequest request, PurchaseRequestStatus expected) {
        if (request.getStatus() != expected) {
            throw new BaseI18nException(BackendDemoErrorCode.PURCHASE_REQUEST_STATUS_INVALID);
        }
    }

    private void ensureVersion(PurchaseRequest request, Integer version) {
        if (!Objects.equals(request.getVersion(), version)) {
            throw new BaseI18nException(BackendDemoErrorCode.PURCHASE_REQUEST_VERSION_CONFLICT);
        }
    }

    private void updateWithVersion(PurchaseRequest request, int version) {
        int affectedRows = purchaseRequestRepository.updatePurchaseRequest(request, version);
        if (affectedRows == 0) {
            throw new BaseI18nException(BackendDemoErrorCode.PURCHASE_REQUEST_VERSION_CONFLICT);
        }
    }

    private void saveItems(Long requestId, List<PurchaseRequestItemDto> items) {
        int sortNo = 0;
        for (PurchaseRequestItemDto itemDto : items) {
            PurchaseRequestItem item = PurchaseRequestItem.fromDto(itemDto, requestId, sortNo + 1);
            purchaseRequestItemRepository.save(item);
            sortNo += 1;
        }
    }

    private BigDecimal sumItems(List<PurchaseRequestItemDto> items) {
        return items.stream()
                .map(item -> item.getQuantity().multiply(item.getUnitPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private PurchaseRequestVo toVo(PurchaseRequest request, List<PurchaseRequestItem> items) {
        PurchaseRequestVo vo = new PurchaseRequestVo();
        vo.setId(request.getId());
        vo.setRequestNo(request.getRequestNo());
        vo.setTitle(request.getTitle());
        vo.setApplicantName(request.getApplicantName());
        vo.setDepartment(request.getDepartment());
        vo.setTotalAmount(request.getTotalAmount());
        vo.setStatus(request.getStatus());
        vo.setVersion(request.getVersion());
        vo.setRemark(request.getRemark());
        vo.setCancelReason(request.getCancelReason());
        vo.setCancelledAt(request.getCancelledTime());
        vo.setApprovalUserName(request.getApprovalUserName());
        vo.setApprovalTime(request.getApprovalTime());
        vo.setApprovalOpinion(request.getApprovalOpinion());
        vo.setCreatedAt(request.getCreatedTime());
        vo.setUpdatedAt(request.getLastModifiedTime());
        vo.setItems(items.stream().map(PurchaseRequestItem::toVo).collect(Collectors.toList()));
        return vo;
    }

    /**
     * 生成全局唯一的采购申请单号：PR + yyyyMMdd + 4 位序号 + 4 位随机字符。
     */
    private String generateRequestNo() {
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 4).toUpperCase();
        String sequence = String.format("%04d", (int) (Math.random() * 10000));
        return "PR" + date + sequence + random;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
