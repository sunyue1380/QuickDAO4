package cn.schoolwow.quickdao.handler;

import cn.schoolwow.quickdao.QuickDAO;
import cn.schoolwow.quickdao.domain.IndexField;

/**
 * 自定义表
 */
public interface TableDefiner {
    /**
     * 映射表名
     */
    TableDefiner tableName(String tableName);

    /**
     * 注释
     */
    TableDefiner comment(String comment);

    /**
     * 建立索引
     */
    TableDefiner index(IndexField indexField);

    /**
     * 处理列
     */
    TablePropertyDefiner property(String fieldName);

    /**
     * 结束
     */
    QuickDAO done();
}
