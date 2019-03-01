package com.imooc.order.message;

import com.fasterxml.jackson.core.type.TypeReference;
import com.imooc.order.dataobject.OrderDetail;
import com.imooc.order.dto.OrderDTO;
import com.imooc.order.service.OrderService;
import com.imooc.order.utils.JsonUtil;
import com.imooc.order.utils.KeyUtil;
import com.imooc.product.client.ProductClient;
import com.imooc.product.common.ProductInfoOutput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class PorductInfoReceiver {

    private static final String PRODUCT_STOCK_TEMPLATE = "product_stock_%s";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private OrderService orderService;
    @Autowired
    private ProductClient productClient;

    @RabbitListener(queuesToDeclare = @Queue("productInfo"))
    public void process(String message){
        List<ProductInfoOutput> productInfoOutputList = (List<ProductInfoOutput>)JsonUtil.fromJson(message,
                new TypeReference<List<ProductInfoOutput>>() {});
        log.info("从队列【{}】接收到消息：{}","productInfo",productInfoOutputList);

        // 存储到redis中
        for(ProductInfoOutput productInfoOutput : productInfoOutputList){
            stringRedisTemplate.opsForValue().set(String.format(PRODUCT_STOCK_TEMPLATE, productInfoOutput.getProductId()),
                    "" + productInfoOutput.getProductStock());
        }

        // 模拟订单入库
        OrderDTO orderDTO = morkOrderDTO(productInfoOutputList);

        OrderDTO result = orderService.create(orderDTO);
        if (!StringUtils.isEmpty(result.getOrderId())) {
            System.out.println("下单成功，订单编号--->" + result.getOrderId());
        }
    }

    private OrderDTO morkOrderDTO(List<ProductInfoOutput> productInfoOutputList) {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setBuyerName(UUID.randomUUID().toString().toLowerCase().replaceAll("-",""));
        orderDTO.setBuyerPhone(UUID.randomUUID().toString().toLowerCase().replaceAll("-",""));
        orderDTO.setBuyerAddress("系统默认地址");
        orderDTO.setBuyerOpenid(UUID.randomUUID().toString().toLowerCase().replaceAll("-",""));
        List<ProductInfoOutput> productInfoList = productClient.listForOrder(Arrays.asList("123456"));
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
