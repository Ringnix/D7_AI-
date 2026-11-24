package com.cyberway.mp.demo.backenddemo.consumer.controller;

import com.cyberway.mp.bc.common.api.Page;
import com.cyberway.mp.bc.common.controller.ResponseResult;
import com.cyberway.mp.demo.backenddemo.api.order.OrderApi;
import com.cyberway.mp.demo.backenddemo.api.order.dto.OrderPageDto;
import com.cyberway.mp.demo.backenddemo.api.order.vo.OrderListVo;
import com.cyberway.mp.demo.backenddemo.api.order.vo.OrderVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/consumer/orders")
@Tag(name = "订单服务调用", description = "通过 Nacos 服务发现和 Feign 调用订单服务")
public class OrderConsumerController {

    private final OrderApi orderApi;

    public OrderConsumerController(OrderApi orderApi) {
        this.orderApi = orderApi;
    }

    @GetMapping("/{id}")
    @Operation(summary = "通过 Feign 查询订单详情")
    public ResponseResult<OrderVo> getOrder(
            @Min(value = 1, message = "订单ID必须大于等于1") @PathVariable("id") long id) {
        return ResponseResult.successResult(orderApi.getOrder(id));
    }

    @GetMapping
    @Operation(summary = "通过 Feign 分页查询订单")
    public ResponseResult<Page<OrderListVo>> queryOrders(@Valid @ModelAttribute OrderPageDto dto) {
        return ResponseResult.successResult(orderApi.queryOrders(dto));
    }
}
