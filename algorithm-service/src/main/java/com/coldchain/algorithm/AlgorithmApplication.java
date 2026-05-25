package com.coldchain.algorithm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 冷链温控预警算法服务 — 启动类
 *
 * 启动方式：
 *   1. IDE 中右键运行 main 方法
 *   2. mvn spring-boot:run
 *   3. mvn package && java -jar target/algorithm-service-risk-v1.jar
 *
 * 启动后访问测试：
 *   curl -X POST http://localhost:5001/evaluate
 *        -H "Content-Type: application/json"
 *        -d '{ ... }'
 */
@SpringBootApplication
public class AlgorithmApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlgorithmApplication.class, args);
    }
}
