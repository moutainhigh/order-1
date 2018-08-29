package com.imooc.order.controller;

import com.imooc.order.client.ProductClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@Slf4j
public class ClientController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @Autowired
    private ProductClient productClient;

    @GetMapping("/getProductMsg")
    public String getProductMsg(){
        /**
         * RestTemplate的三种使用方式
         */
        //1、第一种方式（直接使用restTemplate，url写死）
//        RestTemplate restTemplate = new RestTemplate();
//        String response = restTemplate.getForObject("http://127.0.0.1:8080/msg", String.class);

        //2、第二种方式（利用loadBalancerClient通过应用名获取url，然后再使用restTemplate）
//        ServiceInstance serviceInstance = loadBalancerClient.choose("PRODUCT");
//        serviceInstance.getHost();
//        serviceInstance.getPort();
//        String url = String.format("http://%s:%s", serviceInstance.getHost(), serviceInstance.getPort()) + "/msg";
//        RestTemplate restTemplate = new RestTemplate();
//        String response = restTemplate.getForObject(url, String.class);

        //3、第三种方式（利用@LoadBalanced，可在restTemplate里使用应用名字）
//        String response = restTemplate.getForObject("http://PRODUCT/msg", String.class);


        /**
         * 引用Feign
         * 1、引入spring-cloud-starter-feign依赖
         * 2、在启动的主类上添加@EnableFeignClients
         * 2、定义接口，注入@FeignClient(name = "product")，"product"为服务的名称
         * 3、在接口中定义方法，@GetMapping("/msg")，"/msg"为服务提供的Restful API方法
         */
        String response = productClient.productMsg();
        log.info("response={}",response);


        return  response;
    }
}
