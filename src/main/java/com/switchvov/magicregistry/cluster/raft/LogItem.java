package com.switchvov.magicregistry.cluster.raft;

import com.switchvov.magicregistry.cluster.raft.state.Command;
import lombok.Data;

/**
 * @author switch
 * @since 2024/4/21
 */
@Data
public class LogItem {
    private long item;
    private Command command;
}
