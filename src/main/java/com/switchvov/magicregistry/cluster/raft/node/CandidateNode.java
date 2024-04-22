package com.switchvov.magicregistry.cluster.raft.node;

/**
 * @author switch
 * @since 2024/4/21
 */
public class CandidateNode extends FollowNode {
    /**
     * 投票的候选人ID
     */
    private String votedFor;
}
