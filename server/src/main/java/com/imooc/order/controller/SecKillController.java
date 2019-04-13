package com.imooc.order.controller;

import com.imooc.order.service.SecKillService;
import com.netflix.hystrix.contrib.javanica.annotation.DefaultProperties;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 秒杀商品
 * @Author: zoomz_lin
 */
@RestController
@RequestMapping("/skill")
@Slf4j
@DefaultProperties(defaultFallback = "defaultFallback")
public class SecKillController {

    @Autowired
    private SecKillService secKillService;

    @GetMapping("/query/{productId}")
    public String query(@PathVariable("productId") String productId) throws Exception {
        int stockNum = secKillService.queryStockNum(productId);
        String info = "";
        if (0 == stockNum) {
            info = "秒杀结束\n";
        }
        info += secKillService.querySecKillProductInfo(productId);
        return info;
    }

    @GetMapping("/order/{productId}")
    @HystrixCommand
    public String order(@PathVariable("productId") String productId) throws Exception {
        log.info("@skill request, productId:" + productId);
        String info = secKillService.orderProductMockDiffUser(productId);
        if (!StringUtils.isEmpty(info)) {
            return info;
        }
        return secKillService.querySecKillProductInfo(productId);
    }

    private String defaultFallback(){
        return "系统默认提示：系统异常，请稍后再试~~~";
    }

}
