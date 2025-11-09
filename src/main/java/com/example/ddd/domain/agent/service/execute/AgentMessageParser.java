package com.example.ddd.domain.agent.service.execute;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import java.util.*;
import java.util.stream.Collectors;

public final class AgentMessageParser {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private AgentMessageParser() {}

    // 反序列化
    public static AgentMessage fromJson(String json) throws JsonProcessingException {
        try {
            AgentMessage msg = MAPPER.readValue(json, AgentMessage.class);
            validate(msg);
            return msg;
        } catch (MismatchedInputException mie) {
            throw new JsonProcessingException("JSON 结构与 AgentMessage 不匹配: " + mie.getOriginalMessage()){};
        }
    }

    // 序列化
    public static String toJson(AgentMessage msg) throws JsonProcessingException {
        validate(msg);
        return MAPPER.writeValueAsString(msg);
    }

    // 基本校验（必要字段）
    public static void validate(AgentMessage msg) {
        List<String> errors = new ArrayList<>();
        if (msg == null) {
            errors.add("消息为空");
        } else {
            // 校验 action 字段
            if (msg.getAction() == null) {
                errors.add("action 不能为空");
            }
            
            // 校验 confidence 字段（如果存在，应在 0.0-1.0 范围内）
            if (msg.getConfidence() != null) {
                double conf = msg.getConfidence();
                if (conf < 0.0 || conf > 1.0) {
                    errors.add("confidence 必须在 0.0-1.0 范围内");
                }
            }
            
            // 当 action=final 时，answer 应该存在
            if (msg.getAction() == AgentMessage.Action.final_ && isEmpty(msg.getAnswer())) {
                errors.add("当 action=final 时，answer 不能为空");
            }
            
            // 校验 status 字段
            if (msg.getStatus() == null) {
                errors.add("status 不能为空");
            }
            
            // 校验 metadata 中的 priority（如果存在）
            if (msg.getMetadata() != null && msg.getMetadata().getPriority() == null) {
                // priority 是可选的，但如果 metadata 存在，建议设置 priority
                // 这里不强制要求，只做警告
            }
        }
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("AgentMessage 校验失败: " + String.join("; ", errors));
        }
    }

    // 累加拼接多个 Agent 的 answer（无缝串起来）
    public static String mergeReplies(List<AgentMessage> messages) {
        if (messages == null || messages.isEmpty()) return "";
        return messages.stream()
                .map(AgentMessage::getAnswer)
                .filter(Objects::nonNull)
                .collect(Collectors.joining());
    }

    // 兼容旧代码：累加拼接多个 Agent 的 reply
    @Deprecated
    public static String mergeAnswers(List<AgentMessage> messages) {
        return mergeReplies(messages);
    }

    // 是否已完成（简便方法）
    public static boolean isFinal(AgentMessage msg) {
        return msg != null && 
               (msg.getAction() == AgentMessage.Action.final_ || 
                msg.getStatus() == AgentMessage.Status.completed);
    }

    // 根据 action 判断下一步操作
    public static NextAction parseNext(AgentMessage msg) {
        if (msg == null) {
            return NextAction.stop();
        }
        
        // 根据 action 和 status 判断下一步
        if (msg.getAction() == AgentMessage.Action.final_ || 
            msg.getStatus() == AgentMessage.Status.completed) {
            return NextAction.stop();
        }
        
        if (msg.getAction() == AgentMessage.Action.execute) {
            return NextAction.continueSame();
        }
        
        // 其他情况默认继续
        return NextAction.continueSame();
    }

    // 从 needs 字段提取需要的资源
    public static List<String> getNeeds(AgentMessage msg) {
        if (msg == null || msg.getNeeds() == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(msg.getNeeds());
    }

    // 从 plan 字段获取执行计划
    public static List<String> getPlan(AgentMessage msg) {
        if (msg == null || msg.getPlan() == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(msg.getPlan());
    }

    // --- 工具: 深拷贝（通过 JSON）
    private static AgentMessage deepCopy(AgentMessage original) {
        try {
            String json = MAPPER.writeValueAsString(original);
            return MAPPER.readValue(json, AgentMessage.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("AgentMessage 深拷贝失败", e);
        }
    }

    private static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    // 路由解析结果
    public static final class NextAction {
        public enum Type { CONTINUE, STOP, ROUTE }
        public final Type type;
        public final String routeAgentId;

        private NextAction(Type type, String routeAgentId) {
            this.type = type;
            this.routeAgentId = routeAgentId;
        }
        public static NextAction continueSame() { return new NextAction(Type.CONTINUE, null); }
        public static NextAction stop() { return new NextAction(Type.STOP, null); }
        public static NextAction routeTo(String id) { return new NextAction(Type.ROUTE, id); }

        @Override public String toString() {
            return "NextAction{type=" + type + ", routeAgentId='" + routeAgentId + "'}";
        }
    }
}
