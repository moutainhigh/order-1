package com.imooc.order.service;

/**
 * @Author: zoomz_lin
 */
public interface SecKillService {

    /**
     * 查询秒杀活动特价商品的信息
     * @param productId
     * @return
     */
    String querySecKillProductInfo(String productId);

    /**
     * 模拟不同用户秒杀同一商品的请求
     * @param productId
     */
    String orderProductMockDiffUser(String productId);

    /**
     * 查询库存数量
     * @param productId
     * @return
     */
    int queryStockNum(String productId);
}
