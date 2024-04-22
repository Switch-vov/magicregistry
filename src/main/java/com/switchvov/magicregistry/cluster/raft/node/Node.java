package com.switchvov.magicregistry.cluster.raft.node;

import com.switchvov.magicregistry.cluster.raft.LogItem;
import lombok.Data;

import java.util.List;

/**
 * @author switch
 * @since 2024/4/21
 */
@Data
public class Node {
    /**
     * 当前任期号
     */
    private long currentTerm;
    /**
     * 节点类型
     */
    private NodeType nodeType;

    /**
     * 日志
     */
    private List<LogItem> log;

    /**
     * 最大的已提交
     */
    private long commitIndex;
}
