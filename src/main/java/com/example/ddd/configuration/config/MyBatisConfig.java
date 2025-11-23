package com.example.ddd.configuration.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis配置类
 */
@Configuration
@MapperScan("com.example.ddd.infrastructure.dao.mapper")
public class MyBatisConfig {
}



