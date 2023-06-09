package com.cgz.message;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.cgz.message.dao.mapper")
@SpringBootApplication
public class MessageStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(MessageStoreApplication.class, args);
    }

}
