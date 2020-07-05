package com.zhangxp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.zhangxp.service.RibbonService;

/**
 * Created by zhangxp on 2020/6/30.
 */
@RestController
public class RibbonController {
    @Autowired
    RibbonService ribbonService;
    @Autowired
    private LoadBalancerClient loadBalancerClient;
    @GetMapping("/hi")
    public String hi(@RequestParam(required = false, defaultValue = "zhangxp") String name) {
        return ribbonService.hi(name);
    }

    @GetMapping("/testRibbon")
    public String testRibbon() {
        ServiceInstance serviceInstance = loadBalancerClient.choose("eureka-client");
        return serviceInstance.getHost() + " : " + serviceInstance.getPort();
    }
}
