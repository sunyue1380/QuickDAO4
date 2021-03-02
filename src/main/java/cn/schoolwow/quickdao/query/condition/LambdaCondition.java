package cn.schoolwow.quickdao.query.condition;

import cn.schoolwow.quickdao.domain.SFunction;
import cn.schoolwow.quickdao.query.response.Response;
import cn.schoolwow.quickdao.query.subCondition.LambdaSubCondition;

import java.io.Serializable;
import java.util.Collection;

/**
 * 定义Lambda查询参数接口
 * */
public interface LambdaCondition<T> extends Serializable {
    /**
     * 添加空查询
     *
     * @param field 指明哪个字段为Null
     */
    LambdaCondition<T> addNullQuery(SFunction<T,?> field);

    /**
     * 添加非空查询
     *
     * @param field 指明哪个字段不为Null
     */
    LambdaCondition<T> addNotNullQuery(SFunction<T,?> field);

    /**
     * 添加空查询
     *
     * @param field 指明哪个字段不为空字符串
     */
    LambdaCondition<T> addEmptyQuery(SFunction<T,?> field);

    /**
     * 添加非空查询
     *
     * @param field 指明哪个字段不为空字符串
     */
    LambdaCondition<T> addNotEmptyQuery(SFunction<T,?> field);

    /**
     * 添加范围查询语句
     *
     * @param field   字段名
     * @param inQuery 英文逗号隔开的字段值
     */
    LambdaCondition<T> addInQuery(SFunction<T,?> field, String inQuery);

    /**
     * 添加范围查询语句
     *
     * @param field  字段名
     * @param values 指明在该范围内的值
     */
    LambdaCondition<T> addInQuery(SFunction<T,?> field, Object... values);

    /**
     * 添加范围查询语句
     *
     * @param field  字段名
     * @param values 指明在该范围内的值
     */
    LambdaCondition<T> addInQuery(SFunction<T,?> field, Collection values);

    /**
     * 添加范围查询语句
     *
     * @param field   字段名
     * @param inQuery 英文逗号隔开的字段值
     */
    LambdaCondition<T> addNotInQuery(SFunction<T,?> field, String inQuery);

    /**
     * 添加范围查询语句
     *
     * @param field  字段名
     * @param values 指明在不该范围内的值
     */
    LambdaCondition<T> addNotInQuery(SFunction<T,?> field, Object... values);

    /**
     * 添加范围查询语句
     *
     * @param field  字段名
     * @param values 指明在不该范围内的值
     */
    LambdaCondition<T> addNotInQuery(SFunction<T,?> field, Collection values);

    /**
     * 添加between语句
     *
     * @param field 字段名
     * @param start 范围开始值
     * @param end   范围结束值
     */
    LambdaCondition<T> addBetweenQuery(SFunction<T,?> field, Object start, Object end);

    /**
     * 添加Like查询
     *
     * @param field 字段名
     * @param value 字段值
     */
    LambdaCondition<T> addLikeQuery(SFunction<T,?> field, Object value);

    /**
     * 添加Not Like查询
     *
     * @param field 字段名
     * @param value 字段值
     */
    LambdaCondition<T> addNotLikeQuery(SFunction<T,?> field, Object value);

    /**
     * 添加字段查询
     *
     * @param field 字段名
     * @param value 字段值
     */
    LambdaCondition<T> addQuery(SFunction<T,?> field, Object value);

    /**
     * 添加字段查询
     *
     * @param field    字段名
     * @param operator 操作符,可为<b>></b>,<b>>=</b>,<b>=</b>,<b><</b><b><=</b>
     * @param value    字段值
     */
    LambdaCondition<T> addQuery(SFunction<T,?> field, String operator, Object value);

    /**
     * 添加where子查询
     *
     * @param field    字段名
     * @param operator 操作符,可为<b>></b>,<b>>=</b>,<b>=</b>,<b><</b><b><=</b>
     * @param subQuery 子查询语句
     */
    LambdaCondition<T> addSubQuery(SFunction<T,?> field, String operator, LambdaCondition<T> subQuery);

