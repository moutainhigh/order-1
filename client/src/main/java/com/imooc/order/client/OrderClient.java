package com.imooc.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: zoomz_lin
 */
@FeignClient(name = "order",fallback = OrderClient.OrderClientFallback.class)
@Component
public interface OrderClient {

    @PostMapping("/order/queryOrderNum")
    String queryOrderNum(@RequestBody String productId);

    @Component
    class OrderClientFallback implements OrderClient {

        @Override
        public String queryOrderNum(String productId) {
            return null;
        }
    }

}
