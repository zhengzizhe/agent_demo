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

        String handle = rootNode.handle(new ArmoryCommandEntity(1L), new DynamicContext());
        System.out.println(handle);
    }

}
