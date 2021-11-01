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
    void getDatabaseName() throws SQLException;

    /**
     * 获取虚拟表信息
     * */
    List<Entity> getVirtualEntity();

    /**
     * 获取数据库信息
     */
    List<Entity> getDatabaseEntity() throws SQLException;

    /**
     * 判断表是否已经存在
     */
    String hasTableExists(Entity entity);

    /**
     * 创建新表
     */
    String createTable(Entity entity);

    /**
     * 创建字段
     */
    String createProperty(Property property);

    /**
     * 修改列
     */
    String alterColumn(Property property);

    /**
     * 删除列
     */
    String dropColumn(Property property);

    /**
     * 删除表
     */
    String dropTable(String tableName);

    /**
     * 判断索引是否存在
     * @param tableName 表名
     * @param indexName 索引名称
     * */
    String hasIndexExists(String tableName, String indexName) throws SQLException;

    /**
     * 判断约束是否存在
     * @param tableName 表名
     * @param constraintName 约束名称
     * */
    String hasConstraintExists(String tableName, String constraintName) throws SQLException;

    /**
     * 创建索引
     * @param indexField 索引字段
     */
    String createIndex(IndexField indexField);

    /**
     * 删除索引
     * @param tableName 表名
     * @param indexName 索引名称
     */
    String dropIndex(String tableName, String indexName);

    /**
     * 建立外键约束
     */
    String createForeignKey(Property property) throws SQLException;

    /**
     * 是否开启外键约束检查
     */
    void enableForeignConstraintCheck(boolean enable) throws SQLException;

    /**
     * 获取默认Java类型与数据库类型映射关系表
     */
    Map<String,String> getTypeFieldMapping();
}