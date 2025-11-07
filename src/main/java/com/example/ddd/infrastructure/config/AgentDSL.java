package com.example.ddd.infrastructure.config;

import org.jooq.ConnectionProvider;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDSLContext;

import java.sql.Connection;

public class AgentDSL extends DefaultDSLContext {
    public AgentDSL(SQLDialect dialect) {
        super(dialect);
    }

    public AgentDSL(Connection connection, SQLDialect dialect) {
        super(connection, dialect);
    }

    public AgentDSL(ConnectionProvider connectionProvider, SQLDialect dialect) {
        super(connectionProvider, dialect);
    }

    public void execute(DSLContextFactory.ExecuteOperation executeOperation) {
        try {
            this.transaction((configuration -> {
                executeOperation.execute(DSL.using(configuration));
            }));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T callable(DSLContextFactory.CallableOperation<T> executeOperation) {
        try {
            return this.transactionResult((configuration -> executeOperation.callable(DSL.using(configuration))));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
