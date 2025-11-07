package com.example.ddd.domain.agent.service.armory;

import com.example.ddd.common.utils.AbstractStrategyILogicRoot;
import com.example.ddd.domain.agent.model.entity.ArmoryCommandEntity;
import jakarta.inject.Inject;

import java.util.concurrent.ThreadPoolExecutor;


public abstract class AbstractArmorySupport extends AbstractStrategyILogicRoot<ArmoryCommandEntity, DynamicContext, String> {

}
