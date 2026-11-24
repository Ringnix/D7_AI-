package com.cyberway.mp.demo.backenddemo.domain.purchase.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cyberway.mp.bc.common.api.Page;
import com.cyberway.mp.bc.common.api.PageParam;
import com.cyberway.mp.bc.i18n.api.exception.BaseI18nException;
import com.cyberway.mp.demo.backenddemo.api.purchase.dto.PurchaseRequestApproveDto;
import com.cyberway.mp.demo.backenddemo.api.purchase.dto.PurchaseRequestCreateDto;
import com.cyberway.mp.demo.backenddemo.api.purchase.dto.PurchaseRequestItemDto;
import com.cyberway.mp.demo.backenddemo.api.purchase.dto.PurchaseRequestModifyDto;
import com.cyberway.mp.demo.backenddemo.api.purchase.dto.PurchaseRequestPageDto;
import com.cyberway.mp.demo.backenddemo.api.purchase.dto.PurchaseRequestReasonDto;
import com.cyberway.mp.demo.backenddemo.api.purchase.dto.PurchaseRequestVersionDto;
import com.cyberway.mp.demo.backenddemo.api.purchase.enums.PurchaseRequestStatus;
import com.cyberway.mp.demo.backenddemo.api.purchase.vo.PurchaseRequestListVo;
import com.cyberway.mp.demo.backenddemo.api.purchase.vo.PurchaseRequestVo;
import com.cyberway.mp.demo.backenddemo.domain.purchase.entity.PurchaseRequest;
import com.cyberway.mp.demo.backenddemo.domain.purchase.entity.PurchaseRequestItem;
import com.cyberway.mp.demo.backenddemo.domain.purchase.repository.PurchaseRequestItemRepository;
import com.cyberway.mp.demo.backenddemo.domain.purchase.repository.PurchaseRequestQuery;
import com.cyberway.mp.demo.backenddemo.domain.purchase.repository.PurchaseRequestRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

@ExtendWith(MockitoExtension.class)
class PurchaseRequestDomainServiceTest {

    @Mock
    private PurchaseRequestRepository purchaseRequestRepository;

    @Mock
    private PurchaseRequestItemRepository purchaseRequestItemRepository;

    @InjectMocks
    private PurchaseRequestDomainService purchaseRequestDomainService;

    @Test
    void createsDraftAndComputesTotalAmount() {
        PurchaseRequestCreateDto dto = createDto();
        doAnswer(invocation -> {
            PurchaseRequest request = invocation.getArgument(0);
            request.setId(100L);
            return null;
        }).when(purchaseRequestRepository).save(any(PurchaseRequest.class));

        long id = purchaseRequestDomainService.createPurchaseRequest(dto);

        assertEquals(100L, id);
        ArgumentCaptor<PurchaseRequest> requestCaptor =
                ArgumentCaptor.forClass(PurchaseRequest.class);
        verify(purchaseRequestRepository).save(requestCaptor.capture());
        PurchaseRequest saved = requestCaptor.getValue();
        assertEquals(PurchaseRequestStatus.DRAFT, saved.getStatus());
        assertEquals(0, saved.getVersion());
        assertTrue(saved.getRequestNo().startsWith("PR"));
        assertEquals(new BigDecimal("259.80"), saved.getTotalAmount());
        verify(purchaseRequestItemRepository, org.mockito.Mockito.times(2))
                .save(any(PurchaseRequestItem.class));
    }

    @Test
    void translatesRequestNoUniqueConflict() {
        doThrow(new DuplicateKeyException("duplicate"))
                .when(purchaseRequestRepository)
                .save(any(PurchaseRequest.class));

        assertThrows(
                BaseI18nException.class,
                () -> purchaseRequestDomainService.createPurchaseRequest(createDto()));
    }

