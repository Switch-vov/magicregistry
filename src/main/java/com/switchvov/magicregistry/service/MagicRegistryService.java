package com.switchvov.magicregistry.service;

import com.switchvov.magicregistry.cluster.Snapshot;
import com.switchvov.magicregistry.model.InstanceMeta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Default implementation of RegisterService.
 *
 * @author switch
 * @since 2024/4/13
 */
@Slf4j
public class MagicRegistryService implements RegistryService {
    private final static MultiValueMap<String, InstanceMeta> REGISTRY = new LinkedMultiValueMap<>();
    private final static Map<String, Long> VERSIONS = new ConcurrentHashMap<>();
    public final static Map<String, Long> TIMESTAMPS = new ConcurrentHashMap<>();
    public final static AtomicLong VERSION = new AtomicLong(0);

    @Override
    public synchronized InstanceMeta register(String service, InstanceMeta instance) {
        List<InstanceMeta> instances = REGISTRY.get(service);
        if (Objects.isNull(instances)) {
            instances = new ArrayList<>();
            REGISTRY.put(service, instances);
        }
        if (instances.contains(instance)) {
            log.info(" ===> instance {} already registered", instance.toUrl());
            instance.setStatus(true);
            return instance;
        }
        log.info(" ===> register instance {}", instance.toUrl());
        instances.add(instance);
        instance.setStatus(true);
        renew(instance, service);
        VERSIONS.put(service, VERSION.incrementAndGet());
        return instance;
    }

    @Override
    public synchronized InstanceMeta unregister(String service, InstanceMeta instance) {
        List<InstanceMeta> instances = REGISTRY.get(service);
        if (Objects.isNull(instances) || instances.isEmpty()) {
            return null;
        }
        log.info(" ===> unregister instance {}", instance.toUrl());
        instances.removeIf(ins -> ins.equals(instance));
        instance.setStatus(false);
        renew(instance, service);
        VERSIONS.put(service, VERSION.incrementAndGet());
        return instance;
    }

    @Override
    public List<InstanceMeta> getAllInstances(String service) {
        return REGISTRY.getOrDefault(service, new ArrayList<>());
    }

    @Override
    public synchronized long renew(InstanceMeta instance, String... services) {
        long now = System.currentTimeMillis();
        for (String service : services) {
            TIMESTAMPS.put(service + "@" + instance.toUrl(), now);
        }
        return now;
    }

    @Override
    public Long version(String service) {
        return VERSIONS.get(service);
    }

    @Override
    public Map<String, Long> versions(String... services) {
        return Arrays.stream(services).collect(HashMap::new, (map, service) -> map.put(service, version(service)), HashMap::putAll);
    }

    public static synchronized Snapshot snapshot() {
        LinkedMultiValueMap<String, InstanceMeta> registry = new LinkedMultiValueMap<>();
        registry.addAll(REGISTRY);
        Map<String, Long> versions = new HashMap<>(VERSIONS);
        Map<String, Long> timestamps = new HashMap<>(TIMESTAMPS);
        return new Snapshot(registry, versions, timestamps, VERSION.get());
    }

    public static synchronized long restore(Snapshot snapshot) {
        REGISTRY.clear();
        REGISTRY.addAll(snapshot.getRegistry());
        VERSIONS.clear();
        VERSIONS.putAll(snapshot.getVersions());
        TIMESTAMPS.clear();
        TIMESTAMPS.putAll(snapshot.getTimestamps());
        VERSION.set(snapshot.getVersion());
        return snapshot.getVersion();
    }
}
