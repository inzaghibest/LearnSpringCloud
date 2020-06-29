package com.zhangxp.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Administrator
 */
@RestController
public class IndexController {
    @RequestMapping("/")
    public String index() {
        return "hello world!";
    }
}
