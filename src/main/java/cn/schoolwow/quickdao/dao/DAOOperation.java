package cn.schoolwow.quickdao.dao;

import cn.schoolwow.quickdao.domain.*;
import cn.schoolwow.quickdao.transaction.Transaction;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

public interface DAOOperation {
    /**
     * 添加过滤器
     */
    void interceptor(Interceptor interceptor);

    /**
     * 开启事务
     */
    Transaction startTransaction();

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
     * @param tableName 表名
     * @param column    字段名称
     */
    Property getProperty(String tableName, String column);

    /**
     * 获取表字段列表
     *
     * @param tableName 表名
     */
    List<Property> getPropertyList(String tableName);

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
