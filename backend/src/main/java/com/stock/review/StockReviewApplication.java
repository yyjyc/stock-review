package com.stock.review;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.stock.review.mapper")
@EnableAsync
public class StockReviewApplication {
    public static void main(String[] args) {
        SpringApplication.run(StockReviewApplication.class, args);
    }
}
