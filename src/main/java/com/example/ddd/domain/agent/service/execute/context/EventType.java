package com.example.ddd.domain.agent.service.execute.context;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 事件类型枚举
 */
public enum EventType {
    /**
     * 规划开始
     */
    PLANNING_START("planning_start", 1001),
    
    /**
     * 规划运行中
     */
    PLANNING_RUNNING("planning_running", 1002),
    
    /**
     * 规划完成
     */
    PLANNING_COMPLETE("planning_complete", 1003),
    
    /**
     * 计划就绪
     */
    PLAN_READY("plan_ready", 1004),
    
    /**
     * 规划失败
     */
    PLANNING_FAILED("planning_failed", 1005),
    
    /**
     * 计划解析失败
     */
    PLAN_PARSE_FAILED("plan_parse_failed", 1006),
    
    /**
     * 任务开始
     */
    TASK_START("task_start", 2001),
    
    /**
     * 任务运行中
     */
    TASK_RUNNING("task_running", 2002),
    
    /**
     * 任务完成
     */
    TASK_COMPLETE("task_complete", 2003),
    
    /**
     * 任务失败
     */
    TASK_FAILED("task_failed", 2004),
    
    /**
     * 流式输出
     */
    STREAMING("streaming", 3001),
    
    /**
     * 执行失败
     */
    EXECUTION_FAILED("execution_failed", 4001),
    
    /**
     * 错误
     */
    ERROR("error", 5001);

    private final String value;
    private final int code;

    EventType(String value, int code) {
        this.value = value;
        this.code = code;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * 获取事件类型代码
     */
    public int getCode() {
        return code;
    }

    /**
     * 根据字符串值获取枚举
     */
    @JsonCreator
    public static EventType fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (EventType type : EventType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据代码获取枚举
     */
    public static EventType fromCode(int code) {
        for (EventType type : EventType.values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}

