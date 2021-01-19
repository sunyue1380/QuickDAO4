package cn.schoolwow.quickdao.builder.dml;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 负责数据库增删改操作
 */
public interface DMLBuilder {
    /**
     * 插入语句
     */
    PreparedStatement insert(Object instance) throws Exception;

    /**
     * 批量插入语句(返回自增id)
     */
    PreparedStatement[] insert(Object[] instances) throws Exception;

    /**
     * 批量插入语句
     */
    PreparedStatement insertBatch(Object[] instances) throws Exception;

    /**
     * 根据唯一性约束更新
     */
    PreparedStatement updateByUniqueKey(Object instance) throws Exception;

    /**
     * 根据唯一性约束更新
     */
    PreparedStatement updateByUniqueKey(Object[] instances) throws Exception;

    /**
     * 根据id更新
     */
    PreparedStatement updateById(Object instance) throws Exception;

    /**
     * 根据id更新
     */
    PreparedStatement updateById(Object[] instances) throws Exception;

    /**
     * 根据字段值删除
     */
    PreparedStatement deleteByProperty(Class clazz, String property, Object value) throws SQLException;

    /**
     * 根据字段值删除
     */
    PreparedStatement deleteByProperty(String tableName, String property, Object value) throws SQLException;

    /**
     * 清空表
     */
    PreparedStatement clear(Class clazz) throws SQLException;
}
