package com.switchvov.magicregistry.health;

import com.switchvov.magicregistry.cluster.Cluster;
import com.switchvov.magicregistry.model.InstanceMeta;
import com.switchvov.magicregistry.service.MagicRegistryService;
import com.switchvov.magicregistry.service.RegistryService;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Default implementation of HealthChecker.
 *
 * @author switch
 * @since 2024/4/13
 */
@Slf4j
public class MagicHealthChecker implements HealthChecker {
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private final long timeout = 20_000;

    private final RegistryService registryService;
    private final Cluster cluster;

    public MagicHealthChecker(RegistryService registryService, Cluster cluster) {
        this.registryService = registryService;
        this.cluster = cluster;
    }

    @Override
    public void start() {
        executor.scheduleWithFixedDelay(
                () -> {
                    log.info(" ===> Health checker running...");
                    long now = System.currentTimeMillis();
                    MagicRegistryService.TIMESTAMPS.forEach((serviceAndInstance, timestamp) -> {
                        if (now - timestamp <= timeout) {
                            return;
                        }
                        if (!cluster.self().isLeader()) {
                            return;
                        }
                        log.info(" ===> Health checker: {} is down", serviceAndInstance);
                        int index = serviceAndInstance.indexOf("@");
                        String service = serviceAndInstance.substring(0, index);
                        String url = serviceAndInstance.substring(index + 1);
                        InstanceMeta instance = InstanceMeta.from(url);
                        registryService.unregister(service, instance);
                        MagicRegistryService.TIMESTAMPS.remove(serviceAndInstance);
                    });
                }, 10, 10, TimeUnit.SECONDS
        );
    }

    @Override
    public void stop() {

    }
}
