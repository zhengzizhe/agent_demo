package com.example.ddd.domain.agent.service.execute.executor;

import com.example.ddd.domain.agent.service.armory.ServiceNode;
import com.example.ddd.domain.agent.service.execute.context.UserContext;
import com.example.ddd.domain.agent.service.execute.task.Task;
import lombok.extern.slf4j.Slf4j;

/**
 * 执行器工厂
 * 根据Task的executionStrategy创建对应的执行器
 * 每个策略对应一个独立的执行器类
 */
@Slf4j
public class ExecutorFactory {

    /**
     * 创建执行器
     * 根据Task的executionStrategy创建对应的执行器
     *
     * @param task        任务
     * @param serviceNode 服务节点
     * @return 执行器实例
     */
    public static TaskExecutor create(Task task, ServiceNode serviceNode) {
        return create(task, serviceNode, null);
    }

    /**
     * 创建执行器（带UserContext）
     * 根据Task的executionStrategy创建对应的执行器
     *
     * @param task        任务
     * @param serviceNode 服务节点
     * @param userContext 用户上下文
     * @return 执行器实例
     */
    public static TaskExecutor create(Task task, ServiceNode serviceNode, UserContext userContext) {
        if (task == null || serviceNode == null) {
            throw new IllegalArgumentException("Task 和 ServiceNode 不能为空");
        }

        ExecutionStrategy strategy = task.getExecutionStrategy();
        if (strategy == null) {
            // 默认使用SILENT策略
            strategy = ExecutionStrategy.SILENT;
            log.warn("任务 {} 未指定执行策略，使用默认策略 SILENT", task.getId());
        }
        return switch (strategy) {
            case SILENT:
                yield new SilentExecutor(task, serviceNode, userContext);
            case STREAMING:
                yield new StreamingExecutor(task, serviceNode, userContext);
            default:
                log.warn("未知的执行策略: {}, 使用默认SILENT策略", strategy);
                yield new SilentExecutor(task, serviceNode, userContext);
        };
    }
}

