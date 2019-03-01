package com.imooc.order.service;

import com.imooc.order.dto.OrderDTO;

public interface OrderService {
    /**
     * 创建订单
     * @param orderDTO
     * @return
     */
    OrderDTO create(OrderDTO orderDTO);

    /**
     * 获取订单数量
     * @param productId
     * @return
     */
    String queryOrderNum(String productId);

    String del(String productId);
}
