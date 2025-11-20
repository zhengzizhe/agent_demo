package com.example.ddd.domain.agent.service.execute.executor;

import com.example.ddd.domain.agent.service.armory.ServiceNode;
import com.example.ddd.domain.agent.service.execute.graph.WorkspaceState;
import com.example.ddd.domain.agent.service.execute.task.Task;
import org.bsc.langgraph4j.action.NodeAction;

/**
 * 任务执行器接口
 * 包含一个 task 和一个 serviceNode
 */
public interface TaskExecutor extends NodeAction<WorkspaceState> {
    /**
     * 获取任务
     */
    Task getTask();

    /**
     * 获取服务节点
     */
    ServiceNode getServiceNode();
}


