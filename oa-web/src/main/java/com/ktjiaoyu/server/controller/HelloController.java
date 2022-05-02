package com.ktjiaoyu.server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class HelloController {
    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

    /*// 基本资料 权限
    @GetMapping("/employee/basic/list")
    public String hello2() {
        return "/employee/basic/list";
    }

    // 高级资料 权限
    @GetMapping("/employee/advanced/list")
    public String hello3() {
        return "/employee/advanced/list";
    }*/

}


