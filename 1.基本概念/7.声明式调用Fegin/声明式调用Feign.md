# 声明式调用Feign

Feign受Retrofit,JAXRS-2.0和WebSocket的影响，采用了声明式API接口风格，将java Http客户端绑定到它的内部。Feign的首要目标是将Java Http客户端的书写过程变得简单。

## 1.写一个Feign客户端

pom.xml文件

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <artifactId>eureka-demo</artifactId>
        <groupId>com.zhangxp</groupId>
        <version>1.0-SNAPSHOT</version>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <artifactId>eureka-feign-client</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
        </dependency>
    </dependencies>
</project>
```

在application.yml文件中做相应配置:

```yaml
spring:
  application:
    name: eureka-feign-client
server:
  port: 8767
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

### 1.1 首先是在启动类中加入EnableFeignClients注解

```java
package com.zhangxp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
public class EurekaFeignClientApplicaiton {
    public static void main(String[] args) {
        SpringApplication.run(EurekaFeignClientApplicaiton.class, args);
    }
}

```

### 1.2 之后是建立一个@FeignClient注解的接口

```java
package com.zhangxp.InterFace;

import com.zhangxp.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

// 添加FeignClient注解来声明一个FeignClient,其中value为远程调用的其他服务服务名,
// configuration为Feign Client的配置类
@FeignClient(value = "eureka-client", configuration = FeignConfig.class)
public interface EurekaClientFeign {
    @GetMapping("/hi")
    String sayHiFromClientEureka(@RequestParam(value = "name") String name);
}
```

### 1.3 建立配置类,定义远程调用失败的规则

```java
package com.zhangxp.config;

import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
    // 注入该Bean后,Feign在远程调用失败后会进行重试
    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(100, 1, 5);
    }
}
```

### 1.4 在Service层注入EurekaClientFeign的Bean,并去调用方法

```java
package com.zhangxp.service;

import com.zhangxp.InterFace.EurekaClientFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HiService {
    @Autowired
    private EurekaClientFeign eurekaClientFeign;
    public String sayHi(String name) {
        return eurekaClientFeign.sayHiFromClientEureka(name);
    }
}

```

### 1.5 在controller层写一个API接口/hi,并调用service的sayHi方法

```java
package com.zhangxp.controller;

import com.zhangxp.service.HiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HiController {
    @Autowired
    private HiService hiService;

    @GetMapping("/hi")
    public String sayHi(@RequestParam(defaultValue = "zhangxp", required = false)
                        String name) {
        return hiService.sayHi(name);
    }
}

```

启动工程，此时的工程框架如图所示:

