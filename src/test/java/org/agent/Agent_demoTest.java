package org.agent;

import com.example.ddd.domain.agent.model.entity.ArmoryCommandEntity;
import com.example.ddd.domain.agent.service.armory.DynamicContext;
import com.example.ddd.domain.agent.service.armory.RootNode;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@MicronautTest
class Agent_demoTest {
    @Inject
    RootNode rootNode;

    @Test
    void testItWorks() {

        ArmoryCommandEntity command = new ArmoryCommandEntity();
        command.setOrchestratorId(1L);
        String handle = rootNode.handle(command, new DynamicContext());
        System.out.println(handle);
    }

}
