package cn.schoolwow.quickdao.builder.dql;

import cn.schoolwow.quickdao.domain.Query;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 负责SQL简单查询接口
 */
public interface DQLBuilder {
    /**
     * is null查询
     */
    PreparedStatement fetchNull(Class clazz, String field) throws SQLException;

    /**
     * 根据id查询
     */
    PreparedStatement fetch(Class clazz, long id) throws SQLException;

    /**
     * 根据字段值查询
     */
    PreparedStatement fetch(Class clazz, String field, Object value) throws SQLException;

    /**
     * is null查询
     */
    PreparedStatement fetchNull(String tableName, String field) throws SQLException;

    /**
     * 根据字段值查询
     */
    PreparedStatement fetch(String tableName, String field, Object value) throws SQLException;

    /**
     * 获取结果集行数
     */
    int getResultSetRowCount(Query query) throws SQLException;

    /**
     * 获取符合条件的总数目
     */
    PreparedStatement count(Query query) throws SQLException;

    /**
     * 插入记录
     */
    PreparedStatement insert(Query query) throws SQLException;

    /**
     * 批量插入记录
     */
    PreparedStatement[] insertArray(Query query) throws SQLException;

    /**
     * 更新符合条件的记录
     */
    PreparedStatement update(Query query) throws SQLException;

    /**
     * 删除符合条件的数据库记录
     */
    PreparedStatement delete(Query query) throws SQLException;

    /**
     * 返回符合条件的数据库记录
     */
    PreparedStatement getArray(Query query) throws SQLException;

    /**
     * 获取query对应的SQL语句
     */
    StringBuilder getArraySQL(Query query);
}
