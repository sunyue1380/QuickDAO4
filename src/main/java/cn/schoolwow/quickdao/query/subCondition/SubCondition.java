package cn.schoolwow.quickdao.query.subCondition;

import cn.schoolwow.quickdao.query.condition.Condition;

import java.io.Serializable;
import java.util.List;

/**
 * 关联子表查询接口
 *
 * <p>关联子表查询的joinTable方法和Condition接口中的类似,只不过这是调用joinTable方法的表称为父表,关联的表为子表,父表和子表的概念是相对的.</p>
 * <ol>
 *     <li>done()方法返回主表</li>
 *     <li>doneSubCondition返回父表</li>
 * </ol>
 * </p>
 *
 * @see cn.schoolwow.quickdao.query.condition.Condition
 */
public interface SubCondition<T,P> extends Serializable {
    /**
     * 设置子表别名
     */
    SubCondition<T,P> tableAliasName(String tableAliasName);

    /**
     * 左外连接
     */
    SubCondition<T,P> leftJoin();

    /**
     * 右外连接
     */
    SubCondition<T,P> rightJoin();

    /**
     * 全外连接
     */
    SubCondition<T,P> fullJoin();

    /**
     * 添加on查询条件
     */
    SubCondition<T,P> on(String primaryField, String joinTableField);

    /**
     * 添加空查询
     *
     * @param field 指明哪个字段为Null
     */
    SubCondition<T,P> addNullQuery(String field);

    /**
     * 添加非空查询
     *
     * @param field 指明哪个字段不为Null
     */
    SubCondition<T,P> addNotNullQuery(String field);

    /**
     * 添加空查询
     *
     * @param field 指明哪个字段不为空字符串
     */
    SubCondition<T,P> addEmptyQuery(String field);

    /**
     * 添加非空查询
     *
     * @param field 指明哪个字段不为空字符串
     */
    SubCondition<T,P> addNotEmptyQuery(String field);

    /**
     * 添加范围查询语句
     *
     * @param field  字段名
     * @param values 指明在该范围内的值
     */
    SubCondition<T,P> addInQuery(String field, Object... values);

    /**
     * 添加范围查询语句
     *
     * @param field  字段名
     * @param values 指明在该范围内的值
     */
    SubCondition<T,P> addInQuery(String field, List values);

    /**
     * 添加范围查询语句
     *
     * @param field  字段名
     * @param values 指明在不该范围内的值
     */
    SubCondition<T,P> addNotInQuery(String field, Object... values);

    /**
     * 添加范围查询语句
     *
     * @param field  字段名
     * @param values 指明在不该范围内的值
     */
    SubCondition<T,P> addNotInQuery(String field, List values);

    /**
     * 添加between语句
     *
     * @param field 字段名
     * @param start 范围开始值
     * @param end   范围结束值
     */
    SubCondition<T,P> addBetweenQuery(String field, Object start, Object end);

    /**
     * 添加Like查询
     *
     * @param field 字段名
     * @param value 字段值
     */
    SubCondition<T,P> addLikeQuery(String field, Object value);

    /**
     * 添加字段查询
     *
     * @param field 字段名
     * @param value 字段值
     */
    SubCondition<T,P> addQuery(String field, Object value);

    /**
     * 添加字段查询
     *
     * @param field    字段名
     * @param operator 操作符,可为<b>></b>,<b>>=</b>,<b>=</b>,<b><</b><b><=</b>
     * @param value    字段值
     */
    SubCondition<T,P> addQuery(String field, String operator, Object value);

    /**
     * 添加自定义查询条件
     * <p>调用本方法时请先查看Condition类JavaDoc注释和SubCondition类的JavaDoc注释</p>
     *
     * @param query         子查询条件
     * @param parameterList 查询参数
     * @see cn.schoolwow.quickdao.query.condition.Condition
     * @see SubCondition
     */
    SubCondition<T,P> addRawQuery(String query, Object... parameterList);

    /**
     * 添加自定义字段,具体映射规则请看Condition类的JavaDoc注释
     *
     * @param fields 自定义查询列\
     * @see cn.schoolwow.quickdao.query.condition.Condition
     */
    SubCondition<T,P> addColumn(String... fields);

    /**
     * 关联表查询,子表可再次关联子表
     * <p>调用本方法时请先查看Condition类JavaDoc注释和SubCondition类的JavaDoc注释</p>
     *
     * @param clazz          待关联的子表
     * @param primaryField   <b>主表</b>关联字段
     * @param joinTableField <b>子表</b>关联字段
     * @see cn.schoolwow.quickdao.query.condition.Condition
     * @see SubCondition
     */
    <E> SubCondition<E,T> joinTable(Class<E> clazz, String primaryField, String joinTableField);

    /**
     * 关联表查询,子表可再次关联子表
     * <p>调用本方法时请先查看Condition类JavaDoc注释和SubCondition类的JavaDoc注释</p>
     *
     * @param clazz          待关联的子表
     * @param primaryField   <b>主表</b>关联字段
     * @param joinTableField <b>子表</b>关联字段
     * @param compositField  <b>子表</b>实体类成员变量名
     * @see cn.schoolwow.quickdao.query.condition.Condition
     * @see SubCondition
     */
    <E> SubCondition<E,T> joinTable(Class<E> clazz, String primaryField, String joinTableField, String compositField);

    /**
     * 关联表查询,子表可再次关联子表
     * <p>调用本方法时请先查看Condition类JavaDoc注释和SubCondition类的JavaDoc注释</p>
     *
     * @param tableName      待关联的子表
     * @param primaryField   <b>主表</b>关联字段
     * @param joinTableField <b>子表</b>关联字段
     * @see cn.schoolwow.quickdao.query.condition.Condition
     * @see SubCondition
     */
    SubCondition joinTable(String tableName, String primaryField, String joinTableField);

    /**
     * 添加分组查询
     *
     * @param fields 分组字段
     */
    SubCondition<T,P> groupBy(String... fields);

    /**
     * 设置指定字段排序方式
     *
     * @param field 升序排列字段名
     * @param asc 排序方式,值为asc或者desc
     */
    SubCondition<T,P> order(String field, String asc);

    /**
     * 根据指定字段升序排列
     *
     * @param fields 升序排列字段名
     */
    SubCondition<T,P> orderBy(String... fields);

    /**
     * 根据指定字段降序排列
     *
     * @param fields 降序排列字段名
     */
    SubCondition<T,P> orderByDesc(String... fields);

    /**
     * 获取lambdaSubCondition对象
     */
    LambdaSubCondition<T,P> lambdaSubCondition();

    /**
     * 返回<b>父表</b>
     *
     * @see {@link SubCondition#joinTable(Class, String, String)}
     */
    SubCondition doneSubCondition();

    /**
     * 返回<b>主表</b>
     *
     * @see {@link cn.schoolwow.quickdao.query.condition.Condition#joinTable(Class, String, String)}
     */
    Condition done();
}
