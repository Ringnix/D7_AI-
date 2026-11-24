package com.cyberway.mp.demo.backenddemo.api.order;

import com.cyberway.mp.bc.common.api.Page;
import com.cyberway.mp.demo.backenddemo.api.order.dto.OrderCreateDto;
import com.cyberway.mp.demo.backenddemo.api.order.dto.OrderModifyDto;
import com.cyberway.mp.demo.backenddemo.api.order.dto.OrderPageDto;
import com.cyberway.mp.demo.backenddemo.api.order.vo.OrderListVo;
import com.cyberway.mp.demo.backenddemo.api.order.vo.OrderVo;
import jakarta.validation.Valid;

public interface OrderApi {

    long createOrder(@Valid OrderCreateDto dto);

    OrderVo getOrder(long id);

    Page<OrderListVo> queryOrders(@Valid OrderPageDto dto);

    void modifyOrder(long id, @Valid OrderModifyDto dto);

    void deleteOrder(long id);
}
