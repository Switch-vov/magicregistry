package com.switchvov.magicregistry.cluster.raft;

import lombok.Data;

import java.util.List;

/**
 * @author switch
 * @since 2024/4/21
 */
@Data
public class AppendEntriesRequest {
    private long term;
    private String leaderId;
    private long prevLogIndex;
    private long prevLogTerm;
    private List<LogItem> entries;
    private long leaderCommit;
}
