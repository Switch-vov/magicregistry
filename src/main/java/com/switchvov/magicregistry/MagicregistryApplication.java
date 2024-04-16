package com.switchvov.magicregistry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(MagicRegistryConfigProperties.class)
public class MagicregistryApplication {

    public static void main(String[] args) {
        SpringApplication.run(MagicregistryApplication.class, args);
    }

}
