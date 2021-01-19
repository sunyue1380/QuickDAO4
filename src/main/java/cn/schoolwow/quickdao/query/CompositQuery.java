package cn.schoolwow.quickdao.query;

import cn.schoolwow.quickdao.query.condition.Condition;

/**
 * 复杂查询接口
 */
public interface CompositQuery {
    /**
     * 复杂查询
     *
     * @param clazz 实体类表
     */
    Condition query(Class clazz);

    /**
     * 复杂查询
     *
     * @param tableName 指定表名
     */
    Condition query(String tableName);

    /**
     * 添加FROM子查询
     *
     * @param condition 子查询
     */
    Condition query(Condition condition);
}
