package com.zhangxp.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Created by zhangxp on 2020/6/30.
 * 将RestTemplate和Ribbon相结合进行负载均衡的方式:
 * 在程序的Ioc容器中注入一个restTemplate的Bean,并在这个bean上添加LoadBabanced
 * 注解,此时RestTemplate就结合了Ribbon开启了负载均衡的功能
 */
@Configuration
public class RibbonConfig {
    @Bean
    @LoadBalanced
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
