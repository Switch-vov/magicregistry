package com.switchvov.magicregistry;

import com.switchvov.magicregistry.cluster.Cluster;
import com.switchvov.magicregistry.health.HealthChecker;
import com.switchvov.magicregistry.health.MagicHealthChecker;
import com.switchvov.magicregistry.service.MagicRegistryService;
import com.switchvov.magicregistry.service.RegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * configuration for all beans.
 *
 * @author switch
 * @since 2024/4/13
 */
@Configuration
public class MagicRegistryConfig {
    @Bean
    public RegistryService registryService() {
        return new MagicRegistryService();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public HealthChecker healthChecker(
            @Autowired RegistryService registryService
    ) {
        return new MagicHealthChecker(registryService);
    }

    @Bean
    public Cluster cluster(
            @Autowired MagicRegistryConfigProperties registryConfigProperties
    ) {
        return new Cluster(registryConfigProperties);
    }
}
