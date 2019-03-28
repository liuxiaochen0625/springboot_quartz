/**
 *
 * Copyright (C), 2011 - 2019, .
 */
package com.example.demo.config;

import java.beans.PropertyVetoException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.druid.pool.DruidDataSource;

/**
 * @version $Id: DataSourceAutoConfiguration.java, v 0.1 2019-03-21 reus Exp $
 * @ClassName: DataSourceAutoConfiguration
 * @Description:
 * @author: reus
 */

@Configuration
public class DataSourceAutoConfiguration implements InitializingBean {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DruidDataSource dataSource() throws PropertyVetoException {
        System.out.println("自定义数据库");
        //可以在此处调用相关接口获取数据库的配置信息进行 DataSource 的配置
        //        properties.setPassword("mysql");
        return new DruidDataSource();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        DruidDataSource dataSource= dataSource();
        dataSource.setPassword("mysql");
    }
}