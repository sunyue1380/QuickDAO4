package cn.schoolwow.quickdao.domain;

import cn.schoolwow.quickdao.query.condition.AbstractCondition;
import cn.schoolwow.quickdao.query.subCondition.AbstractSubCondition;
import cn.schoolwow.quickdao.query.subCondition.SubCondition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 子查询
 */
public class SubQuery<T,P> implements Serializable {
    /**
     * 关联实体
     */
    public transient Entity entity;
    /**
     * 表别名
     */
    public String tableAliasName;
    /**
     * 主表字段
     */
    public String primaryField;
    /**
     * 子表字段
     */
    public String joinTableField;
    /**
     * 对象变量名
     */
    public String compositField;
    /**
     * on条件映射
     * */
    public Map<String,String> onConditionMap = new HashMap();
    /**
     * 连接方式
     */
    public String join = "join";
    /**
     * 查询条件
     */
    public String where = "";
    /**
     * 查询参数
     */
    public List parameterList = new ArrayList();
    /**
     * join Condition 关联子查询
     * */
    public StringBuilder subQuerySQLBuilder;
    /**
     * join Condition 关联子查询Query
     * */
    public Query subQuery;
    /**
     * 关联SubCondition对象
     * */
    public AbstractSubCondition subCondition;
    /**
     * 父表
     */
    public SubQuery<T,P> parentSubQuery;
    /**
     * 父表
     */
    public SubCondition<T,P> parentSubCondition;
    /**
     * 主表
     */
    public transient Query query;
    /**
     * 主表
     */
    public transient AbstractCondition<P> condition;
}
