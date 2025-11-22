package com.example.ddd.infrastructure.dao.typehandler;

import com.example.ddd.application.agent.service.execute.role.AgentRole;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * AgentRole枚举类型处理器
 * 用于MyBatis中AgentRole枚举与数据库INTEGER类型之间的转换
 */
@MappedTypes(AgentRole.class)
@MappedJdbcTypes(JdbcType.INTEGER)
public class AgentRoleTypeHandler extends BaseTypeHandler<AgentRole> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, AgentRole parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getCode());
    }

    @Override
    public AgentRole getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Integer code = rs.getInt(columnName);
        if (rs.wasNull()) {
            return null;
        }
        return AgentRole.by(code);
    }

    @Override
    public AgentRole getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Integer code = rs.getInt(columnIndex);
        if (rs.wasNull()) {
            return null;
        }
        return AgentRole.by(code);
    }

    @Override
    public AgentRole getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Integer code = cs.getInt(columnIndex);
        if (cs.wasNull()) {
            return null;
        }
        return AgentRole.by(code);
    }
}

