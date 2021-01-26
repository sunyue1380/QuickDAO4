package cn.schoolwow.quickdao.domain;

import cn.schoolwow.quickdao.annotation.IndexType;

import java.util.ArrayList;
import java.util.List;

public class IndexField {
    /**
     * 表名
     * */
    public String tableName;

    /**
     * 索引类型
     * */
    public IndexType indexType;

    /**
     * 索引名称
     * */
    public String indexName;

    /**
     * 索引方法
     * */
    public String using;

    /**
     * 索引注释
     * */
    public String comment;

    /**
     * 索引字段
     * */
    public List<String> columns = new ArrayList<>();
}
