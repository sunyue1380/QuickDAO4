package cn.schoolwow.quickdao.dao.sql.dql;

import cn.schoolwow.quickdao.domain.SFunction;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * 负责数据简单查询操作
 */
public interface DQLDAO {
    /**
     * 根据id查询实例
     *
     * @param clazz 实例类对象
     * @param id    待查询id值
     */
    <T> T fetch(Class<T> clazz, long id);

    /**
     * 根据属性查询单个记录
     *
     * @param clazz 实例类对象
     * @param field 指定字段名
     * @param value 指字段值
     */
    <T> T fetch(Class<T> clazz, String field, Object value);

    /**
     * 根据属性查询多个记录
     *
     * @param clazz 实例类对象
     * @param field 指定字段名
     * @param value 指字段值
     */
    <T> List<T> fetchList(Class<T> clazz, String field, Object value);

    /**
     * 根据属性查询单个记录
     *
     * @param clazz 实例类对象
     * @param field 指定字段名
     * @param value 指字段值
     */
    <T> T fetch(Class<T> clazz, SFunction<T,?> field, Object value);

    /**
     * 根据属性查询多个记录
     *
     * @param clazz 实例类对象
     * @param field 指定字段名
     * @param value 指字段值
     */
    <T> List<T> fetchList(Class<T> clazz, SFunction<T,?> field, Object value);

    /**
     * 根据属性查询单个记录
     *
     * @param tableName 表名
     * @param field     指定字段名
     * @param value     指字段值
     */
    JSONObject fetch(String tableName, String field, Object value);

    /**
     * 根据属性查询多个记录
     *
     * @param tableName 表名
     * @param field     指定字段名
     * @param value     指字段值
     */
    JSONArray fetchList(String tableName, String field, Object value);

    /**
     * 执行原生查询语句
     * @param selectSQL SQL查询语句
     * @param parameters 参数
     */
    JSONArray rawSelect(String selectSQL, Object... parameters);
}
