package com.cyberway.mp.demo.backenddemo.domain.demo.repository.query;

import com.cyberway.mp.bc.dal.annotation.Alias;
import com.cyberway.mp.bc.dal.annotation.Predicate;
import com.cyberway.mp.bc.dal.annotation.query.Contains;
import com.cyberway.mp.bc.dal.annotation.query.OrderByField;
import com.cyberway.mp.bc.dal.model.OrderFragment;
import com.cyberway.mp.demo.backenddemo.api.demo.dto.DemoPageDto;
import com.cyberway.mp.demo.backenddemo.api.demo.enums.DemoStatus;

@Predicate(dynamic = true)
public class DemoQuery {

    private static final OrderFragment DEFAULT_ORDER_FRAGMENT = new OrderFragment("created_time", "DESC");

    /**
     * 编码模糊搜索
     */
    @Contains
    @Alias(name = "code")
    private String codeLike;

    /**
     * 名称模糊搜索
     */
    @Contains
    @Alias(name = "name")
    private String nameLike;

    /**
     * 状态
     */
    private DemoStatus status;

    /**
     * 排序条件
     */
    @OrderByField
    private OrderFragment orderFragment;

    public String getCodeLike() {
        return codeLike;
    }

    public void setCodeLike(String codeLike) {
        this.codeLike = codeLike;
    }

    public String getNameLike() {
        return nameLike;
    }

    public void setNameLike(String nameLike) {
        this.nameLike = nameLike;
    }

    public DemoStatus getStatus() {
        return status;
    }

    public void setStatus(DemoStatus status) {
        this.status = status;
    }

    public OrderFragment getOrderFragment() {
        return orderFragment;
    }

    public void setOrderFragment(OrderFragment orderFragment) {
        this.orderFragment = orderFragment;
    }

    public static DemoQuery fromDemoPageDto(DemoPageDto dto) {
        DemoQuery query = new DemoQuery();
        query.setCodeLike(dto.getCodeLike());
        query.setNameLike(dto.getNameLike());
        query.setStatus(dto.getStatus());
        final OrderFragment paramOrderFragment = OrderFragment.fromOrderPageParam(dto);
        if (paramOrderFragment == null) {
            query.setOrderFragment(DEFAULT_ORDER_FRAGMENT);
        } else {
            query.setOrderFragment(paramOrderFragment);
        }
        return query;
    }
}
