package com.example.ddd.infrastructure.config;

import io.micronaut.data.connection.annotation.Connectable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;

import javax.sql.DataSource;
import java.sql.Connection;

@Singleton
public class DSLContextFactory {
    public final SQLDialect sqlDialect = SQLDialect.POSTGRES;
    @Inject
    private DataSource dataSource;

    public AgentDSL createDsl() {
        try {
            // 这里获取的 connection 是 Micronaut 的代理连接（ContextualConnection）
            // 它需要在 @Connectable 上下文中才能正常工作
            Connection connection = dataSource.getConnection();
            return new AgentDSL(connection, sqlDialect);
        } catch (Exception e) {
            throw new RuntimeException("创建DSLContext失败", e);
        }
    }

    @Connectable
    public void execute(ExecuteOperation executeOperation) {
        AgentDSL dsl = createDsl();
        try {
            dsl.execute(executeOperation);
        } finally {
            closeConnection(dsl);
        }
    }

    @Connectable
    public <T> T callable(CallableOperation<T> executeOperation) {
        AgentDSL dsl = createDsl();
        try {
            return dsl.callable(executeOperation);
        } finally {
            closeConnection(dsl);
        }
    }

    private void closeConnection(AgentDSL dsl) {
        if (dsl != null) {
            try {
                Connection connection = dsl.configuration().connectionProvider().acquire();
                if (connection != null && !connection.isClosed()) {
                    dsl.configuration().connectionProvider().release(connection);
                    connection.close();
                }
            } catch (Exception e) {
                // 忽略关闭连接时的异常
            }
        }
    }

    @FunctionalInterface
    public interface CallableOperation<T> {
        T callable(DSLContext dslContext) throws Exception;
    }

    @FunctionalInterface
    public interface ExecuteOperation {
        void execute(DSLContext dslContext) throws Exception;
    }
}
