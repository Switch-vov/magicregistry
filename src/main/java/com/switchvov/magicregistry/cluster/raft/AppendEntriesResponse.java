package com.switchvov.magicregistry.cluster.raft;

import lombok.Data;

/**
 * @author switch
 * @since 2024/4/21
 */
@Data
public class AppendEntriesResponse {
    private long term;
    private boolean success;
}
