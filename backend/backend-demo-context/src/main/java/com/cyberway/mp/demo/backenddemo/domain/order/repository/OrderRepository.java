package com.cyberway.mp.demo.backenddemo.domain.order.repository;

import com.cyberway.mp.bc.common.api.PageParam;
import com.cyberway.mp.bc.dal.GenericMapper;
import com.cyberway.mp.bc.dal.annotation.IgnoreMethod;
import com.cyberway.mp.demo.backenddemo.domain.order.entity.Order;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends GenericMapper<Order, Long> {

    long countByOrderNo(@Param("orderNo") String orderNo);

    @IgnoreMethod
    long countOrder(@Param("query") OrderQuery query);

    @IgnoreMethod
    List<Order> searchOrder(@Param("query") OrderQuery query, @Param("pageParam") PageParam pageParam);

    @IgnoreMethod
    long countByOrderNoExcludingId(
            @Param("orderNo") String orderNo,
            @Param("id") long id);
}
