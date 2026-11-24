package com.cyberway.mp.demo.backenddemo.feign.client;

import com.cyberway.mp.bc.common.api.Page;
import com.cyberway.mp.bc.common.controller.ResponseResult;
import com.cyberway.mp.demo.backenddemo.api.order.dto.OrderCreateDto;
import com.cyberway.mp.demo.backenddemo.api.order.dto.OrderModifyDto;
import com.cyberway.mp.demo.backenddemo.api.order.dto.OrderPageDto;
import com.cyberway.mp.demo.backenddemo.api.order.vo.OrderListVo;
import com.cyberway.mp.demo.backenddemo.api.order.vo.OrderVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        contextId = "OrderClient",
        value = "${backend-demo.feign.server-name:backend-demo-server}",
        path = "/orders")
public interface OrderClient {

    @PostMapping
    ResponseResult<Long> createOrder(@RequestBody OrderCreateDto dto);

    @GetMapping("/{id}")
    ResponseResult<OrderVo> getOrder(@PathVariable("id") long id);

    @GetMapping
    ResponseResult<Page<OrderListVo>> queryOrders(@SpringQueryMap OrderPageDto dto);

    @PutMapping("/{id}")
    ResponseResult<Void> modifyOrder(@PathVariable("id") long id, @RequestBody OrderModifyDto dto);

    @DeleteMapping("/{id}")
    ResponseResult<Void> deleteOrder(@PathVariable("id") long id);
}
