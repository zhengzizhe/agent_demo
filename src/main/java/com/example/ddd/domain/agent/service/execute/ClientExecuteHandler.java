package com.example.ddd.domain.agent.service.execute;

import com.example.ddd.common.utils.BeanUtil;
import com.example.ddd.common.utils.AbstractStrategyILogicRoot;
import com.example.ddd.configuration.config.ChatApi;
import com.example.ddd.domain.agent.model.entity.ClientEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.TokenStream;
import io.micronaut.http.sse.Event;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.FluxSink;

import java.util.Map;

import static com.example.ddd.common.constant.IAgentConstant.COLON;

/**
 * Client执行处理器
 * 负责调用ChatApi处理消息
 */
@Slf4j
public class ClientExecuteHandler extends AbstractStrategyILogicRoot<ExecuteCommandEntity, ExecuteContext, String> {

    private final BeanUtil beanUtil;
    private final Long clientId;
    private final ClientEntity clientEntity;
    private final Long modelId; // 存储第一个modelId，用于获取ChatModel
    private final ClientExecuteHandler nextHandler;

    public ClientExecuteHandler(BeanUtil beanUtil, Long clientId, ClientExecuteHandler nextHandler) {
        this.beanUtil = beanUtil;
        this.clientId = clientId;
        this.clientEntity = null;
        this.modelId = null;
        this.nextHandler = nextHandler;
    }

    public ClientExecuteHandler(BeanUtil beanUtil, ClientEntity clientEntity, Long modelId, ClientExecuteHandler nextHandler) {
        this.beanUtil = beanUtil;
        this.clientId = clientEntity.getId();
        this.clientEntity = clientEntity;
        this.modelId = modelId;
        this.nextHandler = nextHandler;
    }

    @Override
    public String handle(ExecuteCommandEntity command, ExecuteContext context) {
        log.info("开始处理Client: clientId={}, message={}", clientId, command.getMessage());
        try {
            ChatApi chatApi = beanUtil.getBean(ChatApi.class, "ChatApi" + COLON + clientId);
            if (chatApi == null) {
                log.warn("ChatApi不存在: clientId={}", clientId);
                return router(command, context);
            }

            // 使用当前消息或上一步的结果作为输入（上一步是 AgentMessage，则取 reply）
            String inputMessage = context.getResult() != null ? context.getResult().getReply() : command.getMessage();

            // 非流式：约定 chat 返回的就是 JSON（或你也可让其返回纯文本，这里用解析器兜底）
            String result = chatApi.chat(inputMessage);
            AgentMessage agentMessage;
            try {
                agentMessage = AgentMessageParser.fromJson(result);
            } catch (Exception ex) {
                // 容错：如果上游返回纯文本，包装为 AgentMessage
                agentMessage = wrapPlainTextAsAgentMessage(context, result);
            }

            log.info("Client处理完成: clientId={}, step={}, action={}, status={}",
                    clientId,
                    agentMessage.getStep(),
                    agentMessage.getAction(),
                    agentMessage.getStatus());

            context.setResult(agentMessage);
            context.setCurrentClientId(clientId);
            return router(command, context);
        } catch (Exception e) {
            log.error("Client处理失败: clientId={}, error={}", clientId, e.getMessage(), e);
            throw new RuntimeException("Client处理失败: " + clientId, e);
        }
    }

