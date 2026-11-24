package com.cyberway.mp.demo.backenddemo.domain.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cyberway.mp.bc.common.api.Page;
import com.cyberway.mp.bc.common.api.PageParam;
import com.cyberway.mp.bc.i18n.api.exception.BaseI18nException;
import com.cyberway.mp.demo.backenddemo.api.order.dto.OrderPageDto;
import com.cyberway.mp.demo.backenddemo.api.order.enums.OrderStatus;
import com.cyberway.mp.demo.backenddemo.api.order.vo.OrderListVo;
import com.cyberway.mp.demo.backenddemo.domain.order.entity.Order;
import com.cyberway.mp.demo.backenddemo.domain.order.repository.OrderQuery;
import com.cyberway.mp.demo.backenddemo.domain.order.repository.OrderRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

@ExtendWith(MockitoExtension.class)
class OrderDomainServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderDomainService orderDomainService;

    @Test
    void createsValidOrderAndSavesOnce() {
        Order order = order("ORD-001", 1L, "10.25", OrderStatus.CREATED);
        when(orderRepository.countByOrderNo("ORD-001")).thenReturn(0L);

        orderDomainService.createOrder(order);

        verify(orderRepository).save(order);
    }

    @Test
    void rejectsDuplicateOrderNumberBeforeSaving() {
        Order order = order("ORD-002", 1L, "10.25", OrderStatus.CREATED);
        when(orderRepository.countByOrderNo("ORD-002")).thenReturn(1L);

        assertThrows(BaseI18nException.class, () -> orderDomainService.createOrder(order));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void translatesDatabaseUniqueConflict() {
        Order order = order("ORD-003", 1L, "10.25", OrderStatus.CREATED);
        when(orderRepository.countByOrderNo("ORD-003")).thenReturn(0L);
        doThrow(new DuplicateKeyException("duplicate"))
                .when(orderRepository)
                .save(order);

        assertThrows(BaseI18nException.class, () -> orderDomainService.createOrder(order));
    }

    @Test
    void reportsMissingOrder() {
        when(orderRepository.findOne(99L)).thenReturn(null);

        assertThrows(BaseI18nException.class, () -> orderDomainService.getOrder(99L));
    }

    @Test
    void deletesExistingOrderOnce() {
        Order order = order("ORD-004", 1L, "10.25", OrderStatus.CREATED);
        order.setId(4L);
        when(orderRepository.findOne(4L)).thenReturn(order);

        orderDomainService.deleteOrder(4L);

        verify(orderRepository).delete(4L);
    }

    @Test
    void returnsEmptyPageWithoutSearchingWhenNoRowsMatch() {
        OrderPageDto dto = page(2, 10);
        when(orderRepository.countOrder(any(OrderQuery.class))).thenReturn(0L);

        Page<OrderListVo> page = orderDomainService.pageQueryOrders(dto);

        assertEquals(2, page.getCurrent());
        assertEquals(0L, page.getTotal());
        assertTrue(page.getDatas().isEmpty());
        verify(orderRepository, never()).searchOrder(any(), any());
    }

    @Test
    void returnsStablePageMetadataAndTransfersFilters() {
        OrderPageDto dto = page(1, 20);
        dto.setOrderNoLike("2026");
        dto.setCustomerId(1001L);
        dto.setStatus(OrderStatus.PAID);

        Order first = order("ORD-005", 1001L, "11.20", OrderStatus.PAID);
        first.setId(5L);
        Order second = order("ORD-006", 1001L, "22.30", OrderStatus.PAID);
        second.setId(6L);
        when(orderRepository.countOrder(any(OrderQuery.class))).thenReturn(2L);
        when(orderRepository.searchOrder(any(OrderQuery.class), any(PageParam.class)))
                .thenReturn(List.of(first, second));

        Page<OrderListVo> page = orderDomainService.pageQueryOrders(dto);

        ArgumentCaptor<OrderQuery> queryCaptor = ArgumentCaptor.forClass(OrderQuery.class);
        ArgumentCaptor<PageParam> pageCaptor = ArgumentCaptor.forClass(PageParam.class);
        verify(orderRepository).countOrder(queryCaptor.capture());
        verify(orderRepository).searchOrder(queryCaptor.capture(), pageCaptor.capture());
        OrderQuery query = queryCaptor.getValue();
        assertEquals("2026", query.getOrderNoLike());
        assertEquals(1001L, query.getCustomerId());
        assertEquals(OrderStatus.PAID, query.getStatus());
        assertEquals(0, pageCaptor.getValue().getCurrent());
        assertEquals(20, pageCaptor.getValue().getPageSize());
        assertEquals(2L, page.getTotal());
        assertEquals(List.of(5L, 6L), page.getDatas().stream().map(OrderListVo::getId).toList());
    }

    private Order order(String orderNo, Long customerId, String amount, OrderStatus status) {
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setCustomerId(customerId);
        order.setAmount(new BigDecimal(amount));
        order.setStatus(status);
        return order;
    }

    private OrderPageDto page(int current, int pageSize) {
        OrderPageDto dto = new OrderPageDto();
        dto.setCurrent(current);
        dto.setPageSize(pageSize);
        return dto;
    }
}
