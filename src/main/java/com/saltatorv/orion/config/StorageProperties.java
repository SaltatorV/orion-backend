package com.saltatorv.orion.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "orion.storage")
public class StorageProperties {

    private String path;
}