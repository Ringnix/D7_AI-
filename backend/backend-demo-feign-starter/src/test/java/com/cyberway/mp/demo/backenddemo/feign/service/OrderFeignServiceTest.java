package com.cyberway.mp.demo.backenddemo.feign.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cyberway.mp.bc.common.api.exception.BaseException;
import com.cyberway.mp.bc.common.controller.ResponseResult;
import com.cyberway.mp.demo.backenddemo.api.order.vo.OrderVo;
import com.cyberway.mp.demo.backenddemo.feign.client.OrderClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderFeignServiceTest {

    @Mock
    private OrderClient orderClient;

    @InjectMocks
    private OrderFeignService orderFeignService;

    @Test
    void returnsOrderDataFromRemoteService() {
        OrderVo vo = new OrderVo();
        vo.setId(1L);
        when(orderClient.getOrder(1L))
                .thenReturn(ResponseResult.successResult(vo));

        assertEquals(vo, orderFeignService.getOrder(1L));
        verify(orderClient).getOrder(1L);
    }

    @Test
    void translatesRemoteBusinessFailure() {
        when(orderClient.deleteOrder(9L))
                .thenReturn(ResponseResult.failResult("90000102", "订单不存在", null));

        assertThrows(BaseException.class, () -> orderFeignService.deleteOrder(9L));
    }
}
