package com.zhangxp.InterFace;

import com.zhangxp.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

// 添加FeignClient注解来声明一个FeignClient,其中value为远程调用的其他服务服务名,
// configuration为Feign Client的配置类
@FeignClient(value = "eureka-client", configuration = FeignConfig.class,
            // HiHystrix作为Feign熔断器的逻辑处理类,必须实现被@FeignClient装饰的接口
            fallback = HiHystrix.class)
public interface EurekaClientFeign {
    @GetMapping("/hi")
    String sayHiFromClientEureka(@RequestParam(value = "name") String name);
}