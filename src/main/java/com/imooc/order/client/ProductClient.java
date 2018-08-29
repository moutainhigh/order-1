package com.imooc.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "product")
@Component
public interface ProductClient {

    @GetMapping("/msg")
    String productMsg();
}
