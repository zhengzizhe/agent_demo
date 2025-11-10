package com.example.ddd.domain.agent.service.execute;

import com.example.ddd.common.utils.BeanUtil;
import com.example.ddd.domain.agent.model.entity.ArmoryCommandEntity;
import com.example.ddd.domain.agent.service.armory.DynamicContext;
import com.example.ddd.domain.agent.service.armory.RootNode;
import com.example.ddd.domain.agent.service.execute.blackBoard.InMemoryBlackboard;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.FluxSink;

/**
 * Agent执行服务实现
 */
@Slf4j

public class AgentExecuteService implements IAgentExecuteService {
    @Inject
    private BeanUtil beanUtil;
    @Inject
    RootNode rootNode;

    @Override
    public String execute(Long agentId, String message) {
        return null;
    }

    @Override
    public void StreamExecute(Long agentId, String message, FluxSink<String> emitter) {
        ArmoryCommandEntity build = ArmoryCommandEntity.builder()
                .agentId(agentId)
                .build();
        rootNode.handle(build, new DynamicContext());
        log.info("开始流式执行Agent: agentId={}, message={}", agentId, message);
        try {
            AgentOrchestrator orchestrator = tryGetOrchestrator(agentId);
            if (orchestrator != null) {
                log.info("使用Orchestrator模式执行");
                executeWithOrchestrator(agentId, message, orchestrator, emitter);
            } else {
                log.warn("无Orchestrator: agentId={}", agentId);
            }
            log.info("Agent流式执行完成: agentId={}", agentId);
        } catch (Exception e) {
            log.error("Agent流式执行失败: agentId={}, error={}", agentId, e.getMessage(), e);
            if (!emitter.isCancelled()) {
                emitter.error(e);
            }
        }
    }

    private AgentOrchestrator tryGetOrchestrator(Long agentId) {
        if (agentId == null) {
            log.warn("agentId为空，无法获取Orchestrator");
            return null;
        }
        log.info("尝试获取Orchestrator: agentId={}", agentId);
        try {
            AgentOrchestrator orchestrator = beanUtil.getOrchestrator(agentId);
            if (orchestrator != null) {
                log.info("找到Orchestrator: agentId={}", agentId);
                return orchestrator;
            } else {
                log.warn("Orchestrator不存在: agentId={}。可能原因：1) Orchestrator构建失败 2) Agent未正确配置Model", agentId);
            }
        } catch (Exception e) {
            log.error("获取Orchestrator异常: agentId={}, error={}", agentId, e.getMessage(), e);
        }
        return null;
    }

    /**
     * 使用Orchestrator执行
     */
    private void executeWithOrchestrator(Long agentId, String message,
                                         AgentOrchestrator orchestrator, FluxSink<String> emitter) {
        try {
            InMemoryBlackboard board =
                    new InMemoryBlackboard();
            orchestrator.runStream(agentId, message, 4, board, emitter);
        } catch (Exception e) {
            log.error("Orchestrator执行失败: agentId={}, error={}", agentId, e.getMessage(), e);
            if (!emitter.isCancelled()) {
                emitter.error(e);
            }
        }
    }


}

