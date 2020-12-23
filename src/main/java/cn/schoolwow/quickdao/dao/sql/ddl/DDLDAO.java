package cn.schoolwow.quickdao.dao.sql.ddl;

import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.Property;

/**
 * 负责数据表和字段修改
 * */
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
    void drop(Class clazz);

    /**
     * 删表
     */
    void drop(String tableName);

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
     * @param tableName 表名
     * @param property 字段属性
     */
    void createColumn(String tableName, Property property);

    /**
     * 删除列
     * @param tableName 表名
     * @param column 列名
     */
    Property dropColumn(String tableName, String column);

    /**
     * 双向同步扫描实体类信息和数据库表信息
     */
    void syncEntityList();

    /**
     * 自动建表和新增字段
     * */
    void automaticCreateTableAndField();

    /**
     * 重新获取数据库信息
     */
    void refreshDbEntityList();
}
