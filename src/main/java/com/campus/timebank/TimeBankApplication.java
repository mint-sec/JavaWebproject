package com.campus.timebank;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.campus.timebank.mapper")
@SpringBootApplication
public class TimeBankApplication {

    public static void main(String[] args) {
        SpringApplication.run(TimeBankApplication.class, args);
    }
}