    @Test
    void rejectsModifyWhenStatusIsNotDraft() {
        PurchaseRequest existing = request(PurchaseRequestStatus.SUBMITTED, 1);
        when(purchaseRequestRepository.findOne(1L)).thenReturn(existing);
        PurchaseRequestModifyDto dto = new PurchaseRequestModifyDto();
        dto.setTitle("修改标题");
        dto.setApplicantName("张三");
        dto.setDepartment("采购部");
        dto.setVersion(1);
        dto.setItems(List.of(itemDto()));

        assertThrows(
                BaseI18nException.class,
                () -> purchaseRequestDomainService.modifyPurchaseRequest(1L, dto));
        verify(purchaseRequestRepository, never())
                .updatePurchaseRequest(any(PurchaseRequest.class), anyInt());
    }

    @Test
    void rejectsModifyWhenVersionStale() {
        PurchaseRequest existing = request(PurchaseRequestStatus.DRAFT, 2);
        when(purchaseRequestRepository.findOne(1L)).thenReturn(existing);
        PurchaseRequestModifyDto dto = new PurchaseRequestModifyDto();
        dto.setTitle("修改标题");
        dto.setApplicantName("张三");
        dto.setDepartment("采购部");
        dto.setVersion(1);
        dto.setItems(List.of(itemDto()));

        assertThrows(
                BaseI18nException.class,
                () -> purchaseRequestDomainService.modifyPurchaseRequest(1L, dto));
    }

    @Test
    void submitsDraftWithItems() {
        PurchaseRequest existing = request(PurchaseRequestStatus.DRAFT, 0);
        when(purchaseRequestRepository.findOne(1L)).thenReturn(existing);
        when(purchaseRequestItemRepository.findByRequestId(1L))
                .thenReturn(List.of(new PurchaseRequestItem()));
        when(purchaseRequestRepository.updatePurchaseRequest(existing, 0)).thenReturn(1);

        purchaseRequestDomainService.submitPurchaseRequest(1L, version(0));

        assertEquals(PurchaseRequestStatus.SUBMITTED, existing.getStatus());
        verify(purchaseRequestRepository).updatePurchaseRequest(existing, 0);
    }

    @Test
    void rejectsSubmitWhenItemListIsEmpty() {
        PurchaseRequest existing = request(PurchaseRequestStatus.DRAFT, 0);
        when(purchaseRequestRepository.findOne(1L)).thenReturn(existing);
        when(purchaseRequestItemRepository.findByRequestId(1L)).thenReturn(List.of());

        assertThrows(
                BaseI18nException.class,
                () -> purchaseRequestDomainService.submitPurchaseRequest(1L, version(0)));
    }

    @Test
    void rejectsSubmitWhenStatusIsNotDraft() {
        when(purchaseRequestRepository.findOne(1L))
                .thenReturn(request(PurchaseRequestStatus.SUBMITTED, 1));

        assertThrows(
                BaseI18nException.class,
                () -> purchaseRequestDomainService.submitPurchaseRequest(1L, version(1)));
    }

    @Test
    void requiresCancelReason() {
        when(purchaseRequestRepository.findOne(1L))
                .thenReturn(request(PurchaseRequestStatus.DRAFT, 0));
        PurchaseRequestReasonDto dto = new PurchaseRequestReasonDto();
        dto.setVersion(0);
        dto.setReason("  ");

        assertThrows(
                BaseI18nException.class,
                () -> purchaseRequestDomainService.cancelPurchaseRequest(1L, dto));
    }

    @Test
    void cancelsSubmittedRequestWithReason() {
        PurchaseRequest existing = request(PurchaseRequestStatus.SUBMITTED, 1);
        when(purchaseRequestRepository.findOne(1L)).thenReturn(existing);
        when(purchaseRequestRepository.updatePurchaseRequest(existing, 1)).thenReturn(1);
        PurchaseRequestReasonDto dto = new PurchaseRequestReasonDto();
        dto.setVersion(1);
        dto.setReason("需求取消");

        purchaseRequestDomainService.cancelPurchaseRequest(1L, dto);

        assertEquals(PurchaseRequestStatus.CANCELLED, existing.getStatus());
        assertEquals("需求取消", existing.getCancelReason());
        org.junit.jupiter.api.Assertions.assertNotNull(existing.getCancelledTime());
    }

