package com.imooc.order.controller;

import com.imooc.product.client.ProductClient;
import com.netflix.hystrix.contrib.javanica.annotation.DefaultProperties;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@RestController
@DefaultProperties(defaultFallback = "defaultFallback")
public class HystrixController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ProductClient productClient;

    //fallback是一个方法名
    //@HystrixCommand(fallbackMethod = "fallback")
    //超时设置（由于用的是feign，feign底层是ribbon的封装，ribbon默认超时时间也是1s，需要设置ribbon的超时时间才可以让hystrix超时设置生效）
//    @HystrixCommand(commandProperties = {
//            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "3000"),
//    })
    //设置熔断（CircuitBreaker：断路器）
    @HystrixCommand(commandProperties = {
            @HystrixProperty(name = "circuitBreaker.enabled", value = "true"),  //设置熔断
            @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
            @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "10000"),
            @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "60")
    })
    //@HystrixCommand
    @GetMapping("getProductInfoList")
    public String getProductInfoList(@RequestParam("number") Integer number){
        if (number % 2 == 0){
            return "success";
        }

//        方式一：RestTemplate原生url访问
//        return restTemplate.postForObject("http://127.0.0.1:8005/listForOrder",
//                Arrays.asList("1527165136839371483"),
//                String.class);

        //方式二：通过feign访问
        return productClient.listForOrder(Arrays.asList("1527165136839371483")).toString();
    }

    //fallback方法的返回值必须与HystrixCommand对应方法的返回值一致
    private String fallback(){
        return "太拥挤了，请稍后再试~~~";
    }

    private String defaultFallback(){
        return "系统默认提示：系统异常，请稍后再试~~~";
    }
}
