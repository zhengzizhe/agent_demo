package com.example.ddd.domain.agent.service.execute;

import com.example.ddd.common.utils.BeanUtil;
import com.example.ddd.domain.agent.model.entity.ArmoryCommandEntity;
import com.example.ddd.domain.agent.model.entity.ClientEntity;
import com.example.ddd.domain.agent.service.armory.DynamicContext;
import com.example.ddd.domain.agent.service.armory.RootNode;
import com.example.ddd.domain.agent.adapter.repository.IClientRepository;
import com.example.ddd.infrastructure.config.DSLContextFactory;
import io.micronaut.http.sse.Event;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.FluxSink;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Agent执行服务实现
 */
@Slf4j

public class AgentExecuteService implements IAgentExecuteService {

    @Inject
    private RootNode rootNode;

    @Inject
    private IClientRepository clientRepository;

    @Inject
    private DSLContextFactory dslContextFactory;

    @Inject
    private BeanUtil beanUtil;

    @Override
    public String execute(Long agentId, String message) {
        return null;
    }

    private List<ClientEntity> getClientEntities(Long agentId) {
        log.info("步骤1: 构建Agent");
        ArmoryCommandEntity armoryCommand = new ArmoryCommandEntity(agentId);
        DynamicContext dynamicContext = new DynamicContext();
        String buildResult = rootNode.handle(armoryCommand, dynamicContext);
        log.info("Agent构建完成: {}", buildResult);
        log.info("步骤2: 查询Client顺序");
        List<ClientEntity> clients = dslContextFactory.callable(dslContext -> clientRepository.queryByAgentId(dslContext, agentId));
        if (clients == null || clients.isEmpty()) {
            log.warn("Agent {} 没有配置Client", agentId);
            return null;
        }
        log.info("查询到{}个Client，顺序: {}", clients.size(),
                clients.stream().map(ClientEntity::getId).collect(Collectors.toList()));
        return clients;
    }

    @Override
    public void StreamExecute(Long agentId, String message, FluxSink<String> emitter) {
        log.info("开始流式执行Agent: agentId={}, message={}", agentId, message);
        try {
            List<ClientEntity> clients = getClientEntities(agentId);
            if (clients == null) {
                emitter.error(new RuntimeException("Agent没有配置Client"));
                return;
            }
            log.info("步骤3: 构建责任链");
            ClientExecuteHandler chain = buildChain(clients);

            // 4. 流式执行责任链
            log.info("步骤4: 流式执行责任链");
            ExecuteCommandEntity executeCommand = ExecuteCommandEntity.builder()
                    .agentId(agentId)
                    .message(message)
                    .build();
            ExecuteContext executeContext = new ExecuteContext();
            executeContext.setMessage(message);

            // 流式执行，逐字符发送
            chain.handle(executeCommand, executeContext, emitter);

            log.info("Agent流式执行完成: agentId={}", agentId);

            // 发送完成标记
            if (!emitter.isCancelled()) {
                emitter.next("\n\n[DONE]");
            }

        } catch (Exception e) {
            log.error("Agent流式执行失败: agentId={}, error={}", agentId, e.getMessage(), e);
            if (!emitter.isCancelled()) {
                emitter.error(e);
            }
        }
    }

    /**
     * 构建责任链
     * 按照Client的顺序构建，最后一个Handler的nextHandler为null
     */
    private ClientExecuteHandler buildChain(List<ClientEntity> clients) {
        if (clients == null || clients.isEmpty()) {
            return null;
        }
        // 从后往前构建，最后一个Handler的nextHandler为null
        ClientExecuteHandler nextHandler = null;
        for (int i = clients.size() - 1; i >= 0; i--) {
            ClientEntity client = clients.get(i);
            // 查询client对应的第一个modelId（用于流式调用）
            Long modelId = dslContextFactory.callable(dslContext -> {
                var models = clientRepository.queryModelsByClientId(dslContext, client.getId());
                return models != null && !models.isEmpty() ? models.get(0).getId() : null;
            });
            if (modelId != null) {
                nextHandler = new ClientExecuteHandler(beanUtil, client, modelId, nextHandler);
            } else {
                // 如果没有modelId，使用旧的构造函数（非流式）
                nextHandler = new ClientExecuteHandler(beanUtil, client.getId(), nextHandler);
            }
        }

        return nextHandler;
    }
}

