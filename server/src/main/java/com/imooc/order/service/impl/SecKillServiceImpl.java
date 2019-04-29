package com.imooc.order.service.impl;

import com.imooc.order.dataobject.OrderDetail;
import com.imooc.order.dto.OrderDTO;
import com.imooc.order.service.DistributedLock;
import com.imooc.order.service.OrderService;
import com.imooc.order.service.RedisLock;
import com.imooc.order.service.SecKillService;
import com.imooc.order.utils.KeyUtil;
import com.imooc.product.client.ProductClient;
import com.imooc.product.common.ProductInfoOutput;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @Author: zoomz_lin
 */
@Service
public class SecKillServiceImpl implements SecKillService {

    // 超时10s
    private static final long TIMEOUT = 10000L;

    @Autowired
    private RedisLock redisLock;
    @Autowired
    private OrderService orderService;
    @Autowired
    private ProductClient productClient;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private DistributedLock distributedLock;

    /**
     * 国庆活动，皮蛋粥特价，限量100000份
     */
    static Map<String, Integer> products;
    static Map<String, Integer> stock;
    static Map<String, String> orders;
    private static final String PRODUCT_ID = "123456";
    private static final Integer STOCK = 150;

    private static final String PRODUCT_STOCK_TEMPLATE = "product_stock_%s";
    private static final String PRODUCT_STOCKSUM_TEMPLATE = "product_stocksum_%s";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    static
    {
        /**
         * 模拟多个表，商品信息表，库存表，秒杀成功订单表
         */
        products = new HashMap<>();
        stock = new HashMap<>();
        orders = new HashMap<>();
        products.put(PRODUCT_ID, STOCK);
        stock.put(PRODUCT_ID, STOCK);
    }

    private String queryMap(String productId)
    {
        /*return "秒杀活动，皮蛋粥特价0.01，限量份"
                + products.get(productId)
                +" 还剩：" + queryStockNum(productId)+" 份"
                +" 该商品成功下单用户数目："
                +  queryOrderNum(productId) +" 人" ;*/
        return "秒杀活动，皮蛋粥特价0.01，限量份"
                + stringRedisTemplate.opsForValue().get(String.format(PRODUCT_STOCKSUM_TEMPLATE, productId))
                +" 还剩：" + queryStockNum(productId)+" 份"
                +" 该商品成功下单用户数目："
                +  queryOrderNum(productId) +" 人" ;
    }

    @Override
    public String querySecKillProductInfo(String productId) {
        return this.queryMap(productId);
    }

    @Override
    public String orderProductMockDiffUser(String productId) {
        // 1.查询该商品库存，为0则活动结束。
        String info = "";
        int stockNum = queryStockNum(productId);
        if (0 == stockNum) {
            info = "秒杀结束，耗时：";
            System.out.println(info);
            return info;
        }

        String rs = distributedLock.acquireLock(productId, 200, 700);
        if (null == rs) {
            info = "哎呦喂，人也太多了吧，换个姿势试试~";
            System.out.println(info);
            return info;
        } else {
            // 秒杀活动限购一份
            // 模拟订单入库
            ProductInfoOutput productInfoOutput = new ProductInfoOutput();
            productInfoOutput.setProductId(productId);
            OrderDTO orderDTO = morkOrderDTO(Arrays.asList(productInfoOutput));
            OrderDTO result = orderService.create(orderDTO);
            if (!StringUtils.isEmpty(result.getOrderId())) {
                System.out.println("下单成功，订单编号--->" + result.getOrderId());
            }
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        // 释放锁
        //distributedLock.releaseLock(productId, rs);
        distributedLock.releaseLockWithLua(productId, rs);
        return "";
    }

    /**
     * 从redis中查询库存
     * @param productId
     * @return
     */
    @Override
    public int queryStockNum(String productId) {
        String key = "product_stock_" + productId;
        String stockValue = redisTemplate.opsForValue().get(key);
        if (StringUtils.isEmpty(stockValue)) {
            ProductInfoOutput productInfoOutput = queryOneProductInfo(productId);
            if (null == productInfoOutput) {
                return 0;
            }
            return productInfoOutput.getProductStock();
        }
        return Integer.parseInt(stockValue);
    }

    /**
     * 查询单个商品信息
     * @param productId
     * @return
     */
    private ProductInfoOutput queryOneProductInfo(String productId) {
        List<ProductInfoOutput> productInfoOutputList = productClient.listForOrder(Arrays.asList(productId));
        if (!CollectionUtils.isEmpty(productInfoOutputList)) {
            return productInfoOutputList.get(0);
        }
        return null;
    }

    /**
     * 查看下单人数
     * @param productId
     * @return
     */
    private String queryOrderNum(String productId) {
        String orderNum = orderService.queryOrderNum(productId);
        if (StringUtils.isEmpty(orderNum)) {
            return "0";
        }
        int stockNum = queryStockNum(productId);
        Integer stockSum = Integer.parseInt(stringRedisTemplate.opsForValue().get(String.format(PRODUCT_STOCKSUM_TEMPLATE, productId)));
        Integer stock = Integer.parseInt(stringRedisTemplate.opsForValue().get(String.format(PRODUCT_STOCK_TEMPLATE, productId)));
        if (stockSum - stock != Integer.parseInt(orderNum)) {
            return "" + (stockSum - stock);
        }
        return orderNum;
    }

    /**
     * 模拟用户下单
     * @param productInfoOutputList
     * @return
     */
    private OrderDTO morkOrderDTO(List<ProductInfoOutput> productInfoOutputList) {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setBuyerName(UUID.randomUUID().toString().toLowerCase().replaceAll("-",""));
        orderDTO.setBuyerPhone(UUID.randomUUID().toString().toLowerCase().replaceAll("-",""));
        orderDTO.setBuyerAddress("系统默认地址");
        orderDTO.setBuyerOpenid(UUID.randomUUID().toString().toLowerCase().replaceAll("-",""));
        List<String> productIdList = productInfoOutputList.stream()
                .map(ProductInfoOutput :: getProductId)
                .collect(Collectors.toList());
        List<ProductInfoOutput> productInfoList = productClient.listForOrder(productIdList);
        OrderDetail orderDetail = new OrderDetail();
        if (!CollectionUtils.isEmpty(productInfoList)) {
            ProductInfoOutput productInfoOutput = productInfoOutputList.get(0);
            BeanUtils.copyProperties(productInfoOutput, orderDetail);
        }
        orderDetail.setDetailId(KeyUtil.getUniqueKey());
        // 秒杀活动限购一份
        orderDetail.setProductQuantity(1);
        // orderDetailList格式：//[{productId: "123457",productQuantity: 1},{productId: "123458",productQuantity: 2}]
        orderDTO.setOrderDetailList(Arrays.asList(orderDetail));
        return orderDTO;
    }

}
