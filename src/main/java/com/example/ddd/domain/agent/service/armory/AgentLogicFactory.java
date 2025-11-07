package com.example.ddd.domain.agent.service.armory;

import com.example.ddd.common.utils.ILogicHandler;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class AgentLogicFactory {
    @Inject
    RootNode rootNode;

    public ILogicHandler rootNode() {
        return rootNode;
    }


}
