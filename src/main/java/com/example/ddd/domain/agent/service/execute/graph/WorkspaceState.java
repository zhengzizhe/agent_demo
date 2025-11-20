package com.example.ddd.domain.agent.service.execute.graph;

import dev.langchain4j.data.message.ChatMessage;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.Map;
import java.util.Optional;

public class WorkspaceState extends MessagesState<ChatMessage> {

    public WorkspaceState(Map<String, Object> initData) {
        super(initData);
    }

    public Optional<String> userMessage() {
        return this.value("userMessage");
    }

    // 临时数据，例如当前处理的 todoId 等
    public Optional<Map<String, Object>> scratchpad() {
        return this.value("scratchpad");
    }
}
