package com.example.ddd.domain.agent.service.armory;

import com.example.ddd.common.utils.BeanUtil;
import com.example.ddd.common.utils.ILogicHandler;
import com.example.ddd.common.utils.JSON;
import com.example.ddd.domain.agent.model.entity.ArmoryCommandEntity;
import com.example.ddd.domain.agent.model.entity.McpEntity;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static com.example.ddd.common.constant.IAgentConstant.MCP_KEY;

/**
 * MCP节点
 * 负责构建MCP客户端并注册到BeanUtil
 */
@Slf4j
@Singleton
public class McpNode extends AbstractArmorySupport {
    @Inject
    BeanUtil beanUtil;

    @Inject
    RagNode ragNode;

    @Override
    public String handle(ArmoryCommandEntity armoryCommandEntity, DynamicContext dynamicContext) {
        Long agentId = armoryCommandEntity.getOrchestratorId();
        String mcpJson = dynamicContext.get(MCP_KEY);
        Map<Long, List<McpEntity>> mcpMap = JSON.parseObject(mcpJson,
                new com.fasterxml.jackson.core.type.TypeReference<>() {
                });
        if (mcpMap == null || mcpMap.isEmpty()) {
            log.warn("多agent构建中 orchestratorId={} 没有配置MCP，跳过MCP构建", agentId);
            return router(armoryCommandEntity, dynamicContext);
        }
        mcpMap.values().stream()
                .flatMap(List::stream)
                .forEach(mcpEntity -> {
                    log.info("多agent构建中 构建MCP: mcpName={}, orchestratorId={}, type={}, baseUrl={}",
                            mcpEntity.getName(), agentId, mcpEntity.getType(), mcpEntity.getBaseUrl());
                    if ("streamable_http".equalsIgnoreCase(mcpEntity.getType()) && "ACTIVE".equals(mcpEntity.getStatus())) {
                        try {
                            StreamableHttpMcpTransport.Builder transportBuilder = new StreamableHttpMcpTransport.Builder();
                            transportBuilder.url(mcpEntity.getBaseUrl());
                            transportBuilder.logRequests(true);
                            transportBuilder.logResponses(true);
                            transportBuilder.timeout(Duration.ofSeconds(30));
                            transportBuilder.customHeaders(mcpEntity.getHeaders());
                            StreamableHttpMcpTransport transport = transportBuilder.build();
                            DefaultMcpClient.Builder clientBuilder = new DefaultMcpClient.Builder();
                            clientBuilder.transport(transport);
                            clientBuilder.clientName(mcpEntity.getName());
                            McpClient mcpClient = clientBuilder.build();
                            beanUtil.registerMcpClient(agentId, mcpEntity.getId(), mcpClient);
                            log.info("多agent构建中 MCP Client注册成功: orchestratorId={}, mcpId={}, mcpName={}", agentId, mcpEntity.getId(), mcpEntity.getName());
                        } catch (Exception e) {
                            log.error("多agent构建中 构建MCP Client失败: mcpId={}, mcpName={}, error={}",
                                    mcpEntity.getId(), mcpEntity.getName(), e.getMessage(), e);
                        }
                    }
                });


        return router(armoryCommandEntity, dynamicContext);
    }

    @Override
    public ILogicHandler<ArmoryCommandEntity, DynamicContext, String> getNextHandler() {
        return ragNode;
    }
}

