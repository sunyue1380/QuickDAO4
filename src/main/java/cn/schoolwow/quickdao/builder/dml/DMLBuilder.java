package cn.schoolwow.quickdao.builder.dml;

import cn.schoolwow.quickdao.domain.ConnectionExecutorItem;

import java.sql.SQLException;

/**
 * 负责数据库增删改操作
 */
public interface DMLBuilder {
    /**
     * 插入语句
     */
    ConnectionExecutorItem insert(Object instance) throws Exception;

    /**
     * 批量插入语句(返回自增id)
     */
    ConnectionExecutorItem[] insert(Object[] instances) throws Exception;

    /**
     * 批量插入语句
     */
    int insertBatch(Object[] instances) throws Exception;

    /**
     * 根据唯一性约束更新
     */
    ConnectionExecutorItem updateByUniqueKey(Object instance) throws Exception;

    /**
     * 根据唯一性约束更新
     */
    ConnectionExecutorItem updateByUniqueKey(Object[] instances) throws Exception;

    /**
     * 根据id更新
     */
    ConnectionExecutorItem updateById(Object instance) throws Exception;

    /**
     * 根据id更新
     */
    ConnectionExecutorItem updateById(Object[] instances) throws Exception;

    /**
     * 根据字段值删除
     */
    ConnectionExecutorItem deleteByProperty(Class clazz, String property, Object value) throws SQLException;

    /**
     * 根据字段值删除
     */
    ConnectionExecutorItem deleteByProperty(String tableName, String property, Object value) throws SQLException;

    /**
     * 清空表
     */
    ConnectionExecutorItem clear(String tableName) throws SQLException;
}
