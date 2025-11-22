package com.example.ddd.application.agent.service.armory;

import com.example.ddd.common.utils.BeanUtil;
import com.example.ddd.common.utils.ILogicHandler;
import com.example.ddd.domain.agent.model.entity.ArmoryCommandEntity;
import com.example.ddd.application.agent.service.Orchestrator;
import com.example.ddd.application.agent.service.execute.role.AgentRole;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrator构建节点
 * 在ServiceNode构建完成后，构建Orchestrator
 */
@Slf4j
@Service
public class OrchestratorNode extends AbstractArmorySupport {

    @Autowired
    private BeanUtil beanUtil;

    @Override
    public String handle(ArmoryCommandEntity armoryCommandEntity, DynamicContext dynamicContext) {
        String orchestratorId = String.valueOf(armoryCommandEntity.getOrchestratorId());
        log.info("多agent构建中 开始构建Orchestrator: orchestratorId={}", orchestratorId);
        try {
            List<ServiceNode> allNodes = beanUtil.getServiceNodes(orchestratorId);
            if (allNodes == null || allNodes.isEmpty()) {
                log.warn("多agent构建中 orchestratorId={} 没有ServiceNode，无法构建Orchestrator", orchestratorId);
                return router(armoryCommandEntity, dynamicContext);
            }
            ServiceNode supervisorNode = null;
            List<ServiceNode> workerNodes = new ArrayList<>();
            for (ServiceNode node : allNodes) {
                if (node.getRole() == AgentRole.SUPERVISOR) {
                    supervisorNode = node;
                } else {
                    workerNodes.add(node);
                }
            }
            if (supervisorNode == null) {
                log.warn("多agent构建中 orchestratorId={} 没有Supervisor节点，无法构建Orchestrator", orchestratorId);
                return router(armoryCommandEntity, dynamicContext);
            }
            if (workerNodes.isEmpty()) {
                log.warn("多agent构建中 orchestratorId={} 没有Worker节点，无法构建Orchestrator", orchestratorId);
                return router(armoryCommandEntity, dynamicContext);
            }
            Orchestrator orchestrator = new Orchestrator(orchestratorId, supervisorNode, workerNodes);
            beanUtil.registerOrchestrator(orchestratorId, orchestrator);
            log.info("多agent构建中 Orchestrator构建成功: orchestratorId={}, supervisor={}, workers={}",
                    orchestratorId, supervisorNode.getRole(), workerNodes.size());

        } catch (Exception e) {
            log.error("多agent构建中 构建Orchestrator失败: orchestratorId={}, error={}", orchestratorId, e.getMessage(), e);
            throw new RuntimeException("构建Orchestrator失败: orchestratorId=" + orchestratorId, e);
        }

        return router(armoryCommandEntity, dynamicContext);
    }

    @Override
    public ILogicHandler<ArmoryCommandEntity, DynamicContext, String> getNextHandler() {
        return null; // 这是最后一个节点
    }
}

