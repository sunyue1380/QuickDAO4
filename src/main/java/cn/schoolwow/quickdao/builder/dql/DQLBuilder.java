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
     * @param clazz 实体类
     * @param field 字段名称
     */
    ConnectionExecutorItem fetchNull(Class clazz, String field) throws SQLException;

    /**
     * 根据id查询
     * @param clazz 实体类
     * @param id 实体类id值
     */
    ConnectionExecutorItem fetch(Class clazz, long id) throws SQLException;

    /**
     * 根据字段值查询
     * @param clazz 实体类
     * @param field 字段名称
     * @param value 字段值
     */
    ConnectionExecutorItem fetch(Class clazz, String field, Object value) throws SQLException;

    /**
     * is null查询
     * @param tableName 表名
     * @param field 字段名称
     */
    ConnectionExecutorItem fetchNull(String tableName, String field) throws SQLException;

    /**
     * 根据字段值查询
     * @param tableName 表名
     * @param field 字段名称
     * @param value 字段值
     */
    ConnectionExecutorItem fetch(String tableName, String field, Object value) throws SQLException;

    /**
     * 获取结果集行数
     * @param query 查询条件
     */
    int getResultSetRowCount(Query query) throws SQLException;

    /**
     * 获取符合条件的总数目
     * @param query 查询条件
     */
    ConnectionExecutorItem count(Query query) throws SQLException;

    /**
     * 插入记录
     * @param query 查询条件
     */
    ConnectionExecutorItem insert(Query query) throws SQLException;

    /**
     * 批量插入记录
     * @param query 查询条件
     */
    ConnectionExecutorItem[] insertArray(Query query) throws SQLException;

    /**
     * 批量插入记录
     * @param query 查询条件
     */
    ConnectionExecutorItem insertArrayBatch(Query query) throws SQLException;

    /**
     * 批量插入记录
     * @param query 查询条件
     * @param offset 偏移量
     * @param length 长度
     */
    ConnectionExecutorItem insertArrayBatch(Query query, int offset, int length) throws SQLException;

    /**
     * 更新符合条件的记录
     * @param query 查询条件
     */
    ConnectionExecutorItem update(Query query) throws SQLException;

    /**
     * 删除符合条件的数据库记录
     * @param query 查询条件
     */
    ConnectionExecutorItem delete(Query query) throws SQLException;

    /**
     * 返回符合条件的数据库记录
     * @param query 查询条件
     */
    ConnectionExecutorItem getArray(Query query) throws SQLException;

    /**
     * 获取query对应的SQL语句
     * @param query 查询条件
     */
    StringBuilder getArraySQL(Query query);
}