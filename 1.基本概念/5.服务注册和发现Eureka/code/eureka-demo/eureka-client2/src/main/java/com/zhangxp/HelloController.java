package com.zhangxp;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by zhangxp on 2020/6/30.
 */
@RestController
public class HelloController {
    @RequestMapping("/helloworld")
    public String Hello() {
        return "Hello World!";
    }
}
