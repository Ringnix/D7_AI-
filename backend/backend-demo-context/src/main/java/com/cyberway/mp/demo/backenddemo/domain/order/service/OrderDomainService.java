package com.cyberway.mp.demo.backenddemo.domain.order.service;

import com.cyberway.mp.bc.common.api.Page;
import com.cyberway.mp.bc.common.api.PageParam;
import com.cyberway.mp.bc.i18n.api.exception.BaseI18nException;
import com.cyberway.mp.demo.backenddemo.api.constant.BackendDemoErrorCode;
import com.cyberway.mp.demo.backenddemo.api.order.dto.OrderPageDto;
import com.cyberway.mp.demo.backenddemo.api.order.vo.OrderListVo;
import com.cyberway.mp.demo.backenddemo.domain.order.entity.Order;
import com.cyberway.mp.demo.backenddemo.domain.order.repository.OrderQuery;
import com.cyberway.mp.demo.backenddemo.domain.order.repository.OrderRepository;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

public class OrderDomainService {

    private final OrderRepository orderRepository;

    public OrderDomainService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public void createOrder(Order order) {
        ensureOrderNoUnique(order.getOrderNo(), null);
        try {
            orderRepository.save(order);
        } catch (DuplicateKeyException exception) {
            throw new BaseI18nException(BackendDemoErrorCode.ORDER_NO_EXISTED, exception);
        }
    }

    public Order getOrder(long id) {
        Order order = orderRepository.findOne(id);
        if (order == null) {
            throw new BaseI18nException(BackendDemoErrorCode.ORDER_ID_NOT_EXISTED);
        }
        return order;
    }

    @Transactional(rollbackFor = Exception.class)
    public void modifyOrder(Order order) {
        ensureOrderNoUnique(order.getOrderNo(), order.getId());
        try {
            orderRepository.updateById(order, order.getId());
        } catch (DuplicateKeyException exception) {
            throw new BaseI18nException(BackendDemoErrorCode.ORDER_NO_EXISTED, exception);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteOrder(long id) {
        getOrder(id);
        orderRepository.delete(id);
    }

    public Page<OrderListVo> pageQueryOrders(OrderPageDto dto) {
        OrderQuery query = OrderQuery.fromPageDto(dto);
        long total = orderRepository.countOrder(query);
        if (total < 1) {
            return new Page<>(dto.getCurrent(), dto.getPageSize(), 0, Collections.emptyList());
        }

        PageParam pageParam = new PageParam();
        pageParam.setCurrent(dto.getCurrent() - 1);
        pageParam.setPageSize(dto.getPageSize());
        List<Order> orders = orderRepository.searchOrder(query, pageParam);
        return new Page<>(
                dto.getCurrent(),
                dto.getPageSize(),
                total,
                orders.stream().map(Order::toListVo).collect(Collectors.toList()));
    }

    private void ensureOrderNoUnique(String orderNo, Long currentOrderId) {
        long duplicateCount = currentOrderId == null
                ? orderRepository.countByOrderNo(orderNo)
                : orderRepository.countByOrderNoExcludingId(orderNo, currentOrderId);
        if (duplicateCount > 0) {
            throw new BaseI18nException(BackendDemoErrorCode.ORDER_NO_EXISTED);
        }
    }
}
