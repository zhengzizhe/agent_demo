package com.example.ddd.trigger.controller;

import com.example.ddd.common.utils.BeanUtil;
import com.example.ddd.domain.agent.model.entity.ArmoryCommandEntity;
import com.example.ddd.domain.agent.service.armory.DynamicContext;
import com.example.ddd.domain.agent.service.armory.RootNode;
import com.example.ddd.domain.agent.service.execute.Orchestrator;
import com.example.ddd.domain.agent.service.execute.context.UserContext;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.GraphStateException;
import reactor.core.publisher.Flux;

/**
 * 任务执行Controller
 * 流程：用户请求 → Orchestrator → 主管生成plan → 构建图 → 执行
 */
@Slf4j
@Controller("/task")
public class TaskExecuteController {

    @Inject
    private BeanUtil beanUtil;
    @Inject
    RootNode rootNode;

    /**
     * 执行任务
     *
     * @param request 执行请求
     * @return SSE事件流
     */
    @Post(value = "/execute", produces = MediaType.TEXT_EVENT_STREAM)
    public Flux<UserContext.TaskStatusEvent> execute(@Body TaskExecuteRequest request) {
        ArmoryCommandEntity armoryCommandEntity = new ArmoryCommandEntity();
        armoryCommandEntity.setOrchestratorId(request.getOrchestratorId());
        rootNode.handle(armoryCommandEntity, new DynamicContext());
        log.info("多agent执行中 收到任务执行请求: orchestratorId={}, message={}", request.getOrchestratorId(), request.getMessage());
        return Flux.create(emitter -> {
            Orchestrator orchestrator = getOrchestrator(request.getOrchestratorId());
            if (orchestrator == null) {
                emitter.error(new IllegalStateException("无法获取Orchestrator，请先调用 /armory 构建"));
                return;
            }
            UserContext userContext = new UserContext();
            userContext.startEventDispatcher(emitter);
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
        });
    }

    /**
     * 获取Orchestrator实例
     * 从BeanUtil中获取已构建的Orchestrator
     */
    private Orchestrator getOrchestrator(Long orchestratorId) {
        Orchestrator orchestrator = beanUtil.getOrchestrator(orchestratorId);
        if (orchestrator == null) {
            log.error("多agent执行中 orchestratorId={} 没有Orchestrator，请先调用 /armory 构建", orchestratorId);
        }
        return orchestrator;
    }

    /**
     * 任务执行请求
     */
    @Data
    @Serdeable
    public static class TaskExecuteRequest {
        private Long orchestratorId;
        private String message;
    }


}

