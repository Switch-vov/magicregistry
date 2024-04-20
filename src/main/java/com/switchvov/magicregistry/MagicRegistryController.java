package com.switchvov.magicregistry;

import com.switchvov.magicregistry.cluster.Cluster;
import com.switchvov.magicregistry.cluster.Server;
import com.switchvov.magicregistry.cluster.Snapshot;
import com.switchvov.magicregistry.model.InstanceMeta;
import com.switchvov.magicregistry.service.MagicRegistryService;
import com.switchvov.magicregistry.service.RegistryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * endpoint for magic registry.
 *
 * @author switch
 * @since 2024/4/13
 */
@RestController
@Slf4j
public class MagicRegistryController {
    private final RegistryService registryService;
    private final Cluster cluster;

    public MagicRegistryController(
            @Autowired RegistryService registryService,
            @Autowired Cluster cluster
    ) {
        this.registryService = registryService;
        this.cluster = cluster;
    }

    @RequestMapping("/reg")
    public InstanceMeta register(@RequestParam String service, @RequestBody InstanceMeta instance) {
        log.info(" ===>register {} @ {}", service, instance);
        checkLeader();
        return registryService.register(service, instance);
    }

    private void checkLeader() {
        if (!cluster.self().isLeader()) {
            throw new RuntimeException("current server is not a leader, the leader is " + cluster.leader().getUrl());
        }
    }

    @RequestMapping("/unreg")
    public InstanceMeta unregister(@RequestParam String service, @RequestBody InstanceMeta instanceMeta) {
        log.info(" ===> unregister {} @ {}", service, instanceMeta);
        checkLeader();
        return registryService.unregister(service, instanceMeta);
    }

    @RequestMapping("/findAll")
    public List<InstanceMeta> findAllInstances(@RequestParam String service) {
        log.info(" ===> findAllInstances {}", service);
        return registryService.getAllInstances(service);
    }

    @RequestMapping("/renew")
    public long renew(@RequestParam String service, @RequestBody InstanceMeta instance) {
        log.info(" ===> renew {} @ {}", service, instance);
        checkLeader();
        return registryService.renew(instance, service);
    }

    @RequestMapping("/renews")
    public long renews(@RequestParam String services, @RequestBody InstanceMeta instance) {
        log.info(" ===> renews {} @ {}", services, instance);
        checkLeader();
        return registryService.renew(instance, services.split(","));
    }

    @RequestMapping("/version")
    public Long version(@RequestParam String service) {
        log.info(" ===> version {}", service);
        return registryService.version(service);
    }

    @RequestMapping("/versions")
    public Map<String, Long> versions(@RequestParam String services) {
        log.info(" ===> versions {}", services);
        return registryService.versions(services.split(","));
    }

    @RequestMapping("/info")
    public Server info() {
        log.info(" ===> info {}", cluster.self());
        return cluster.self();
    }

    @RequestMapping("/cluster")
    public List<Server> cluster() {
        log.info(" ===> cluster {}", cluster.getServers());
        return cluster.getServers();
    }

    @RequestMapping("/leader")
    public Server leader() {
        log.info(" ===> leader {}", cluster.leader());
        return cluster.leader();
    }

    @RequestMapping("/sl")
    public Server sl() {
        cluster.self().setLeader(true);
        log.info(" ===> sl {}", cluster.self());
        return cluster.self();
    }

    @RequestMapping("/snapshot")
    public Snapshot snapshot() {
        log.info(" ===> snapshot {}", MagicRegistryService.snapshot());
        return MagicRegistryService.snapshot();
    }

}
