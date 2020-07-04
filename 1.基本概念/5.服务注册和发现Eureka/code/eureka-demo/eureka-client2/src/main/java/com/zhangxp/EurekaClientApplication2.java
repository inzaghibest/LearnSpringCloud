package com.zhangxp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * Created by zhangxp on 2020/6/30.
 */
@EnableEurekaClient
@SpringBootApplication
public class EurekaClientApplication2 {
    public static void main(String[] args) {
        SpringApplication.run(EurekaClientApplication2.class, args);
    }
}
