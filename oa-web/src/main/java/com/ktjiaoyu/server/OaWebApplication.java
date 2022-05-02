package com.ktjiaoyu.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication()
@MapperScan("com.ktjiaoyu.server.mapper")
public class OaWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(OaWebApplication.class, args);
    }

}
