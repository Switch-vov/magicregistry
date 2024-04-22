package com.switchvov.magicregistry.cluster.raft.node;

/**
 * @author switch
 * @since 2024/4/21
 */
public enum NodeType {
    /**
     * 跟随者
     */
    FOLLOW,
    /**
     * 选举者
     */
    CANDIDATE,
    /**
     * 领导者
     */
    LEADER;
}
