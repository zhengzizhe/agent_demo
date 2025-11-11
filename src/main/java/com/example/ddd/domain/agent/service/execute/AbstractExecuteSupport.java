package com.example.ddd.domain.agent.service.execute;

import com.example.ddd.common.utils.AbstractStrategyILogicRoot;
import reactor.core.publisher.FluxSink;

public abstract class AbstractExecuteSupport extends AbstractStrategyILogicRoot<ExecuteCommandEntity, ExecuteContext, String> {
    @Override
    public String handle(ExecuteCommandEntity executeCommandEntity, ExecuteContext executeContext) {
        return "";
    }

    public abstract void handle(ExecuteCommandEntity command, ExecuteContext context, FluxSink<String> emitter);
}
