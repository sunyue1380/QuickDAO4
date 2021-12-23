package cn.schoolwow.quickdao.builder.dml;

import cn.schoolwow.quickdao.domain.ConnectionExecutorItem;

import java.sql.SQLException;

/**
 * 负责数据库增删改操作
 */
public interface DMLBuilder {
    /**
     * 插入语句
     * @param instanc 实例对象
     */
    ConnectionExecutorItem insert(Object instance) throws Exception;

    /**
     * 批量插入语句(返回自增id)
     * @param instances 实例数组
     */
    ConnectionExecutorItem[] insert(Object[] instances) throws Exception;

    /**
     * 批量插入语句
     * @param instances 实例数组
     */
    ConnectionExecutorItem insertBatch(Object[] instances) throws Exception;

    /**
     * 批量插入语句
     * @param instances 实例数组
     * @param offset 便宜量
     * @param length 长度
     *
     */
    ConnectionExecutorItem insertBatch(Object[] instances, int offset, int length) throws Exception;

    /**
     * 根据唯一性约束更新
     * @param instanc 实例对象
     */
    ConnectionExecutorItem updateByUniqueKey(Object instance) throws Exception;

    /**
     * 根据唯一性约束更新
     * @param instances 实例数组
     */
    ConnectionExecutorItem updateByUniqueKey(Object[] instances) throws Exception;

    /**
     * 根据id更新
     * @param instanc 实例对象
     */
    ConnectionExecutorItem updateById(Object instance) throws Exception;

    /**
     * 根据id更新
     * @param instances 实例数组
     */
    ConnectionExecutorItem updateById(Object[] instances) throws Exception;

    /**
     * 根据唯一性约束删除
     * @param instanc 实例对象
     */
    ConnectionExecutorItem deleteByUniqueKey(Object instance) throws Exception;

    /**
     * 根据唯一性约束删除
     * @param instances 实例数组
     */
    ConnectionExecutorItem deleteByUniqueKey(Object[] instances) throws Exception;

    /**
     * 根据id删除
     * @param instanc 实例对象
     */
    ConnectionExecutorItem deleteById(Object instance) throws Exception;

    /**
     * 根据id删除
     * @param instances 实例数组
     */
    ConnectionExecutorItem deleteById(Object[] instances) throws Exception;

    /**
     * 根据字段值删除
     * @param clazz 实体类
     * @param property 属性名称
     * @param value 属性值
     */
    ConnectionExecutorItem deleteByProperty(Class clazz, String property, Object value) throws SQLException;

    /**
     * 根据字段值删除
     * @param tableName 表名
     * @param property 属性名称
     * @param value 属性值
     */
    ConnectionExecutorItem deleteByProperty(String tableName, String property, Object value) throws SQLException;

    /**
     * 清空表
     * @param tableName 表名称
     */
    ConnectionExecutorItem clear(String tableName) throws SQLException;
}