    /**
     * 添加自定义字段,具体映射规则请看Condition类的JavaDoc注释
     *
     * @param fields 自定义查询列
     * @see LambdaCondition<T>
     */
    LambdaCondition<T> addColumn(SFunction<T,?>... fields);

    /**
     * 添加插入字段,用于{@link cn.schoolwow.quickdao.query.response.Response#insert()}方法
     *
     * @param field 待更新的字段
     * @param value 待更新字段的值
     */
    LambdaCondition<T> addInsert(SFunction<T,?> field, Object value);

    /**
     * 添加更新字段,用于{@link cn.schoolwow.quickdao.query.response.Response#update()}方法
     *
     * @param field 待更新的字段
     * @param value 待更新字段的值
     */
    LambdaCondition<T> addUpdate(SFunction<T,?> field, Object value);

    /**
     * 添加分组查询
     *
     * @param fields 分组字段
     */
    LambdaCondition<T> groupBy(SFunction<T,?>... fields);

    /**
     * 关联表查询
     * <p>调用本方法时请先查看Condition类JavaDoc注释</p>
     *
     * @param clazz          待关联的子表
     * @param primaryField   <b>主表</b>关联字段
     * @param joinTableField <b>子表</b>关联字段
     */
    <E> LambdaSubCondition<E,T> joinTable(Class<E> clazz, SFunction<T,?> primaryField, SFunction<E,?> joinTableField);

    /**
     * 关联表查询
     * <p>调用本方法时请先查看Condition类JavaDoc注释</p>
     *
     * @param clazz          待关联的子表
     * @param primaryField   <b>主表</b>关联字段
     * @param joinTableField <b>子表</b>关联字段
     * @param compositField  <b>子表</b>实体类成员变量名
     */
    <E> LambdaSubCondition<E,T> joinTable(Class<E> clazz, SFunction<T,?> primaryField, SFunction<E,?> joinTableField, SFunction<T,?> compositField);

    /**
     * 关联子查询
     * <p>调用本方法时请先查看Condition类JavaDoc注释</p>
     * <p>调用本方法将在sql语句中拼接如下字符串</p>
     * <p><b>join #{condition子表} as t1 on t.primaryField = t1.joinTableField</b>
     * </p>
     *
     * @param joinCondition      关联Condition
     * @param primaryField       <b>主表</b>关联字段
     * @param joinConditionField <b>子查询</b>关联字段
     */
    <E> LambdaSubCondition<E,T> joinTable(Condition<E> joinCondition, SFunction<T,?> primaryField, SFunction<E,?> joinConditionField);

    /**
     * 关联表查询
     * <p>调用本方法时请先查看Condition类JavaDoc注释</p>
     * <p>调用本方法将在sql语句中拼接如下字符串</p>
     * <p><b>join #{clazz} as t1 on t.primaryField = t1.joinTableField</b>
     * </p>
     *
     * @param tableName      待关联的子表
     * @param primaryField   <b>主表</b>关联字段
     * @param joinTableField <b>子表</b>关联字段
     */
    LambdaSubCondition<?,T> joinTable(String tableName, SFunction primaryField, SFunction joinTableField);

    /**
     * 设置指定字段排序方式
     *
     * @param field 升序排列字段名
     * @param asc 排序方式,值为asc或者desc
     */
    LambdaCondition<T> order(SFunction<T,?> field, String asc);

    /**
     * 根据指定字段升序排列
     *
     * @param field 升序排列字段名
     */
    LambdaCondition<T> orderBy(SFunction<T,?>... field);

    /**
     * 根据指定字段降序排列
     *
     * @param field 降序排列字段名
     */
    LambdaCondition<T> orderByDesc(SFunction<T,?>... field);

    /**
     * 执行并返回Response实例
     */
    Response<T> execute();

    /**
     * 返回Condition实例
     */
    Condition<T> done();
}
