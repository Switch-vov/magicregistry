package com.switchvov.magicregistry.cluster;

import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * @author switch
 * @since 2024/4/20
 */
@Slf4j
public class Election {
    public void electLeader(List<Server> servers) {
        List<Server> masters = servers.stream().filter(Server::isStatus).filter(Server::isLeader).toList();
        if (masters.isEmpty()) {
            log.warn(" ===> elect for no leader: {}", servers);
            elect(servers);
            return;
        }
        if (masters.size() > 1) {
            log.warn(" ===> elect for more than one leader: {}", servers);
            elect(servers);
            return;
        }
        log.debug(" ===> no need election for leader: {}", masters.get(0));
    }

    private void elect(List<Server> servers) {
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
        log.warn(" ===> elect failed for no leaders: {}", servers);
    }

}