    /**
     * 流式处理：只推 reply 的增量（delta），完成时一次性推送完整 AgentMessage JSON
     */
    public void handle(ExecuteCommandEntity command, ExecuteContext context, FluxSink<String> emitter) {
        log.info("开始流式处理Client: clientId={}, message={}", clientId, command.getMessage());
        try {
            StreamingChatModel chatModel = getChatModelForClient();
            if (chatModel == null) {
                log.warn("ChatModel不存在: clientId={}", clientId);
                return; // 直接返回，不做路由（防止空转）
            }
            String inputMessage = context.getResult() != null
                    ? context.getResult().getReply()
                    : command.getMessage();
            log.info("开始流式调用ChatModel: clientId={}", clientId);
            ChatApi chatApi = beanUtil.getBean(ChatApi.class, "ChatApi" + COLON + clientId);
            final StringBuilder replyBuf = new StringBuilder();
            TokenStream tokenStream = chatApi.stream(inputMessage);
            tokenStream.onPartialResponse(token -> {
                if (!emitter.isCancelled()) {
                    replyBuf.append(token);
                    log.info("推送纯文本增量: clientId={}, step={}, text={}",
                            clientId,
                            safeStep(replyBuf.toString()),
                            token);
                    emitter.next(token);
                }
            });
            tokenStream.onCompleteResponse(e -> {
                try {
                    AiMessage aiMessage = e.aiMessage();
                    String text = aiMessage.text();
                    log.info("组装最终消息: clientId={}, step={}, text={}",
                            clientId,
                            safeStep(text),
                            text);
                    AgentMessage msg = AgentMessageParser.fromJson(text);
                    // 更新上下文，不向前端再推送任何内容
                    context.setResult(msg);
                    context.setCurrentClientId(clientId);
                    routerStream(command, context, emitter);
                } catch (Exception ex) {
                    log.error("封装最终消息失败: {}", ex.getMessage(), ex);
                    if (!emitter.isCancelled()) {
                        emitter.error(ex); // 不路由
                    }
                }
            });
            tokenStream.onError(error -> {
                log.error("流式生成出错: clientId={}, error={}", clientId, error.getMessage(), error);
                if (!emitter.isCancelled()) {
                    emitter.error(error);
                }
            });
            tokenStream.start();
        } catch (Exception e) {
            log.error("Client流式处理失败: clientId={}, error={}", clientId, e.getMessage(), e);
            if (!emitter.isCancelled()) {
                emitter.error(e); // 不路由
            }
            throw new RuntimeException("Client处理失败: " + clientId, e);
        }
    }
    /**
     * 获取Client对应的ChatModel
     */
    private StreamingChatModel getChatModelForClient() {
        if (modelId == null) {
            log.warn("ClientExecuteHandler没有存储modelId: clientId={}", clientId);
            return null;
        }
        try {
            StreamingChatModel chatModel = beanUtil.getChatModel(modelId);
            if (chatModel == null) {
                log.warn("ChatModel不存在: modelId={}, clientId={}", modelId, clientId);
            }
            return chatModel;
        } catch (Exception e) {
            log.error("获取ChatModel失败: modelId={}, clientId={}, error={}", modelId, clientId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 流式路由到下一个Handler（只在 complete 中调用）
     */
    private void routerStream(ExecuteCommandEntity command, ExecuteContext context, FluxSink<String> emitter) {
        if (nextHandler == null) return;
        nextHandler.handle(command, context, emitter);
    }

    @Override
    public ClientExecuteHandler getNextHandler() {
        return nextHandler;
    }

    // -------------------- 工具方法：事件构造 / 容错包装 --------------------

    private String buildDeltaEvent(String token) {
        // {"type":"delta","reply_delta":"..."}  —— 前端直接 append 即可
        return JsonUtil.toJson(Map.of(
                "type", "delta",
                "reply_delta", token
        ));
    }

    private String buildFinalEventJson(AgentMessage msg) throws JsonProcessingException {
        // {"type":"final","message":{...完整AgentMessage...}}
        String messageJson = AgentMessageParser.toJson(msg);
        return "{\"type\":\"final\",\"message\":" + messageJson + "}";
    }

    private String buildErrorFinalEvent(String partialReply, String errorMsg) {
        try {
            AgentMessage err = new AgentMessage();
            err.setStep(safeStep(partialReply));
            err.setAction(AgentMessage.Action.final_);
            err.setStatus(AgentMessage.Status.failed);
            err.setAnswer(partialReply == null ? "" : partialReply);
            err.setReason("处理失败: " + (errorMsg != null ? errorMsg : "unknown"));

            AgentMessage.Metadata metadata = new AgentMessage.Metadata();
            metadata.setTask_type("error");
            metadata.setPriority(AgentMessage.Priority.P0);
            err.setMetadata(metadata);

            String messageJson = AgentMessageParser.toJson(err);
            return "{\"type\":\"final\",\"message\":" + messageJson + ",\"error\":" + JsonUtil.toJson(errorMsg) + "}";
        } catch (Exception ex) {
            // 兜底：最简错误事件
            return JsonUtil.toJson(Map.of(
                    "type", "final",
                    "error", errorMsg != null ? errorMsg : "unknown"
            ));
        }
    }

    private int safeStep(String partialReply) {
        // 若 context 中没有 step 管理，这里兜底给 1；你也可以改成 context.getStep()
        return 1;
    }

    private AgentMessage wrapPlainTextAsAgentMessage(ExecuteContext context, String text) {
        AgentMessage msg = new AgentMessage();
        int step = context.getStep() > 0 ? context.getStep() : 1;
        msg.setStep(step);
        msg.setAction(AgentMessage.Action.execute);
        msg.setStatus(AgentMessage.Status.running);
        msg.setAnswer(text == null ? "" : text);
        msg.setReason("从纯文本包装的消息");

        AgentMessage.Metadata metadata = new AgentMessage.Metadata();
        metadata.setTask_type("text_wrapper");
        metadata.setPriority(AgentMessage.Priority.P2);
        msg.setMetadata(metadata);
        return msg;
    }

    // 你可以把 JsonUtil 放到公共包
    static final class JsonUtil {
        private static final com.fasterxml.jackson.databind.ObjectMapper M = new com.fasterxml.jackson.databind.ObjectMapper()
                .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL);

        static String toJson(Object o) {
            try {
                return M.writeValueAsString(o);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
