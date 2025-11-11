package com.example.ddd.domain.agent.service.execute;

import com.example.ddd.common.utils.BeanUtil;
import com.example.ddd.configuration.config.ChatApi;
import com.example.ddd.domain.agent.model.entity.ClientEntity;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.TokenStream;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.FluxSink;

import static com.example.ddd.common.constant.IAgentConstant.COLON;

/**
 * Client执行处理器
 * 负责调用ChatApi处理消息
 */
@Slf4j
public class ClientExecuteHandler extends AbstractExecuteSupport {

    private final BeanUtil beanUtil;
    private final Long clientId;
    private final Long modelId; // 存储第一个modelId，用于获取ChatModel
    private final ClientExecuteHandler nextHandler;

    public ClientExecuteHandler(BeanUtil beanUtil, ClientEntity clientEntity, Long modelId, ClientExecuteHandler nextHandler) {
        this.beanUtil = beanUtil;
        this.clientId = clientEntity.getId();
        this.modelId = modelId;
        this.nextHandler = nextHandler;
    }

    public void handle(ExecuteCommandEntity command, ExecuteContext context, FluxSink<String> emitter) {
        log.info("开始流式处理Client: clientId={}, message={}", clientId, command.getMessage());
        try {
            StreamingChatModel chatModel = getChatModelForClient();
            if (chatModel == null) {
                log.warn("ChatModel不存在: clientId={}", clientId);
                routerStream(command, context, emitter);
                return;
            }
            String inputMessage = context.getResult() != null
                    ? context.getResult()
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
                    // 记录token消耗
                    if (e.tokenUsage() != null) {
                        log.info("Client执行Token消耗 [clientId={}] - inputTokens={}, outputTokens={}, totalTokens={}",
                                clientId,
                                e.tokenUsage().inputTokenCount(),
                                e.tokenUsage().outputTokenCount(),
                                e.tokenUsage().totalTokenCount());
                    } else {
                        log.warn("Client执行Token消耗信息不可用 [clientId={}]", clientId);
                    }
                    log.info("组装最终消息: clientId={}, step={}, text={}",
                            clientId,
                            1,
                            e);
                    context.setResult(e.aiMessage().text());
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
        if (nextHandler == null) {
            // 最后一个handler完成，发送结束标记
            if (!emitter.isCancelled()) {
                emitter.next("[DONE]");
            }
            return;
        }
        nextHandler.handle(command, context, emitter);
    }

    @Override
    public ClientExecuteHandler getNextHandler() {
        return nextHandler;
    }


    private int safeStep(String partialReply) {
        // 若 context 中没有 step 管理，这里兜底给 1；你也可以改成 context.getStep()
        return 1;
    }
}
