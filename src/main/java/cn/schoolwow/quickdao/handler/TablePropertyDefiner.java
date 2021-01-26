package cn.schoolwow.quickdao.handler;

import cn.schoolwow.quickdao.annotation.IdStrategy;
import cn.schoolwow.quickdao.annotation.IndexType;

/**
 * 自定义列属性
 */
public interface TablePropertyDefiner {
    /**
     * 是否id属性
     */
    TablePropertyDefiner id(boolean id);

    /**
     * 指定id生成策略
     */
    TablePropertyDefiner strategy(IdStrategy idStrategy);

    /**
     * 类型
     */
    TablePropertyDefiner columnType(String columnType);

    /**
     * 列名
     */
    TablePropertyDefiner columnName(String columnName);

    /**
     * 注释
     */
    TablePropertyDefiner comment(String comment);

    /**
     * 是否非空
     */
    TablePropertyDefiner notNull(boolean notNull);

    /**
     * 是否主键
     */
    TablePropertyDefiner primaryKey(boolean primaryKey);

    /**
     * check约束
     */
    TablePropertyDefiner check(String check);

    /**
     * 默认值
     */
    TablePropertyDefiner defaultValue(String defaultValue);

    /**
     * 建立索引
     * @param indexType 索引类型
     * @param indexName 索引名称(可为空)
     * @param using 索引方法(可为空)
     * @param comment 索引注释(可为空)
     */
    TablePropertyDefiner index(IndexType indexType, String indexName, String using, String comment);

    /**
     * 结束
     */
    TableDefiner done();
}
