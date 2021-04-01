package cn.schoolwow.quickdao.dao.sql.ddl;

import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.IndexField;
import cn.schoolwow.quickdao.domain.Property;

import java.util.Map;

/**
 * 负责数据表和字段修改
 */
public interface DDLDAO {
    /**
     * 建表
     */
    void create(Class clazz);

    /**
     * 建表
     */
    void create(Entity entity);

    /**
     * 删表
     */
    void dropTable(Class clazz);

    /**
     * 删表
     */
    void dropTable(String tableName);

    /**
     * 重建表
     */
    void rebuild(Class clazz);

    /**
     * 重建表
     */
    void rebuild(String tableName);

    /**
     * 新增列
     *
     * @param tableName 表名
     * @param property  字段属性
     */
    void createColumn(String tableName, Property property);

    /**
     * 删除列
     *
     * @param tableName 表名
     * @param column    列名
     */
    Property dropColumn(String tableName, String column);

    /**
     * 索引是否存在
     * @param tableName 表名
     * @param indexName 索引名称
     */
    boolean hasIndex(String tableName, String indexName);

    /**
     * 新增索引
     * @param indexField 索引信息
     */
    void createIndex(IndexField indexField);

    /**
     * 删除索引
     * @param tableName 表名
     * @param indexName 索引名称
     */
    void dropIndex(String tableName, String indexName);

    /**
     * 是否开启外键约束检查
     */
    void enableForeignConstraintCheck(boolean enable);

    /**
     * 获取Java类型到数据库字段类型映射表
     */
    Map<String,String> getTypeFieldMapping();

    /**
     * 双向同步扫描实体类信息和数据库表信息
     */
    void syncEntityList();

    /**
     * 自动建表和新增字段
     */
    void automaticCreateTableAndColumn();

    /**
     * 重新获取数据库信息
     */
    void refreshDbEntityList();
}
