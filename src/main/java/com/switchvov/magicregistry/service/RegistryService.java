package com.switchvov.magicregistry.service;

import com.switchvov.magicregistry.model.InstanceMeta;

import java.util.List;
import java.util.Map;

/**
 * Interface for registry service.
 *
 * @author switch
 * @since 2024/4/13
 */
public interface RegistryService {
    InstanceMeta register(String service, InstanceMeta instance);

    InstanceMeta unregister(String service, InstanceMeta instance);

    List<InstanceMeta> getAllInstances(String service);

    long renew(InstanceMeta instance, String... services);

    Long version(String service);

    Map<String, Long> versions(String... service);
}
