package com.example.ddd.application.agent.service;

import com.example.ddd.common.utils.BeanUtil;
import com.example.ddd.domain.agent.model.entity.ArmoryCommandEntity;
import com.example.ddd.application.agent.service.armory.DynamicContext;
import com.example.ddd.application.agent.service.armory.RootNode;
import com.example.ddd.application.agent.service.Orchestrator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 任务执行服务
 * 封装任务执行相关的业务逻辑
 */
@Service
@Slf4j
public class TaskExecuteService {

    @Autowired
    private BeanUtil beanUtil;
    @Autowired
    private RootNode rootNode;

    /**
     * 构建 Agent 系统
     *
     * @param orchestratorId Orchestrator ID
     */
    public void buildArmory(String orchestratorId) {
        ArmoryCommandEntity armoryCommandEntity = new ArmoryCommandEntity();
        armoryCommandEntity.setOrchestratorId(orchestratorId);
        rootNode.handle(armoryCommandEntity, new DynamicContext());
        log.info("多agent构建完成: orchestratorId={}", orchestratorId);
    }

    /**
     * 获取Orchestrator实例
     * 从BeanUtil中获取已构建的Orchestrator
     *
     * @param orchestratorId Orchestrator ID
     * @return Orchestrator实例，如果不存在则返回null
     */
    public Orchestrator getOrchestrator(String orchestratorId) {
        Orchestrator orchestrator = beanUtil.getOrchestrator(orchestratorId);
        if (orchestrator == null) {
            log.error("多agent执行中 orchestratorId={} 没有Orchestrator，请先调用 /armory 构建", orchestratorId);
        }
        return orchestrator;
    }
}

