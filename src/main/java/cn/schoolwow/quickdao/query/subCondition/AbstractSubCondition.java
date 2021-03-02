package cn.schoolwow.quickdao.query.subCondition;

import cn.schoolwow.quickdao.domain.Property;
import cn.schoolwow.quickdao.domain.SubQuery;
import cn.schoolwow.quickdao.query.condition.Condition;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;

public class AbstractSubCondition<T,P> implements SubCondition<T,P>{
    private SubQuery subQuery;

    public AbstractSubCondition(SubQuery subQuery) {
        this.subQuery = subQuery;
    }

    @Override
    public SubCondition<T,P> tableAliasName(String tableAliasName) {
        this.subQuery.tableAliasName = tableAliasName;
        return this;
    }

    @Override
    public SubCondition<T,P> leftJoin() {
        subQuery.join = "left outer join";
        return this;
    }

    @Override
    public SubCondition<T,P> rightJoin() {
        subQuery.join = "right outer join";
        return this;
    }

    @Override
    public SubCondition<T,P> fullJoin() {
        subQuery.join = "full outer join";
        return this;
    }

    @Override
    public SubCondition<T,P> on(String primaryField, String joinTableField) {
        if(null==subQuery.parentSubQuery){
            //主表关联子表
            subQuery.onConditionMap.put(
                    subQuery.query.entity.getColumnNameByFieldName(primaryField),
                    subQuery.entity.getColumnNameByFieldName(joinTableField)
            );
        }else{
            //子表再次关联子表
            subQuery.onConditionMap.put(
                    subQuery.parentSubQuery.entity.getColumnNameByFieldName(primaryField),
                    subQuery.entity.getColumnNameByFieldName(joinTableField)
            );
        }
        return this;
    }

    @Override
    public SubCondition<T,P> addNullQuery(String field) {
        subQuery.whereBuilder.append("(" + getQueryColumnNameByFieldName(field) + " is null) and ");
        return this;
    }

    @Override
    public SubCondition<T,P> addNotNullQuery(String field) {
        subQuery.whereBuilder.append("(" + getQueryColumnNameByFieldName(field) + " is not null) and ");
        return this;
    }

    @Override
    public SubCondition<T,P> addEmptyQuery(String field) {
        subQuery.whereBuilder.append("(" + getQueryColumnNameByFieldName(field) + " is not null and " + getQueryColumnNameByFieldName(field) + " = '') and ");
        return this;
    }

    @Override
    public SubCondition<T,P> addNotEmptyQuery(String field) {
        subQuery.whereBuilder.append("(" + getQueryColumnNameByFieldName(field) + " is not null and " + getQueryColumnNameByFieldName(field) + " != '') and ");
        return this;
    }

    @Override
    public SubCondition<T,P> addInQuery(String field, Object... values) {
        addInQuery(field,values,"in");
        return this;
    }

    @Override
    public SubCondition<T,P> addInQuery(String field, List values) {
        return addInQuery(field, values.toArray(new Object[0]));
    }

    @Override
    public SubCondition<T,P> addNotInQuery(String field, Object... values) {
        addInQuery(field,values,"not in");
        return this;
    }

    @Override
    public SubCondition<T,P> addNotInQuery(String field, List values) {
        return addNotInQuery(field, values.toArray(new Object[0]));
    }

    @Override
    public SubCondition<T,P> addBetweenQuery(String field, Object start, Object end) {
        subQuery.whereBuilder.append("(" + getQueryColumnNameByFieldName(field) + " between ? and ? ) and ");
        subQuery.parameterList.add(start);
        subQuery.parameterList.add(end);
        return this;
    }

    @Override
    public SubCondition<T,P> addLikeQuery(String field, Object value) {
        if (value == null || value.toString().equals("")) {
            return this;
        }
        subQuery.whereBuilder.append("(" + getQueryColumnNameByFieldName(field) + " like ?) and ");
        subQuery.parameterList.add(value);
        return this;
    }

    @Override
    public SubCondition<T,P> addQuery(String field, Object value) {
        addQuery(field, "=", value);
        return this;
    }

    @Override
    public SubCondition<T,P> addQuery(String field, String operator, Object value) {
        if(null==value){
            addNullQuery(field);
        }else if(value.toString().isEmpty()){
            addEmptyQuery(field);
        }else {
            subQuery.whereBuilder.append("(" + getQueryColumnNameByFieldName(field) + " " + operator + " ?) and ");
            subQuery.parameterList.add(value);
        }
        return this;
    }

