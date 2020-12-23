package cn.schoolwow.quickdao.dao.sql;

import java.util.Collection;

public interface SQLDAO {
    /**
     * 实例对象是否存在
     * @param instance 实例对象
     */
    boolean exist(Object instance);
    /**
     * 是否数据库中存在任意一个示例对象数组内的对象
     * @param instances 实例对象数组
     */
    boolean existAny(Object... instances);
    /**
     * 是否数据库中存在示例对象数组内的所有对象
     * @param instances 实例对象数组
     */
    boolean existAll(Object... instances);
    /**
     * 是否数据库中存在任意一个示例对象数组内的对象
     * @param instances 实例对象数组
     */
    boolean existAny(Collection instances);
    /**
     * 是否数据库中存在示例对象数组内的所有对象
     * @param instances 实例对象数组
     */
    boolean existAll(Collection instances);
}
