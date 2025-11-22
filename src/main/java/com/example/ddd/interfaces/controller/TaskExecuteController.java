package com.example.ddd.interfaces.controller;

import com.example.ddd.application.agent.service.execute.memory.InMemory;
import com.example.ddd.domain.agent.model.entity.ArmoryCommandEntity;
import com.example.ddd.application.agent.service.TaskExecuteService;
import com.example.ddd.application.agent.service.armory.DynamicContext;
import com.example.ddd.application.agent.service.armory.RootNode;
import com.example.ddd.application.agent.service.Orchestrator;
import com.example.ddd.application.agent.service.execute.context.UserContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.GraphStateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * 任务执行Controller
 * 流程：用户请求 → Orchestrator → 主管生成plan → 构建图 → 执行
 * Controller 层只能引用 Service 层
 */
@Slf4j
@RestController
@RequestMapping("/task")
public class TaskExecuteController {
    @Autowired
    RootNode rootNode;
    @Autowired
    private TaskExecuteService taskExecuteService;

    /**
     * 执行任务
     *
     * @param request 执行请求
     * @return SSE事件流
     */
    @PostMapping(value = "/execute", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<UserContext.TaskStatusEvent> execute(@RequestBody TaskExecuteRequest request) {
        ArmoryCommandEntity armoryCommandEntity = new ArmoryCommandEntity();
        armoryCommandEntity.setOrchestratorId(request.getOrchestratorId());
        rootNode.handle(armoryCommandEntity,new DynamicContext());
        // 先构建 Agent 系统
        taskExecuteService.buildArmory(request.getOrchestratorId());
        log.info("多agent执行中 收到任务执行请求: orchestratorId={}, message={}", request.getOrchestratorId(), request.getMessage());
        return Flux.create(emitter -> {
            Orchestrator orchestrator = taskExecuteService.getOrchestrator(request.getOrchestratorId());
            if (orchestrator == null) {
                emitter.error(new IllegalStateException("无法获取Orchestrator，请先调用 /armory 构建"));
                return;
            }
            UserContext userContext = new UserContext();

            // 设置用户ID和会话ID
            String userId = request.getUserId() != null ? request.getUserId() : "default_user";
            String sessionId = request.getSessionId() != null ? request.getSessionId() :
                    UUID.randomUUID().toString();
            userContext.setUserId(userId);
            userContext.setSessionId(sessionId);

            // 保存用户输入到 InMemory
            if (request.getMessage() != null && !request.getMessage().trim().isEmpty()) {
                InMemory.getInstance()
                        .addMessage(userId, sessionId, "USER", request.getMessage());
            }

            userContext.startEventDispatcher(emitter);
            new Thread(() -> {
                try {
                    orchestrator.execute(request.getMessage(), userContext);
                    userContext.complete();
                } catch (GraphStateException e) {
                    log.error("执行任务失败: {}", e.getMessage(), e);
                    userContext.error("执行任务失败: " + e.getMessage());
                    userContext.stopEventDispatcher();
                    emitter.error(e);
                } catch (Exception e) {
                    log.error("执行任务异常: {}", e.getMessage(), e);
                    userContext.error("执行任务异常: " + e.getMessage());
                    userContext.stopEventDispatcher();
                    emitter.error(e);
                }
            }, "UserContext-EventDispatcher-Wait").start();
        });
    }

    /**
     * 任务执行请求
     */
    @Data
    public static class TaskExecuteRequest {
        private String orchestratorId;
        private String message;
        private String userId;      // 用户ID（可选，如果不传则使用默认值）
        private String sessionId;   // 会话ID（可选，如果不传则自动生成）
    }
}
