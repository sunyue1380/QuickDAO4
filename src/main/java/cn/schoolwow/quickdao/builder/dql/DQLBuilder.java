package cn.schoolwow.quickdao.builder.dql;

import cn.schoolwow.quickdao.domain.ConnectionExecutorItem;
import cn.schoolwow.quickdao.domain.Query;

import java.sql.SQLException;

/**
 * 负责SQL简单查询接口
 */
public interface DQLBuilder {
    /**
     * is null查询
     */
    ConnectionExecutorItem fetchNull(Class clazz, String field) throws SQLException;

    /**
     * 根据id查询
     */
    ConnectionExecutorItem fetch(Class clazz, long id) throws SQLException;

    /**
     * 根据字段值查询
     */
    ConnectionExecutorItem fetch(Class clazz, String field, Object value) throws SQLException;

    /**
     * is null查询
     */
    ConnectionExecutorItem fetchNull(String tableName, String field) throws SQLException;

    /**
     * 根据字段值查询
     */
    ConnectionExecutorItem fetch(String tableName, String field, Object value) throws SQLException;

    /**
     * 获取结果集行数
     */
    int getResultSetRowCount(Query query) throws SQLException;

    /**
     * 获取符合条件的总数目
     */
    ConnectionExecutorItem count(Query query) throws SQLException;

    /**
     * 插入记录
     */
    ConnectionExecutorItem insert(Query query) throws SQLException;

    /**
     * 批量插入记录
     */
    ConnectionExecutorItem[] insertArray(Query query) throws SQLException;

    /**
     * 批量插入记录
     */
    int insertArrayBatch(Query query) throws SQLException;

    /**
     * 更新符合条件的记录
     */
    ConnectionExecutorItem update(Query query) throws SQLException;

    /**
     * 删除符合条件的数据库记录
     */
    ConnectionExecutorItem delete(Query query) throws SQLException;

    /**
     * 返回符合条件的数据库记录
     */
    ConnectionExecutorItem getArray(Query query) throws SQLException;

    /**
     * 获取query对应的SQL语句
     */
    StringBuilder getArraySQL(Query query);
}
