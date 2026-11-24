package com.cyberway.mp.demo.backenddemo.feign.service;

import com.cyberway.mp.bc.common.api.Page;
import com.cyberway.mp.bc.common.api.exception.BaseException;
import com.cyberway.mp.bc.common.controller.ResponseResult;
import com.cyberway.mp.demo.backenddemo.api.order.OrderApi;
import com.cyberway.mp.demo.backenddemo.api.order.dto.OrderCreateDto;
import com.cyberway.mp.demo.backenddemo.api.order.dto.OrderModifyDto;
import com.cyberway.mp.demo.backenddemo.api.order.dto.OrderPageDto;
import com.cyberway.mp.demo.backenddemo.api.order.vo.OrderListVo;
import com.cyberway.mp.demo.backenddemo.api.order.vo.OrderVo;
import com.cyberway.mp.demo.backenddemo.feign.client.OrderClient;

public class OrderFeignService implements OrderApi {

    private final OrderClient orderClient;

    public OrderFeignService(OrderClient orderClient) {
        this.orderClient = orderClient;
    }

    @Override
    public long createOrder(OrderCreateDto dto) {
        return data(orderClient.createOrder(dto));
    }

    @Override
    public OrderVo getOrder(long id) {
        return data(orderClient.getOrder(id));
    }

    @Override
    public Page<OrderListVo> queryOrders(OrderPageDto dto) {
        return data(orderClient.queryOrders(dto));
    }

    @Override
    public void modifyOrder(long id, OrderModifyDto dto) {
        data(orderClient.modifyOrder(id, dto));
    }

    @Override
    public void deleteOrder(long id) {
        data(orderClient.deleteOrder(id));
    }

    private <T> T data(ResponseResult<T> result) {
        if (result == null) {
            throw new BaseException("92000001", "订单服务未返回结果");
        }
        if (!result.isSuccess()) {
            throw new BaseException(result.getErrorCode(), result.getMsg());
        }
        return result.getData();
    }
}
