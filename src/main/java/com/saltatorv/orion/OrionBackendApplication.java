package com.saltatorv.orion;

import com.saltatorv.orion.config.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class OrionBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrionBackendApplication.class, args);
    }

}
