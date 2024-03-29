package cn.schoolwow.quickdao.dao;

import cn.schoolwow.quickdao.builder.dcl.DCLBuilder;
import cn.schoolwow.quickdao.builder.ddl.DDLBuilder;
import cn.schoolwow.quickdao.builder.dml.DMLBuilder;
import cn.schoolwow.quickdao.builder.dql.DQLBuilder;
import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.GenerateEntityFileOption;
import cn.schoolwow.quickdao.domain.Property;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import cn.schoolwow.quickdao.transaction.Transaction;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface DAOOperation {
    /**
     * 开启事务
     */
    Transaction startTransaction();

    /**
     * 开启事务
     */
    void startTransaction(Consumer<Transaction> transactionConsumer);

    /**
     * 表是否存在
     *
     * @param entityClass 实体类
     */
    boolean hasTable(Class entityClass);

    /**
     * 表是否存在
     *
     * @param tableName 表名
     */
    boolean hasTable(String tableName);

    /**
     * 字段是否存在
     *
     * @param tableName 表名
     * @param column    字段名称
     */
    boolean hasColumn(String tableName, String column);

    /**
     * 获取连接池
     */
    DataSource getDataSource();

    /**
     * 获取扫描的所有实体类信息
     */
    Map<String, Entity> getEntityMap();

    /**
     * 获取数据库表列表
     */
    List<Entity> getDbEntityList();

    /**
     * 获取实体类表
     * @param clazz 实体类
     */
    Entity getEntity(Class clazz);

    /**
     * 获取数据库表
     * @param tableName 表名
     */
    Entity getDbEntity(String tableName);

    /**
     * 获取表字段
     *
     * @param clazz 实体类
     * @param column    字段名称
     */
    Property getProperty(Class clazz, String column);

    /**
     * 获取表字段
     *
     * @param tableName 表名
     * @param column    字段名称
     */
    Property getProperty(String tableName, String column);

    /**
     * 获取实体类表字段列表
     *
     * @param clazz 实体类
     */
    List<Property> getPropertyList(Class clazz);

    /**
     * 获取表字段列表
     *
     * @param tableName 表名
     */
    List<Property> getPropertyList(String tableName);

    /**
     * 获取DCL语句构造器
     */
    DCLBuilder getDCLBuilder();

    /**
     * 获取DDL语句构造器
     */
    DDLBuilder getDDLBuilder();

    /**
     * 获取DQL语句构造器
     */
    DQLBuilder getDQLBuilder();

    /**
     * 获取DML语句构造器
     */
    DMLBuilder getDMLBuilder();

    /**
     * 获取配置信息
     */
    QuickDAOConfig getQuickDAOConfig();

    /**
     * 生成entity的java文件
     *
     * @param generateEntityFileOption 生成实体类文件选项
     */
    void generateEntityFile(GenerateEntityFileOption generateEntityFileOption);
}
