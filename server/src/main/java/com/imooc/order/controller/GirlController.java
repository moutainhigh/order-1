package com.imooc.order.controller;

import com.imooc.order.config.GirlConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
public class GirlController {

    @Autowired
    private GirlConfig config;

    @GetMapping("/girl/print")
    public String print(){
        return "name:" + config.getName() + ",age:" + config.getAge();
    }
}