    @Test
    void rejectsCancelWhenStatusIsApproved() {
        when(purchaseRequestRepository.findOne(1L))
                .thenReturn(request(PurchaseRequestStatus.APPROVED, 2));
        PurchaseRequestReasonDto dto = new PurchaseRequestReasonDto();
        dto.setVersion(2);
        dto.setReason("取消");

        assertThrows(
                BaseI18nException.class,
                () -> purchaseRequestDomainService.cancelPurchaseRequest(1L, dto));
    }

    @Test
    void withdrawsSubmittedRequestToDraft() {
        PurchaseRequest existing = request(PurchaseRequestStatus.SUBMITTED, 1);
        when(purchaseRequestRepository.findOne(1L)).thenReturn(existing);
        when(purchaseRequestRepository.updatePurchaseRequest(existing, 1)).thenReturn(1);

        purchaseRequestDomainService.withdrawPurchaseRequest(1L, version(1));

        assertEquals(PurchaseRequestStatus.DRAFT, existing.getStatus());
    }

    @Test
    void approvesSubmittedRequestAndRecordsApprover() {
        PurchaseRequest existing = request(PurchaseRequestStatus.SUBMITTED, 1);
        when(purchaseRequestRepository.findOne(1L)).thenReturn(existing);
        when(purchaseRequestRepository.updatePurchaseRequest(existing, 1)).thenReturn(1);
        PurchaseRequestApproveDto dto = new PurchaseRequestApproveDto();
        dto.setVersion(1);
        dto.setApproverName("李经理");
        dto.setOpinion("同意");

        purchaseRequestDomainService.approvePurchaseRequest(1L, dto);

        assertEquals(PurchaseRequestStatus.APPROVED, existing.getStatus());
        assertEquals("李经理", existing.getApprovalUserName());
        assertEquals("同意", existing.getApprovalOpinion());
    }

    @Test
    void requiresRejectReason() {
        when(purchaseRequestRepository.findOne(1L))
                .thenReturn(request(PurchaseRequestStatus.SUBMITTED, 1));
        PurchaseRequestReasonDto dto = new PurchaseRequestReasonDto();
        dto.setVersion(1);
        dto.setReason("");

        assertThrows(
                BaseI18nException.class,
                () -> purchaseRequestDomainService.rejectPurchaseRequest(1L, dto));
    }

    @Test
    void detectsOptimisticLockConflictWhenUpdateAffectsNoRow() {
        PurchaseRequest existing = request(PurchaseRequestStatus.DRAFT, 0);
        when(purchaseRequestRepository.findOne(1L)).thenReturn(existing);
        when(purchaseRequestItemRepository.findByRequestId(1L))
                .thenReturn(List.of(new PurchaseRequestItem()));
        when(purchaseRequestRepository.updatePurchaseRequest(existing, 0)).thenReturn(0);

        assertThrows(
                BaseI18nException.class,
                () -> purchaseRequestDomainService.submitPurchaseRequest(1L, version(0)));
    }

    @Test
    void deletesDraftWithItsItems() {
        PurchaseRequest existing = request(PurchaseRequestStatus.DRAFT, 0);
        when(purchaseRequestRepository.findOne(1L)).thenReturn(existing);

        purchaseRequestDomainService.deletePurchaseRequest(1L);

        verify(purchaseRequestItemRepository).deleteByRequestId(1L);
        verify(purchaseRequestRepository).delete(1L);
    }

    @Test
    void rejectsDeleteForSubmittedRequest() {
        when(purchaseRequestRepository.findOne(1L))
                .thenReturn(request(PurchaseRequestStatus.SUBMITTED, 1));

        assertThrows(
                BaseI18nException.class,
                () -> purchaseRequestDomainService.deletePurchaseRequest(1L));
        verify(purchaseRequestRepository, never()).delete(anyLong());
    }

