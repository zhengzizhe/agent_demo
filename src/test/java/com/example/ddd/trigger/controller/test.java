package com.example.ddd.trigger.controller;

import com.example.ddd.domain.agent.adapter.repository.IOrchestratorRepository;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@MicronautTest
public class test {
    @Inject
    IOrchestratorRepository repository;

    @Test
    public void test() {
        System.out.println(repository);
    }
}
