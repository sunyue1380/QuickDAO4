package cn.schoolwow.quickdao.annotation;

import java.lang.annotation.*;

/**
 * 组合索引
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(CompositeIndexes.class)
public @interface CompositeIndex {
    /**
     * 索引字段
     */
    String[] columns();

    /**
     * 索引类型
     */
    IndexType indexType() default IndexType.NORMAL;

    /**
     * 索引名称
     */
    String indexName() default "";

    /**
     * 索引方法
     */
    String using() default "";

    /**
     * 索引注释
     */
    String comment() default "";
}
