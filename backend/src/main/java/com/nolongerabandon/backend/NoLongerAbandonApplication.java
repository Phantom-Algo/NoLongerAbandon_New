package com.nolongerabandon.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.nolongerabandon.backend.modules.**.mapper")
@SpringBootApplication
public class NoLongerAbandonApplication {

    public static void main(String[] args) {
        SpringApplication.run(NoLongerAbandonApplication.class, args);
    }
}