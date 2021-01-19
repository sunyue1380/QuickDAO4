package cn.schoolwow.quickdao.builder.ddl;

import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.IndexType;
import cn.schoolwow.quickdao.domain.Property;

import java.sql.SQLException;
import java.util.List;

/**
 * 负责数据库表和字段管理
 */
public interface DDLBuilder {
    /**
     * 获取数据库名称
     */
    String getDatabaseName() throws SQLException;

    /**
     * 获取数据库信息
     */
    List<Entity> getDatabaseEntity() throws SQLException;

    /**
     * 判断表是否已经存在
     */
    boolean hasTableExists(Entity entity) throws SQLException;

    /**
     * 创建新表
     */
    void createTable(Entity entity) throws SQLException;

    /**
     * 创建新表
     */
    void createProperty(Property property) throws SQLException;

    /**
     * 修改列
     */
    void alterColumn(Property property) throws SQLException;

    /**
     * 删除列
     */
    void dropColumn(Property property) throws SQLException;

    /**
     * 删除表
     */
    void dropTable(String tableName) throws SQLException;

    /**
     * 重建表
     */
    void rebuild(Entity entity) throws SQLException;

    /**
     * 创建索引
     */
    void createIndex(Entity entity, IndexType indexType) throws SQLException;

    /**
     * 删除索引
     */
    void dropIndex(Entity entity, IndexType indexType) throws SQLException;

    /**
     * 建立外键约束
     */
    void createForeignKey(Property property) throws SQLException;

    /**
     * 自动建表和新增字段
     */
    void automaticCreateTableAndField() throws SQLException;

    /**
     * 刷新数据库字段信息
     */
    void refreshDbEntityList() throws SQLException;
}
