package cn.schoolwow.quickdao.builder;

import cn.schoolwow.quickdao.domain.ConnectionExecutorItem;

import java.sql.SQLException;

/**
 * 公共SQLBuilder
 */
public interface SQLBuilder {
    /**
     * 根据唯一性约束查询
     */
    ConnectionExecutorItem selectCountById(Object instance) throws Exception;

    /**
     * 根据唯一性约束查询
     */
    ConnectionExecutorItem selectCountByUniqueKey(Object instance) throws Exception;

    /**
     * 用户自定义SQL语句
     * @param sql语句
     * @param parameters 参数
     * */
    ConnectionExecutorItem execute(String sql, Object... parameters) throws SQLException;
}
