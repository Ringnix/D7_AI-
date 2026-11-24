package com.cyberway.mp.demo.backenddemo.api.purchase;

import com.cyberway.mp.bc.common.api.Page;
import com.cyberway.mp.demo.backenddemo.api.purchase.dto.PurchaseRequestApproveDto;
import com.cyberway.mp.demo.backenddemo.api.purchase.dto.PurchaseRequestCreateDto;
import com.cyberway.mp.demo.backenddemo.api.purchase.dto.PurchaseRequestModifyDto;
import com.cyberway.mp.demo.backenddemo.api.purchase.dto.PurchaseRequestPageDto;
import com.cyberway.mp.demo.backenddemo.api.purchase.dto.PurchaseRequestReasonDto;
import com.cyberway.mp.demo.backenddemo.api.purchase.dto.PurchaseRequestVersionDto;
import com.cyberway.mp.demo.backenddemo.api.purchase.vo.PurchaseRequestListVo;
import com.cyberway.mp.demo.backenddemo.api.purchase.vo.PurchaseRequestVo;
import jakarta.validation.Valid;

/**
 * 采购申请单接口定义。
 *
 * <p>状态流转：草稿 -> 提交 -> 待审批 -> 审批通过/审批驳回；草稿或待审批状态可取消，
 * 待审批状态可撤回为草稿。审批只记录状态与审批信息，不接入中台审批流。</p>
 */
public interface PurchaseRequestApi {

    long createPurchaseRequest(@Valid PurchaseRequestCreateDto dto);

    PurchaseRequestVo getPurchaseRequest(long id);

    Page<PurchaseRequestListVo> queryPurchaseRequests(@Valid PurchaseRequestPageDto dto);

    void modifyPurchaseRequest(long id, @Valid PurchaseRequestModifyDto dto);

    void deletePurchaseRequest(long id);

    void submitPurchaseRequest(long id, @Valid PurchaseRequestVersionDto dto);

    void cancelPurchaseRequest(long id, @Valid PurchaseRequestReasonDto dto);

    void withdrawPurchaseRequest(long id, @Valid PurchaseRequestVersionDto dto);

    void approvePurchaseRequest(long id, @Valid PurchaseRequestApproveDto dto);

    void rejectPurchaseRequest(long id, @Valid PurchaseRequestReasonDto dto);
}