    @Override
    public SubCondition<T,P> addRawQuery(String query, Object... parameterList) {
        subQuery.whereBuilder.append("(" + query + ") and ");
        if(null!=parameterList&&parameterList.length>0){
            subQuery.parameterList.addAll(Arrays.asList(parameterList));
        }
        return this;
    }

    @Override
    public SubCondition<T,P> addColumn(String... fields) {
        for(String field:fields){
            subQuery.query.columnBuilder.append(getQueryColumnNameByFieldName(field)+ ",");
        }
        return this;
    }

    @Override
    public <E> SubCondition<E,T> joinTable(Class<E> clazz, String primaryField, String joinTableField) {
        return joinTable(clazz,primaryField,joinTableField,subQuery.entity.getCompositeFieldName(clazz.getName()));
    }

    @Override
    public <E> SubCondition<E,T> joinTable(Class<E> clazz, String primaryField, String joinTableField, String compositField) {
        AbstractSubCondition abstractSubCondition = (AbstractSubCondition) subQuery.condition.joinTable(clazz, primaryField, joinTableField, compositField);
        abstractSubCondition.subQuery.parentSubQuery = this.subQuery;
        abstractSubCondition.subQuery.parentSubCondition = this;
        return abstractSubCondition;
    }

    @Override
    public SubCondition<?,T> joinTable(String tableName, String primaryField, String joinTableField) {
        AbstractSubCondition abstractSubCondition = (AbstractSubCondition) subQuery.condition.joinTable(tableName, primaryField, joinTableField);
        abstractSubCondition.subQuery.parentSubQuery = this.subQuery;
        abstractSubCondition.subQuery.parentSubCondition = this;
        return abstractSubCondition;
    }

    @Override
    public SubCondition<T,P> groupBy(String... fields) {
        for(String field:fields){
            subQuery.query.groupByBuilder.append(getQueryColumnNameByFieldName(field) + ",");
        }
        return this;
    }

    @Override
    public SubCondition<T,P> order(String field, String asc) {
        subQuery.query.orderByBuilder.append(getQueryColumnNameByFieldName(field) + " " + asc + ",");
        return this;
    }

    @Override
    public SubCondition<T,P> orderBy(String... fields) {
        for(String field:fields){
            subQuery.query.orderByBuilder.append(getQueryColumnNameByFieldName(field) + " asc,");
        }
        return this;
    }

    @Override
    public SubCondition<T,P> orderByDesc(String... fields) {
        for(String field:fields){
            subQuery.query.orderByBuilder.append(getQueryColumnNameByFieldName(field) + " desc,");
        }
        return this;
    }

    @Override
    public LambdaSubCondition<T,P> lambdaSubCondition() {
        LambdaSubConditionInvocationHandler<T,P> invocationHandler = new LambdaSubConditionInvocationHandler<T,P>(this);
        LambdaSubCondition<T,P> lambdaSubCondition = (LambdaSubCondition<T,P>) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),new Class<?>[]{LambdaSubCondition.class},invocationHandler);
        return lambdaSubCondition;
    }

    @Override
    public SubCondition<P,?> doneSubCondition() {
        return subQuery.parentSubCondition;
    }

    @Override
    public Condition<P> done() {
        return subQuery.condition;
    }

    /**添加in查询*/
    private void addInQuery(String field, Object[] values, String in) {
        if (null == values || values.length == 0) {
            subQuery.whereBuilder.append("( 1 = 2 ) and ");
            return;
        }
        subQuery.whereBuilder.append("(" + getQueryColumnNameByFieldName(field) + " " + in + " (");
        for (int i = 0; i < values.length; i++) {
            subQuery.whereBuilder.append("?,");
        }
        subQuery.whereBuilder.deleteCharAt(subQuery.whereBuilder.length() - 1);
        subQuery.whereBuilder.append(") ) and ");
        subQuery.parameterList.addAll(Arrays.asList(values));
    }

    /**
     * 根据字段名查询数据库列名
     * */
    private String getQueryColumnNameByFieldName(String field) {
        Property property = subQuery.entity.getPropertyByFieldName(field);
        if(null==property){
            return field;
        }
        if(subQuery.query.unionList.isEmpty()){
            return subQuery.tableAliasName+"."+subQuery.query.quickDAOConfig.database.escape(property.column);
        }else{
            return subQuery.query.quickDAOConfig.database.escape(property.column);
        }
    }
}
