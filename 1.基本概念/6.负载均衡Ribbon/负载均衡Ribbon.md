

# 负载均衡Ribbon

## 1. Ribbon简介

负载均衡是指将负载分摊到多个执行单元上，常见的负载均衡有两种方式：一种是独立进程单元，通过负载均衡策略，将请求转发到不同的执行单元上，例如:Ngnix;另一种是将负载均衡逻辑以代码的形式封装到服务消费者的客户端上，服务消费者客户端维护了一份服务提供者的信息，有了信息列表，通过负载均衡策略将请求分摊给多个服务提供者，从而达到负载均衡的目的。

Ribbon是Netflix公司开源的一个负载均衡的组件属于上述的第二种方式。**是将负载均衡逻辑封装在客户端中，并且运行在客户端的进程里。**

在Spring Cloud构建的微服务系统中,Ribbon做为服务消费者的负载均衡器，有两种使用方式，一种是和RestTemplate相结合，另一种是和Feign相结合。Feign已经默认继承了Ribbon.

目前Ribbon用于生产环境的子模块如下：

ribbon-loadbalancer:可以独立使用或与其他模块一起使用的负载均衡API。

ribbon-eureka:Ribbon结合Eureka客户端的API,为负载均衡提供动态服务注册列表信息。

ribbon-core:Ribbon的核心API。

## 2.使用RestTemplate和Ribbon来消费服务

启动一个Eureka-server服务注册中心,端口号为:8761然后启动两个eureka-client实例向eureka-server注册，他们都提供一个/hi服务。端口号分别为8762和8763.

在之前工程的基础上,在新建一个Module,取名为eureka-ribbon-client,其作为服务消费者，通过RestTemplate来远程调用eureka-client服务API接口的/hi,即消费服务。



pom文件:

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

    <artifactId>eureka-ribbon-client</artifactId>

    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spirng-cloud-starter-netflix-ribbon</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

    </dependencies>
</project>
```

在配置文件中,服务启动的端口号为8765,向8761服务中心注册:

```yaml
server:
  port: 8765
spring:
  application:
    name: eureka-ribbon-client
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

程序入口

```java
package com.zhangxp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * Created by zhangxp on 2020/6/30.
 */
@EnableEurekaClient
@SpringBootApplication
public class EurekaRibbonClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaRibbonClientApplication.class, args);
    }
}

```

现在需要写一个服务，消费eureka-client实例中的/hi服务，希望做到轮流调用8762,8763端口的服务，需要将RestTemplate和Ribbon相结合进行负载均衡：

首先需要在IOC容器中注入一个restTemplate的bean,并在这个bean上开启@LoadBalance注解,此时RestTemplate就结合了Ribbon开启了负载均衡功能:

```java
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
```

写一个Service类，用来利用restTemplate消费eureka-client的服务：

```java
package com.zhangxp.service;

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

    public String hi(String name) {
        // Uri上不需要使用硬编码,只需要写服务名eureka-client即可
        return restTemplate.getForObject("http://eureka-client/hi?name=" + name, String.class);
    }
}
```

在写一个controller类，暴漏/hi服务:

```java
package com.zhangxp.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
    @GetMapping("/hi")
    public String hi(@RequestParam(required = false, defaultValue = "zhangxp") String name) {
        return ribbonService.hi(name);
    }
}

```

启动工程后，在浏览器中通过http://localhost:8765/hi?name=zhangxp来访问，多次访问，浏览器上分别打印出:

Hizhangxp, i am from port: 8764

Hizhangxp, i am from port: 8763

说明负载均衡器起了作用，分别轮流访问8763和8764两个实例中的/hi服务。

## 3.LoadBalancerClient简介

负载均衡器的核心为LoadBalancerClient,其可以获取服务提供者的信息,从而轮流调用服务:

```java
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

```

重新启动服务，在浏览器中访问/testRibbon,轮流打印:

localhost : 8764

localhost : 8763

可见,loadBalancerClient的choose方法，可以轮流获取eureka-client两个服务的实例信息。

loadBalancerClient实际上是从Eureka Client获取服务注册列表信息的，并将服务注册列表信息缓存了一份。在loadBalancerClient调用choose方法时，根据负载均衡策略选择了一个服务实例，从而进行负载均衡的。

loadBalancerClient也可以不从eureka-client上获取服务注册信息，这时需要自己维护一份服务注册信息

新建一个eureka-ribbon-client2工程,与之前工程类似，只是配置文件:

application.yml:

```yaml
server:
  port: 8766
stores:
  ribbon:
    listOfServers: example.com,google.com
ribbon:
  eureka:
    enabled: false
```

自己配置服务stores,并维护两个不同的url地址，即服务实例,新建一个服务:

```java
package com.zhangxp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RibbonController {
    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @GetMapping("/testRibbon")
    public String testRibbon() {
        ServiceInstance serviceInstance = loadBalancerClient.choose("stores");
        return serviceInstance.getHost() + ":" + serviceInstance.getPort();
    }
}

```

启动服务，通过浏览器访问:

发现轮流打印:

google.com:80

example.com:80

由此可以知道,在Ribbon负载均衡客户端为LoadBalancerClient.在Spring Cloud项目中，负载均衡器Ribbon会默认从Eureka Client的服务注册列表中获取服务信息，并缓存一份。根据缓存的服务注册列表信息，可以通过LoadBalancerClient来选择不同的服务实例，从而实现负载均衡。如果禁止Ribbon从Eureka获取注册列表信息，则需要自己去维护一份服务注册列表信息。根据自己维护的服务注册列表信息,Ribbon也可以实现负载均衡。