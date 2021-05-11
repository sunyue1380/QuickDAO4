package cn.schoolwow.quickdao.builder;

import cn.schoolwow.quickdao.domain.ConnectionExecutorItem;

/**
 * 公共SQLBuilder
 */
public interface SQLBuilder {
    /**
     * 根据唯一性约束查询
     */
    ConnectionExecutorItem selectCountById(Object instance) throws Exception;

    /**
     * 根据唯一性约束查询
     */
    ConnectionExecutorItem selectCountByUniqueKey(Object instance) throws Exception;
}
