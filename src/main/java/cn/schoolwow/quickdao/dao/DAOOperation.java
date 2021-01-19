package cn.schoolwow.quickdao.dao;

import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.Interceptor;
import cn.schoolwow.quickdao.domain.Property;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;
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
     * 获取字段类型映射信息
     */
    Map<String, String> getFieldMapping();

    /**
     * 获取配置信息
     */
    QuickDAOConfig getQuickDAOConfig();

    /**
     * 生成entity的java文件
     *
     * @param sourcePath 生成文件夹路径
     */
    void generateEntityFile(String sourcePath);

    /**
     * 生成entity的java文件
     *
     * @param sourcePath 生成文件夹路径
     * @param tableNames 指定需要生成实体类的对应的表名
     */
    void generateEntityFile(String sourcePath, String[] tableNames);
}
