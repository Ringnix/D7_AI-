package com.cyberway.mp.demo.backenddemo.controller;

import com.cyberway.mp.bc.common.api.Page;
import com.cyberway.mp.bc.common.controller.ResponseResult;
import com.cyberway.mp.demo.backenddemo.api.order.OrderApi;
import com.cyberway.mp.demo.backenddemo.api.order.dto.OrderCreateDto;
import com.cyberway.mp.demo.backenddemo.api.order.dto.OrderModifyDto;
import com.cyberway.mp.demo.backenddemo.api.order.dto.OrderPageDto;
import com.cyberway.mp.demo.backenddemo.api.order.vo.OrderListVo;
import com.cyberway.mp.demo.backenddemo.api.order.vo.OrderVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import java.util.function.Supplier;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/orders")
@Tag(name = "订单管理", description = "订单创建、详情、分页、修改和删除")
public class OrderController {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    private final OrderApi orderApi;

    public OrderController(OrderApi orderApi) {
        this.orderApi = orderApi;
    }

    @PostMapping
    @Operation(summary = "创建订单")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "创建成功"),
            @ApiResponse(responseCode = "400", description = "参数校验失败"),
            @ApiResponse(responseCode = "409", description = "订单号冲突")
    })
    public ResponseResult<Long> createOrder(
            @RequestHeader(value = REQUEST_ID_HEADER, required = false) String requestId,
            @Valid @RequestBody OrderCreateDto dto) {
        return execute(requestId, () -> ResponseResult.successResult(orderApi.createOrder(dto)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询订单详情")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "404", description = "订单不存在")
    })
    public ResponseResult<OrderVo> getOrder(
            @RequestHeader(value = REQUEST_ID_HEADER, required = false) String requestId,
            @Min(value = 1, message = "订单ID必须大于等于1") @PathVariable("id") long id) {
        return execute(requestId, () -> ResponseResult.successResult(orderApi.getOrder(id)));
    }

    @GetMapping
    @Operation(summary = "分页查询订单")
    public ResponseResult<Page<OrderListVo>> queryOrders(
            @RequestHeader(value = REQUEST_ID_HEADER, required = false) String requestId,
            @Valid @ModelAttribute OrderPageDto dto) {
        return execute(requestId, () -> ResponseResult.successResult(orderApi.queryOrders(dto)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "完整修改订单")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "修改成功"),
            @ApiResponse(responseCode = "400", description = "参数校验失败"),
            @ApiResponse(responseCode = "404", description = "订单不存在"),
            @ApiResponse(responseCode = "409", description = "订单号冲突")
    })
    public ResponseResult<Void> modifyOrder(
            @RequestHeader(value = REQUEST_ID_HEADER, required = false) String requestId,
            @Min(value = 1, message = "订单ID必须大于等于1") @PathVariable("id") long id,
            @Valid @RequestBody OrderModifyDto dto) {
        return execute(requestId, () -> {
            orderApi.modifyOrder(id, dto);
            return ResponseResult.successResult();
        });
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除订单", description = "采用物理删除；重复删除返回订单不存在")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "404", description = "订单不存在")
    })
    public ResponseResult<Void> deleteOrder(
            @RequestHeader(value = REQUEST_ID_HEADER, required = false) String requestId,
            @Min(value = 1, message = "订单ID必须大于等于1") @PathVariable("id") long id) {
        return execute(requestId, () -> {
            orderApi.deleteOrder(id);
            return ResponseResult.successResult();
        });
    }

    private <T> T execute(String requestId, Supplier<T> action) {
        String currentRequestId = StringUtils.hasText(requestId)
                ? requestId
                : UUID.randomUUID().toString();
        MDC.put("requestId", currentRequestId);
        try {
            return action.get();
        } finally {
            MDC.remove("requestId");
        }
    }
}