    @Test
    void returnsEmptyPageWithoutSearching() {
        PurchaseRequestPageDto dto = new PurchaseRequestPageDto();
        dto.setCurrent(1);
        dto.setPageSize(20);
        when(purchaseRequestRepository.countPurchaseRequest(any(PurchaseRequestQuery.class)))
                .thenReturn(0L);

        Page<PurchaseRequestListVo> page =
                purchaseRequestDomainService.pageQueryPurchaseRequests(dto);

        assertEquals(0L, page.getTotal());
        assertTrue(page.getDatas().isEmpty());
        verify(purchaseRequestRepository, never())
                .searchPurchaseRequest(any(), any(PageParam.class));
    }

    @Test
    void returnsPageWithFiltersAndPagination() {
        PurchaseRequestPageDto dto = new PurchaseRequestPageDto();
        dto.setCurrent(2);
        dto.setPageSize(10);
        dto.setStatus(PurchaseRequestStatus.SUBMITTED);
        PurchaseRequest request = request(PurchaseRequestStatus.SUBMITTED, 1);
        when(purchaseRequestRepository.countPurchaseRequest(any(PurchaseRequestQuery.class)))
                .thenReturn(1L);
        when(purchaseRequestRepository.searchPurchaseRequest(
                        any(PurchaseRequestQuery.class), any(PageParam.class)))
                .thenReturn(List.of(request));

        Page<PurchaseRequestListVo> page =
                purchaseRequestDomainService.pageQueryPurchaseRequests(dto);

        assertEquals(1L, page.getTotal());
        assertEquals(1, page.getDatas().size());
        assertFalse(page.getDatas().isEmpty());
    }

    @Test
    void returnsDetailWithItems() {
        PurchaseRequest existing = request(PurchaseRequestStatus.DRAFT, 0);
        existing.setId(1L);
        when(purchaseRequestRepository.findOne(1L)).thenReturn(existing);
        PurchaseRequestItem item = new PurchaseRequestItem();
        item.setId(11L);
        item.setItemName("笔记本电脑");
        item.setQuantity(new BigDecimal("2.00"));
        item.setUnitPrice(new BigDecimal("5000.00"));
        item.setAmount(new BigDecimal("10000.00"));
        when(purchaseRequestItemRepository.findByRequestId(1L)).thenReturn(List.of(item));

        PurchaseRequestVo vo = purchaseRequestDomainService.getPurchaseRequest(1L);

        assertEquals(1L, vo.getId());
        assertEquals(1, vo.getItems().size());
        assertEquals("笔记本电脑", vo.getItems().get(0).getItemName());
    }

    private PurchaseRequestCreateDto createDto() {
        PurchaseRequestCreateDto dto = new PurchaseRequestCreateDto();
        dto.setTitle("办公用品采购");
        dto.setApplicantName("张三");
        dto.setDepartment("研发部");
        dto.setRemark("季度采购");
        dto.setItems(List.of(itemDto(), itemDtoTwo()));
        return dto;
    }

    private PurchaseRequestItemDto itemDto() {
        PurchaseRequestItemDto dto = new PurchaseRequestItemDto();
        dto.setItemName("显示器");
        dto.setSpec("27寸");
        dto.setUnit("台");
        dto.setQuantity(new BigDecimal("2"));
        dto.setUnitPrice(new BigDecimal("99.90"));
        return dto;
    }

    private PurchaseRequestItemDto itemDtoTwo() {
        PurchaseRequestItemDto dto = new PurchaseRequestItemDto();
        dto.setItemName("键盘");
        dto.setUnit("个");
        dto.setQuantity(new BigDecimal("1"));
        dto.setUnitPrice(new BigDecimal("60.00"));
        return dto;
    }

    private PurchaseRequest request(PurchaseRequestStatus status, int version) {
        PurchaseRequest request = new PurchaseRequest();
        request.setRequestNo("PR202611240001");
        request.setTitle("办公用品采购");
        request.setApplicantName("张三");
        request.setDepartment("研发部");
        request.setStatus(status);
        request.setVersion(version);
        request.setTotalAmount(new BigDecimal("259.80"));
        return request;
    }

    private PurchaseRequestVersionDto version(int version) {
        PurchaseRequestVersionDto dto = new PurchaseRequestVersionDto();
        dto.setVersion(version);
        return dto;
    }
}
