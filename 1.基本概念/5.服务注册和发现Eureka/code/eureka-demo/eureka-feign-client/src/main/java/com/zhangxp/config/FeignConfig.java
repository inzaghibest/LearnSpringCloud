package com.zhangxp.config;

import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
    // 注入该Bean后,Feign在远程调用失败后会进行重试
    // 重写了FeignClientsConfiguration类中的Bean,覆盖掉默认的配置Bean,从而达到
    // 自定义配置的目的.
    @Bean
    public Retryer feignRetryer() {
        // 重试时间间隔为100ms,最大重试时间为1秒,重试次数为5此。
        return new Retryer.Default(100, 1, 5);
    }
}
