package com.switchvov.magicregistry.cluster;

import com.switchvov.magicregistry.MagicRegistryConfigProperties;
import com.switchvov.magicregistry.service.MagicRegistryService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Registry cluster.
 *
 * @author switch
 * @since 2024/4/16
 */
@Slf4j
public class Cluster {
    @Value("${server.port}")
    private String port;

    private String host;

    private Server myself;

    private final MagicRegistryConfigProperties registryConfigProperties;

    public Cluster(MagicRegistryConfigProperties registryConfigProperties) {
        this.registryConfigProperties = registryConfigProperties;
    }

    @Getter
    private List<Server> servers;

    public void init() {
        initHost();
        initMyself();
        initServers();
        new ServerHealth(this).checkServerHealth();
    }

    private void initHost() {
        try {
            host = new InetUtils(new InetUtilsProperties()).findFirstNonLoopbackHostInfo().getIpAddress();
            log.debug(" ===> findFirstNonLoopbackHostInfo = {}", host);
        } catch (Exception e) {
            host = "127.0.0.1";
        }
    }

    private void initMyself() {
        myself = new Server("http://" + host + ":" + port, true, false, -1L);
        log.debug(" ===> myself = {}", myself);
    }

    private void initServers() {
        List<Server> servers = new ArrayList<>();
        for (String url : registryConfigProperties.getServerList()) {
            Server server = new Server();
            if (url.contains("localhost")) {
                url = url.replace("localhost", host);
            }
            if (url.contains("127.0.0.1")) {
                url = url.replace("127.0.0.1", host);
            }
            if (myself.getUrl().equals(url)) {
                servers.add(myself);
                continue;
            }
            server.setUrl(url);
            server.setStatus(false);
            server.setLeader(false);
            server.setVersion(-1L);
            servers.add(server);
        }
        this.servers = servers;
    }

    public Server self() {
        myself.setVersion(MagicRegistryService.VERSION.get());
        return myself;
    }

    public Server leader() {
        return servers.stream()
                .filter(Server::isStatus)
                .filter(Server::isLeader)
                .findFirst()
                .orElse(null);
    }
}
