package com.example.ddd.trigger.controller;

import com.example.ddd.configuration.config.ChatApi;
import com.example.ddd.domain.agent.model.entity.ArmoryCommandEntity;
import com.example.ddd.domain.agent.service.armory.DynamicContext;
import com.example.ddd.domain.agent.service.armory.RootNode;
import com.example.ddd.domain.agent.service.execute.AgentExecuteService;
import com.example.ddd.trigger.request.AgentExecuteRequest;
import io.micronaut.context.ApplicationContext;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.inject.qualifiers.Qualifiers;
import jakarta.inject.Inject;

import static com.example.ddd.common.constant.IAgentConstant.COLON;

@Controller("/openai1")
public class OpenAiController {
    @Inject
    RootNode rootNode;
    @Inject
    ApplicationContext applicationContext;
    @Inject
    AgentExecuteService agentExecuteService;

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

    /**
     * 执行Agent
     *
     * @param request 请求参数，包含agentId和message
     * @return 执行结果
     */
    @Post("/execute")
    public String execute(@Body AgentExecuteRequest request) {
        return agentExecuteService.execute(request.getAgentId(), request.getMessage());
    }


}










