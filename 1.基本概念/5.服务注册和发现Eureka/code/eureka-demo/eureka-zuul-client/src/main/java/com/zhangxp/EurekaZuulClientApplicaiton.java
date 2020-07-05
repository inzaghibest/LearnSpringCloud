package com.zhangxp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@SpringBootApplication
@EnableEurekaClient
// 开启Zuul功能
@EnableZuulProxy
public class EurekaZuulClientApplicaiton {
    public static void main(String[] args) {
        SpringApplication.run(EurekaZuulClientApplicaiton.class, args);
    }
}
