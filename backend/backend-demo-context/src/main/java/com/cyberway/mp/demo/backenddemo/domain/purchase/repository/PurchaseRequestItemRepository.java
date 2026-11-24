package com.cyberway.mp.demo.backenddemo.domain.purchase.repository;

import com.cyberway.mp.bc.dal.GenericMapper;
import com.cyberway.mp.bc.dal.annotation.IgnoreMethod;
import com.cyberway.mp.demo.backenddemo.domain.purchase.entity.PurchaseRequestItem;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseRequestItemRepository extends GenericMapper<PurchaseRequestItem, Long> {

    @IgnoreMethod
    List<PurchaseRequestItem> findByRequestId(@Param("requestId") long requestId);

    @IgnoreMethod
    int deleteByRequestId(@Param("requestId") long requestId);
}
