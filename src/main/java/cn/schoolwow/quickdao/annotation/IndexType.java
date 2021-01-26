package cn.schoolwow.quickdao.annotation;

/**
 * 索引类型
 */
public enum IndexType {
    /**
     * 普通索引
     */
    NORMAL,

    /**
     * 唯一索引
     */
    UNIQUE,

    /**
     * 全文索引
     */
    FULLTEXT;
}
