package com.zhangxp.service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Created by zhangxp on 2020/6/30.
 * 通过restTemplate去调用eureka-client的API接口
 */
@Service
public class RibbonService {
    @Autowired
    RestTemplate restTemplate;

    // 使用RestTemplate进行远程调用
    // 使用HystrixCommand注解,hi方法就有了熔断器的功能,其中,fallbackMethod为处理回退
    // 的逻辑方法
    @HystrixCommand(fallbackMethod = "hiError")
    public String hi(String name) {
        // Uri上不需要使用硬编码,只需要写服务名eureka-client即可
        return restTemplate.getForObject("http://eureka-client/hi?name=" + name, String.class);
    }

    public String hiError(String name) {
        return "hi, " + name + " sorry, error!";
    }
}
