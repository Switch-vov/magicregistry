package com.switchvov.magicregistry.cluster.raft;

import lombok.Data;

/**
 * @author switch
 * @since 2024/4/21
 */
@Data
public class RequestVoteRequest {
    private long term;
    private String candidateId;
    private long lastLogIndex;
    private long lastLogTerm;
}
