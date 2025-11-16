package com.example.ddd.domain.agent.service.execute.role;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Agent角色枚举
 */
public enum AgentRole {
    /**
     * 主管 / 路由器
     */
    SUPERVISOR(1L, "主管"),
    
    /**
     * 拆解需求、结构化分析
     */
    ANALYST(2L, "分析师"),
    
    /**
     * 工作者
     */
    WORKER(3L, "工作者"),
    
    /**
     * 检索资料（结合 RAG）
     */
    RESEARCHER(4L, "研究员"),
    
    /**
     * 生成自然语言内容（报告、总结、说明）
     */
    WRITER(5L, "写作者"),
    
    /**
     * 检查整体结果、给出修改建议
     */
    REVIEWER(6L, "审阅者");

    /**
     * 角色代码
     */
    private final Long code;

    /**
     * 角色名称
     */
    private final String name;

    /**
     * 构造函数
     *
     * @param code 角色代码
     * @param name 角色名称
     */
    AgentRole(Long code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 获取角色代码
     *
     * @return 角色代码
     */
    public Long getCode() {
        return code;
    }

    /**
     * 获取角色名称
     *
     * @return 角色名称
     */
    public String getName() {
        return name;
    }

    /**
     * 根据角色代码查找对应的枚举值
     *
     * @param code 角色代码
     * @return AgentRole枚举值，如果未找到返回null
     */
    public static AgentRole by(Long code) {
        if (code == null) {
            return null;
        }
        return CODE_MAP.get(code);
    }

    /**
     * 根据角色代码查找对应的枚举值（带默认值）
     *
     * @param code         角色代码
     * @param defaultValue 默认值
     * @return AgentRole枚举值，如果未找到返回默认值
     */
    public static AgentRole by(Long code, AgentRole defaultValue) {
        AgentRole role = by(code);
        return role != null ? role : defaultValue;
    }

    /**
     * 代码到枚举值的映射缓存
     */
    private static final Map<Long, AgentRole> CODE_MAP = Arrays.stream(values())
            .collect(Collectors.toMap(AgentRole::getCode, Function.identity()));
}
