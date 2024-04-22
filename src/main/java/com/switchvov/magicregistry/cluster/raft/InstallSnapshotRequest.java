package com.switchvov.magicregistry.cluster.raft;

import lombok.Data;

/**
 * @author switch
 * @since 2024/4/21
 */
@Data
public class InstallSnapshotRequest {
    private long term;
    private String leaderId;
    private long lastIncludedIndex;
    private long lastIncludedTerm;
    private long offset;
    private byte[] data;
    private boolean done;
}
