package com.switchvov.magicregistry.cluster;

import com.switchvov.magicregistry.MagicRegistryConfigProperties;
import com.switchvov.magicregistry.http.HttpInvoker;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

    private MagicRegistryConfigProperties registryConfigProperties;

    public Cluster(MagicRegistryConfigProperties registryConfigProperties) {
        this.registryConfigProperties = registryConfigProperties;
    }

    @Getter
    private List<Server> servers;

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private final long timeout = 5_000;


    public void init() {
        try {
            host = new InetUtils(new InetUtilsProperties()).findFirstNonLoopbackHostInfo().getIpAddress();
            log.debug(" ===> findFirstNonLoopbackHostInfo = {}", host);
        } catch (Exception e) {
            host = "127.0.0.1";
        }

        myself = new Server("http://" + host + ":" + port, true, false, -1L);
        log.debug(" ===> myself = {}", myself);

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

        executor.scheduleWithFixedDelay(() -> {
            try {
                updateServers();
                electLeader();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, timeout, timeout, TimeUnit.MILLISECONDS);
    }

    private void electLeader() {
        List<Server> masters = servers.stream().filter(Server::isStatus).filter(Server::isLeader).toList();
        if (masters.isEmpty()) {
            log.debug(" ===> elect for no leader: {}", servers);
            elect();
            return;
        }
        if (masters.size() > 1) {
            log.debug(" ===> elect for more than one leader: {}", servers);
            elect();
            return;
        }
        log.debug(" ===> no need election for leader: {}", masters.get(0));
    }

    private void elect() {
        // 1. 各个节点自己选，算法保证大家选的是同一个
        // 2. 外部有一个分布式锁，谁拿到锁，谁是主
        // 3. 分布式一致性算法，比如paxos,raft
        for (Server server : servers) {
            server.setLeader(false);
        }
        Optional<Server> minCandidate = servers.stream()
                .filter(Server::isStatus)
                .min(Comparator.comparingInt(Server::hashCode));
        if (minCandidate.isPresent()) {
            Server leader = minCandidate.get();
            leader.setLeader(true);
            log.debug(" ===> elect for leader: {}", leader);
            return;
        }
        log.debug(" ===> elect failed for no leaders: {}", servers);
    }

    private void updateServers() {
        servers.forEach(server -> {
            try {
                Server serverInfo = HttpInvoker.httpGet(server.getUrl() + "/info", Server.class);
                log.debug(" ===> health check success for {}", serverInfo);
                if (!Objects.isNull(serverInfo)) {
                    server.setStatus(true);
                    server.setLeader(serverInfo.isLeader());
                    server.setVersion(serverInfo.getVersion());
                }
            } catch (Exception e) {
                log.debug(" ===> health check failed for {}", server);
                server.setStatus(false);
                server.setLeader(false);
            }
        });
    }

    public Server self() {
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
