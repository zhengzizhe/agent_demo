package com.example.ddd.application.agent.service.armory;

import com.example.ddd.common.utils.BeanUtil;
import com.example.ddd.common.utils.ILogicHandler;
import com.example.ddd.common.utils.JSON;
import com.example.ddd.domain.agent.model.entity.ArmoryCommandEntity;
import com.example.ddd.domain.agent.model.entity.ChatModelEntity;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.example.ddd.common.constant.IAgentConstant.MODEL_KEY;

@Service
@Slf4j
public class ChatModelNode extends AbstractArmorySupport {
    @Autowired
    BeanUtil beanUtil;
    @Autowired
    AgentNode agentNode;

    @Override
    public String handle(ArmoryCommandEntity armoryCommandEntity, DynamicContext dynamicContext) {
        String agentId = String.valueOf(armoryCommandEntity.getOrchestratorId());
        log.info("多agent构建中 开始构建ChatModel: orchestratorId={}", agentId);
        String chatModelJson = dynamicContext.get(MODEL_KEY);
        Map<String, ChatModelEntity> modelMap = JSON.parseObject(chatModelJson,
                new com.fasterxml.jackson.core.type.TypeReference<Map<String, ChatModelEntity>>() {
                });
        if (modelMap == null || modelMap.isEmpty()) {
            log.warn("多agent构建中 orchestratorId={} 没有配置Model，跳过ChatModel构建", agentId);
            return router(armoryCommandEntity, dynamicContext);
        }

        // 遍历所有model并注册
        modelMap.values().forEach(chatModelEntity -> {
            log.info("多agent构建中 构建ChatModel: modelName={}, orchestratorId={}", chatModelEntity.getName(), agentId);
            StreamingChatModel chatModel = getChatModel(chatModelEntity);
            beanUtil.registerChatModel(agentId, String.valueOf(chatModelEntity.getId()), chatModel);
        });

        return router(armoryCommandEntity, dynamicContext);
    }

    public StreamingChatModel getChatModel(ChatModelEntity chatModelEntity) {
        StreamingChatModel chatModel = null;
        switch (chatModelEntity.getProvider()) {
            case "OPENAI":
                chatModel = OpenAiStreamingChatModel.builder()
                        .baseUrl(chatModelEntity.getApiEndpoint())
                        .apiKey(chatModelEntity.getApiKey())
                        .modelName(chatModelEntity.getModelType())
                        .temperature(chatModelEntity.getTemperature())
                        .maxTokens(chatModelEntity.getMaxTokens())
                        .build();
                return chatModel;
            default:
                throw new RuntimeException("不支持的模型");
        }
    }

    @Override
    public ILogicHandler<ArmoryCommandEntity, DynamicContext, String> getNextHandler() {
        return agentNode; // AiServiceNode will be injected if needed
    }
}
