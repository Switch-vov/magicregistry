package com.switchvov.magicregistry;

import com.switchvov.magicregistry.cluster.Cluster;
import com.switchvov.magicregistry.cluster.Server;
import com.switchvov.magicregistry.cluster.Snapshot;
import com.switchvov.magicregistry.http.HttpInvoker;
import com.switchvov.magicregistry.model.InstanceMeta;
import com.switchvov.magicregistry.service.MagicRegistryService;
import com.switchvov.magicregistry.service.RegistryService;
import com.switchvov.magicregistry.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * endpoint for magic registry.
 *
 * @author switch
 * @since 2024/4/13
 */
@RestController
@Slf4j
public class MagicRegistryController {
    private static final String ENDPOINT_REG = "/reg";
    private static final String ENDPOINT_UNREG = "/unreg";
    private static final String ENDPOINT_RENEW = "/renew";
    private static final String ENDPOINT_RENEWS = "/renews";

    private final RegistryService registryService;
    private final Cluster cluster;


    public MagicRegistryController(
            @Autowired RegistryService registryService,
            @Autowired Cluster cluster
    ) {
        this.registryService = registryService;
        this.cluster = cluster;
    }

    @RequestMapping(ENDPOINT_REG)
    public InstanceMeta register(@RequestParam String service, @RequestBody InstanceMeta instance) {
        log.info(" ===>register {} @ {}", service, instance);
        return Optional.ofNullable(changeLeader((leader) ->
                        HttpInvoker.httpPost(JsonUtils.toJson(instance), regPath(service, leader), InstanceMeta.class)))
                .orElseGet(() -> registryService.register(service, instance));
    }

    private <T> T changeLeader(Function<Server, T> f) {
        if (!cluster.self().isLeader()) {
            Server leader = cluster.leader();
            log.info(" ===> current server is not a leader, apply to leader: {}", leader.getUrl());
            return f.apply(leader);
        }
        return null;
    }

    @RequestMapping(ENDPOINT_UNREG)
    public InstanceMeta unregister(@RequestParam String service, @RequestBody InstanceMeta instance) {
        log.info(" ===> unregister {} @ {}", service, instance);
        return Optional.ofNullable(changeLeader((leader) ->
                        HttpInvoker.httpPost(JsonUtils.toJson(instance), unregPath(service, leader), InstanceMeta.class)))
                .orElseGet(() -> registryService.unregister(service, instance));
    }

    @RequestMapping("/findAll")
    public List<InstanceMeta> findAllInstances(@RequestParam String service) {
        log.info(" ===> findAllInstances {}", service);
        return registryService.getAllInstances(service);
    }

    @RequestMapping(ENDPOINT_RENEW)
    public long renew(@RequestParam String service, @RequestBody InstanceMeta instance) {
        log.info(" ===> renew {} @ {}", service, instance);
        return Optional.ofNullable(changeLeader((leader) ->
                        HttpInvoker.httpPost(JsonUtils.toJson(instance), renewPath(service, leader), Long.class)))
                .orElseGet(() -> registryService.renew(instance, service));
    }

    @RequestMapping(ENDPOINT_RENEWS)
    public long renews(@RequestParam String services, @RequestBody InstanceMeta instance) {
        log.info(" ===> renews {} @ {}", services, instance);
        return Optional.ofNullable(changeLeader((leader) ->
                        HttpInvoker.httpPost(JsonUtils.toJson(instance), renewsPath(services, leader), Long.class)))
                .orElseGet(() -> registryService.renew(instance, services.split(",")));
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


    private static String regPath(String service, Server leader) {
        return leader.getUrl() + ENDPOINT_REG + "?service=" + service;
    }

    private static String unregPath(String service, Server leader) {
        return leader.getUrl() + ENDPOINT_UNREG + "?service=" + service;
    }

    private static String renewPath(String service, Server leader) {
        return leader.getUrl() + ENDPOINT_RENEW + "?service=" + service;
    }

    private static String renewsPath(String services, Server leader) {
        return leader.getUrl() + ENDPOINT_RENEWS + "?services=" + services;
    }
}
