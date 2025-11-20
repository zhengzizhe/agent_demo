package com.example.ddd.infrastructure.config;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.TransactionContext;

/**
 * Neo4j Driver工厂类
 * 负责创建和管理Neo4j Driver实例
 */
@Factory
public class Neo4jDriverFactory {

    @Value("${neo4j.uri:bolt://localhost:7687}")
    private String uri;

    @Value("${neo4j.username:neo4j}")
    private String username;

    @Value("${neo4j.password:neo4j123}")
    private String password;

    private Driver driver;

    /**
     * 创建Neo4j Driver实例
     */
    @Bean
    @Singleton
    public Driver createDriver() {
        if (driver == null) {
            driver = GraphDatabase.driver(
                    uri,
                    AuthTokens.basic(username, password)
            );
            // 验证连接
            try (var session = driver.session()) {
                session.run("RETURN 1").consume();
            }
        }
        return driver;
    }

    /**
     * 创建AgentNeo4jDriver包装实例
     */
    public AgentNeo4jDriver createAgentDriver() {
        return new AgentNeo4jDriver(createDriver());
    }

    /**
     * 执行操作（无返回值）
     */
    public void execute(ExecuteOperation executeOperation) {
        try (AgentNeo4jDriver agentDriver = createAgentDriver()) {
            agentDriver.execute(executeOperation);
        }
    }

    /**
     * 执行操作（有返回值）
     */
    public <T> T callable(CallableOperation<T> callableOperation) {
        try (AgentNeo4jDriver agentDriver = createAgentDriver()) {
            return agentDriver.callable(callableOperation);
        }
    }

    /**
     * 执行读操作（有返回值）
     */
    public <T> T callableRead(CallableOperation<T> callableOperation) {
        try (AgentNeo4jDriver agentDriver = createAgentDriver()) {
            return agentDriver.callableRead(callableOperation);
        }
    }

    /**
     * 关闭Driver连接
     */
    @PreDestroy
    public void close() {
        if (driver != null) {
            driver.close();
        }
    }

    /**
     * 执行操作接口（无返回值）
     */
    @FunctionalInterface
    public interface ExecuteOperation {
        void execute(TransactionContext tx) throws Exception;
    }

    /**
     * 执行操作接口（有返回值）
     */
    @FunctionalInterface
    public interface CallableOperation<T> {
        T callable(TransactionContext tx) throws Exception;
    }
}

