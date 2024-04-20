package com.switchvov.magicregistry.cluster;

import com.switchvov.magicregistry.http.HttpInvoker;
import com.switchvov.magicregistry.service.MagicRegistryService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * check health for servers.
 *
 * @author switch
 * @since 2024/4/20
 */
@Slf4j
public class ServerHealth {
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private final long timeout = 5_000;
    private final Cluster cluster;

    public ServerHealth(Cluster cluster) {
        this.cluster = cluster;
    }


    public void checkServerHealth() {
        executor.scheduleWithFixedDelay(() -> {
            try {
                updateServers();          // 1.更新服务器状态
                doElect();                // 2.选主
                syncSnapshotFromLeader(); // 3.同步快照
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, timeout, timeout, TimeUnit.MILLISECONDS);
    }

    private void doElect() {
        new Election().electLeader(cluster.getServers());
    }


    private void syncSnapshotFromLeader() {
        Server self = cluster.self();
        Server leader = cluster.leader();
        if (self.isLeader()) {
            return;
        }
        log.debug(" ===> leader version: {}, my version: {}", leader.getVersion(), self.getVersion());
        if (self.getVersion() == leader.getVersion()) {
            return;
        }
        log.debug(" ===> sync snapshot from leader: {}", leader);
        Snapshot snapshot = HttpInvoker.httpGet(leader.getUrl() + "/snapshot", Snapshot.class);
        log.debug(" ===> sync snapshot from leader: {}", leader);
        MagicRegistryService.restore(snapshot);
        log.debug(" ===> sync and restore snapshot: {}", snapshot);
    }


    private void updateServers() {
        List<Server> servers = cluster.getServers();
        servers.stream().parallel().forEach(server -> {
            try {
                if (server.equals(cluster.self())) {
                    return;
                }
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

}
