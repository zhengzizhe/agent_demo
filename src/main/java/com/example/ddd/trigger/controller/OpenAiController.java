package com.example.ddd.trigger.controller;

import com.example.ddd.configuration.config.ChatApi;
import com.example.ddd.domain.agent.model.entity.ArmoryCommandEntity;
import com.example.ddd.domain.agent.service.armory.DynamicContext;
import com.example.ddd.domain.agent.service.armory.RootNode;
import io.micronaut.context.ApplicationContext;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.inject.qualifiers.Qualifiers;
import jakarta.inject.Inject;

import static com.example.ddd.common.constant.IAgentConstant.COLON;

@Controller("/openai")
public class OpenAiController {
    @Inject
    RootNode rootNode;
    @Inject
    ApplicationContext applicationContext;

    @Post("/chat")
    public String chat(@Body String message) {
        ChatApi chatModel = applicationContext.getBean(ChatApi.class, Qualifiers.byName("ChatApi" + COLON + "5"));
        // 调用OpenAI API进行聊天
        if (chatModel == null) {
            return "不存在";
        }
        return chatModel.chat(message);
    }

    @Post("/armory")
    public String armory(@Body Long agentId) {
        return rootNode.handle(new ArmoryCommandEntity(agentId), new DynamicContext());
    }


}
