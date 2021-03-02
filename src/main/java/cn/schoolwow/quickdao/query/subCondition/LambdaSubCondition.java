package cn.schoolwow.quickdao.query.subCondition;

import cn.schoolwow.quickdao.domain.SFunction;

import java.io.Serializable;
import java.util.List;

/**
 * 定义Lambda查询参数接口
 * */
public interface LambdaSubCondition<T,P> extends Serializable {
    /**
     * 添加on查询条件
     */
    LambdaSubCondition<T,P> on(SFunction<P,?> primaryField, SFunction<T,?> joinTableField);

    /**
     * 添加空查询
     *
     * @param field 指明哪个字段为Null
     */
    LambdaSubCondition<T,P> addNullQuery(SFunction<T,?> field);

    /**
     * 添加非空查询
     *
     * @param field 指明哪个字段不为Null
     */
    LambdaSubCondition<T,P> addNotNullQuery(SFunction<T,?> field);

    /**
     * 添加空查询
     *
     * @param field 指明哪个字段不为空字符串
     */
    LambdaSubCondition<T,P> addEmptyQuery(SFunction<T,?> field);

    /**
     * 添加非空查询
     *
     * @param field 指明哪个字段不为空字符串
     */
    LambdaSubCondition<T,P> addNotEmptyQuery(SFunction<T,?> field);

    /**
     * 添加范围查询语句
     *
     * @param field  字段名
     * @param values 指明在该范围内的值
     */
    LambdaSubCondition<T,P> addInQuery(SFunction<T,?> field, Object... values);

    /**
     * 添加范围查询语句
     *
     * @param field  字段名
     * @param values 指明在该范围内的值
     */
    LambdaSubCondition<T,P> addInQuery(SFunction<T,?> field, List values);

    /**
     * 添加范围查询语句
     *
     * @param field  字段名
     * @param values 指明在不该范围内的值
     */
    LambdaSubCondition<T,P> addNotInQuery(SFunction<T,?> field, Object... values);

    /**
     * 添加范围查询语句
     *
     * @param field  字段名
     * @param values 指明在不该范围内的值
     */
    LambdaSubCondition<T,P> addNotInQuery(SFunction<T,?> field, List values);

    /**
     * 添加between语句
     *
     * @param field 字段名
     * @param start 范围开始值
     * @param end   范围结束值
     */
    LambdaSubCondition<T,P> addBetweenQuery(SFunction<T,?> field, Object start, Object end);

    /**
     * 添加Like查询
     *
     * @param field 字段名
     * @param value 字段值
     */
    LambdaSubCondition<T,P> addLikeQuery(SFunction<T,?> field, Object value);

    /**
     * 添加字段查询
     *
     * @param field 字段名
     * @param value 字段值
     */
    LambdaSubCondition<T,P> addQuery(SFunction<T,?> field, Object value);

    /**
     * 添加字段查询
     *
     * @param field    字段名
     * @param operator 操作符,可为<b>></b>,<b>>=</b>,<b>=</b>,<b><</b><b><=</b>
     * @param value    字段值
     */
    LambdaSubCondition<T,P> addQuery(SFunction<T,?> field, String operator, Object value);

    /**
     * 添加自定义字段,具体映射规则请看Condition类的JavaDoc注释
     *
     * @param fields 自定义查询列
     * @see cn.schoolwow.quickdao.query.condition.Condition
     */
    LambdaSubCondition<T,P> addColumn(SFunction<T,?>... fields);

    /**
     * 关联表查询,子表可再次关联子表
     * <p>调用本方法时请先查看Condition类JavaDoc注释和SubCondition类的JavaDoc注释</p>
     *
     * @param clazz          待关联的子表
     * @param primaryField   <b>主表</b>关联字段
     * @param joinTableField <b>子表</b>关联字段
     * @see cn.schoolwow.quickdao.query.condition.Condition
     * @see LambdaSubCondition
     */
    <E> LambdaSubCondition<E,T> joinTable(Class<E> clazz, SFunction<T,?> primaryField, SFunction<E,?> joinTableField);

    /**
     * 关联表查询,子表可再次关联子表
     * <p>调用本方法时请先查看Condition类JavaDoc注释和SubCondition类的JavaDoc注释</p>
     *
     * @param clazz          待关联的子表
     * @param primaryField   <b>主表</b>关联字段
     * @param joinTableField <b>子表</b>关联字段
     * @param compositField  <b>子表</b>实体类成员变量名
     * @see cn.schoolwow.quickdao.query.condition.Condition
     * @see LambdaSubCondition
     */
    <E> LambdaSubCondition<E,T> joinTable(Class<E> clazz, SFunction<T,?> primaryField, SFunction<E,?> joinTableField, SFunction<T,?> compositField);

    /**
     * 添加分组查询
     *
     * @param fields 分组字段
     */
    LambdaSubCondition<T,P> groupBy(SFunction<T,?>... fields);

    /**
     * 设置指定字段排序方式
     *
     * @param field 升序排列字段名
     * @param asc 排序方式,值为asc或者desc
     */
    LambdaSubCondition<T,P> order(SFunction<T,?> field, SFunction<T,?> asc);

    /**
     * 根据指定字段升序排列
     *
     * @param fields 升序排列字段名
     */
    LambdaSubCondition<T,P> orderBy(SFunction<T,?>... fields);

    /**
     * 根据指定字段降序排列
     *
     * @param fields 降序排列字段名
     */
    LambdaSubCondition<T,P> orderByDesc(SFunction<T,?>... fields);

    /**
     * 返回关联查询对象
     */
    SubCondition<T,P> done();
}
