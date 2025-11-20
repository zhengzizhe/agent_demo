package com.example.ddd.infrastructure.config;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;

/**
 * Agent Neo4j Driver包装类
 * 提供事务管理和便捷的操作方法
 */
public class AgentNeo4jDriver implements AutoCloseable {
    private final Driver driver;

    public AgentNeo4jDriver(Driver driver) {
        this.driver = driver;
    }

    /**
     * 执行操作（无返回值）
     */
    public void execute(Neo4jDriverFactory.ExecuteOperation executeOperation) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                try {
                    executeOperation.execute(tx);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return null;
            });
        } catch (Exception e) {
            throw new RuntimeException("Neo4j执行操作失败", e);
        }
    }

    /**
     * 执行操作（有返回值）
     */
    public <T> T callable(Neo4jDriverFactory.CallableOperation<T> callableOperation) {
        try (Session session = driver.session()) {
            return session.executeWrite(tx -> {
                try {
                    return callableOperation.callable(tx);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Neo4j执行操作失败", e);
        }
    }

    /**
     * 执行读操作（有返回值）
     */
    public <T> T callableRead(Neo4jDriverFactory.CallableOperation<T> callableOperation) {
        try (Session session = driver.session()) {
            return session.executeRead(tx -> {
                try {
                    return callableOperation.callable(tx);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Neo4j执行读操作失败", e);
        }
    }

    /**
     * 获取原生Driver（用于高级操作）
     */
    public Driver getDriver() {
        return driver;
    }

    /**
     * 关闭Driver连接
     */
    @Override
    public void close() {
        // Driver是单例，不应该在这里关闭
        // 实际的关闭由Neo4jDriverFactory的@PreDestroy处理
    }
}

