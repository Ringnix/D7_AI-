package com.cyberway.mp.demo.backenddemo.domain.purchase.repository;

import com.cyberway.mp.bc.common.api.PageParam;
import com.cyberway.mp.bc.dal.GenericMapper;
import com.cyberway.mp.bc.dal.annotation.IgnoreMethod;
import com.cyberway.mp.demo.backenddemo.domain.purchase.entity.PurchaseRequest;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseRequestRepository extends GenericMapper<PurchaseRequest, Long> {

    @IgnoreMethod
    long countPurchaseRequest(@Param("query") PurchaseRequestQuery query);

    @IgnoreMethod
    List<PurchaseRequest> searchPurchaseRequest(
            @Param("query") PurchaseRequestQuery query,
            @Param("pageParam") PageParam pageParam);

    /**
     * 按版本号更新采购申请单，版本号不一致时返回 0 条，由上层抛出并发冲突。
     * @param request 待更新的申请单
     * @param version 前端提交的版本号
     * @return 实际更新行数
     */
    @IgnoreMethod
    int updatePurchaseRequest(@Param("request") PurchaseRequest request, @Param("version") int version);
}
