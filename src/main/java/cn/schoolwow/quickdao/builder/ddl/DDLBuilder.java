package cn.schoolwow.quickdao.builder.ddl;

import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.IndexField;
import cn.schoolwow.quickdao.domain.Property;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

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
     * 判断索引是否存在
     * @param tableName 表名
     * @param indexName 索引名称
     * */
    boolean hasIndexExists(String tableName, String indexName) throws SQLException;

    /**
     * 判断约束是否存在
     * @param tableName 表名
     * @param constraintName 约束名称
     * */
    boolean hasConstraintExists(String tableName, String constraintName) throws SQLException;

    /**
     * 创建索引
     * @param indexField 索引字段
     */
    void createIndex(IndexField indexField) throws SQLException;

    /**
     * 删除索引
     * @param tableName 表名
     * @param indexName 索引名称
     */
    void dropIndex(String tableName, String indexName) throws SQLException;

    /**
     * 建立外键约束
     */
    void createForeignKey(Property property) throws SQLException;

    /**
     * 自动建表和新增字段
     */
    void automaticCreateTableAndColumn() throws SQLException;

    /**
     * 刷新数据库字段信息
     */
    void refreshDbEntityList() throws SQLException;

    /**
     * 获取默认Java类型与数据库类型映射关系表
     */
    Map<String,String> getTypeFieldMapping();
}
