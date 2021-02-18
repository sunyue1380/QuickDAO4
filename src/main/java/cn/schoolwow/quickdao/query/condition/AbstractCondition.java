package cn.schoolwow.quickdao.query.condition;

import cn.schoolwow.quickdao.domain.*;
import cn.schoolwow.quickdao.query.response.AbstractResponse;
import cn.schoolwow.quickdao.query.response.Response;
import cn.schoolwow.quickdao.query.response.ResponseInvocationHandler;
import cn.schoolwow.quickdao.query.subCondition.AbstractSubCondition;
import cn.schoolwow.quickdao.query.subCondition.SubCondition;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class AbstractCondition<T> implements Condition<T>, Serializable,Cloneable {
    /**查询对象*/
    public Query query;
    /**execute方法是否已经被调用过*/
    private boolean hasExecute;

    public AbstractCondition(Query query) {
        this.query = query;
    }

    @Override
    public Condition<T> tableAliasName(String tableAliasName) {
        query.tableAliasName = tableAliasName;
        return this;
    }

    @Override
    public Condition<T> distinct() {
        query.distinct = "distinct";
        return this;
    }

    @Override
    public Condition<T> addNullQuery(String field) {
        query.whereBuilder.append("(" + getQueryColumnNameByFieldName(field) + " is null) and ");
        return this;
    }

    @Override
    public Condition<T> addNotNullQuery(String field) {
        query.whereBuilder.append("(" + getQueryColumnNameByFieldName(field) + " is not null) and ");
        return this;
    }

    @Override
    public Condition<T> addEmptyQuery(String field) {
        query.whereBuilder.append("(" + getQueryColumnNameByFieldName(field) + " is not null and " + getQueryColumnNameByFieldName(field) + " = '') and ");
        return this;
    }

    @Override
    public Condition<T> addNotEmptyQuery(String field) {
        query.whereBuilder.append("(" + getQueryColumnNameByFieldName(field) + " is not null and " + getQueryColumnNameByFieldName(field) + " != '') and ");
        return this;
    }

    @Override
    public Condition<T> addInQuery(String field, String inQuery) {
        if(null==inQuery||inQuery.isEmpty()){
            query.whereBuilder.append("( 1 = 2 ) and ");
            return this;
        }
        query.whereBuilder.append("(" + getQueryColumnNameByFieldName(field) + " in ("+inQuery+") ) and ");
        return this;
    }

    @Override
    public Condition<T> addInQuery(String field, Object... values) {
        addInQuery(field, values, "in");
        return this;
    }

    @Override
    public Condition<T> addInQuery(String field, Collection values) {
        return addInQuery(field,values.toArray(new Object[0]));
    }

    @Override
    public Condition<T> addNotInQuery(String field, String inQuery) {
        if(null==inQuery||inQuery.isEmpty()){
            query.whereBuilder.append("( 1 = 2 ) and ");
            return this;
        }
        query.whereBuilder.append("(" + getQueryColumnNameByFieldName(field) + " not in ("+inQuery+") ) and ");
        return this;
    }

    @Override
    public Condition<T> addNotInQuery(String field, Object... values) {
        addInQuery(field, values, "not in");
        return this;
    }

    @Override
    public Condition<T> addNotInQuery(String field, Collection values) {
        return addNotInQuery(field,values.toArray(new Object[0]));
    }

    @Override
    public Condition<T> addBetweenQuery(String field, Object start, Object end) {
        query.whereBuilder.append("(" + getQueryColumnNameByFieldName(field) + " between ? and ? ) and ");
        query.parameterList.add(start);
        query.parameterList.add(end);
        return this;
    }

    @Override
    public Condition<T> addLikeQuery(String field, Object value) {
        if (value == null || value.toString().equals("")) {
            return this;
        }
        query.whereBuilder.append("(" + getQueryColumnNameByFieldName(field) + " like ?) and ");
        query.parameterList.add(value);
        return this;
    }

    @Override
    public Condition<T> addNotLikeQuery(String field, Object value) {
        if (value == null || value.toString().equals("")) {
            return this;
        }
        query.whereBuilder.append("(" + getQueryColumnNameByFieldName(field) + " not like ?) and ");
        query.parameterList.add(value);
        return this;
    }

    @Override
    public Condition<T> addQuery(String field, Object value) {
        addQuery(field, "=", value);
        return this;
    }

    @Override
    public Condition<T> addQuery(String field, String operator, Object value) {
        if(null==value){
            addNullQuery(field);
        }else if(value.toString().isEmpty()){
            addEmptyQuery(field);
        }else {
            Property property = query.entity.getPropertyByFieldName(field);
            query.whereBuilder.append("(" + getQueryColumnNameByFieldName(field) + " " + operator + " "+(null==property||null==property.function?"?":property.function)+") and ");
            query.parameterList.add(value);
        }
        return this;
    }

    @Override
    public Condition<T> addRawQuery(String query, Object... parameterList) {
        this.query.whereBuilder.append("(" + query + ") and ");
        if(null!=parameterList&&parameterList.length>0){
            this.query.parameterList.addAll(Arrays.asList(parameterList));
        }
        return this;
    }

    @Override
    public Condition<T> addSubQuery(String field, String operator, Condition subQuery) {
        subQuery.execute();
        AbstractCondition abstractCondition = (AbstractCondition) subQuery;
        abstractCondition.query.tableAliasName = query.tableAliasName + (query.joinTableIndex++);
        String limit = " "+((AbstractCondition) subQuery).query.limit;
        query.whereBuilder.append(getQueryColumnNameByFieldName(field) + " " + operator + " (" + query.dqlBuilder.getArraySQL(abstractCondition.query) + limit +") and ");
        this.query.parameterList.addAll(abstractCondition.query.parameterList);
        return this;
    }

    @Override
    public Condition<T> addExistSubQuery(Condition subQuery) {
        addExistSubQuery(subQuery,"exists");
        return this;
    }

    @Override
    public Condition<T> addNotExistSubQuery(Condition subQuery) {
        addExistSubQuery(subQuery,"not exists");
        return this;
    }

    @Override
    public Condition<T> addColumn(String... fields) {
        for(String field:fields){
            query.columnBuilder.append(query.entity.getColumnNameByFieldName(field)+ ",");
        }
        return this;
    }

    @Override
    public Condition<T> setColumnTypeMapping(ColumnTypeMapping columnTypeMapping){
        query.columnTypeMapping = columnTypeMapping;
        return this;
    }

    @Override
    public Condition<T> addColumn(Condition subQuery) {
        subQuery.execute();
        Query selectQuery = ((AbstractCondition)subQuery).query;
        String limit = " "+((AbstractCondition) subQuery).query.limit;
        query.columnBuilder.append("( " + query.dqlBuilder.getArraySQL(selectQuery) + limit + " ),");
        query.parameterList.addAll(selectQuery.parameterList);
        return this;
    }

    @Override
    public Condition<T> addColumn(Condition subQuery, String columnNameAlias) {
        subQuery.execute();
        Query selectQuery = ((AbstractCondition)subQuery).query;
        String limit = " "+((AbstractCondition) subQuery).query.limit;
        query.columnBuilder.append("( " + query.dqlBuilder.getArraySQL(selectQuery) + limit +") "+columnNameAlias+",");
        query.selectQueryList.add(selectQuery);
        return this;
    }

    @Override
    public Condition<T> addInsert(String field, Object value) {
        query.insertBuilder.append(query.quickDAOConfig.database.escape(query.entity.getColumnNameByFieldName(field)) + ",");
        query.insertParameterList.add(value);
        return this;
    }

    @Override
    public Condition<T> addInsert(JSONObject value) {
        if(null==value||value.isEmpty()){
            throw new IllegalArgumentException("value参数不能为空!");
        }
        List<Property> properties = query.entity.properties;
        for(Property property:properties){
            if(value.containsKey(property.column)){
                addInsert(property.column,value.get(property.column));
            }
        }
        query.insertValue = value;
        return this;
    }

    @Override
    public Condition<T> addInsert(JSONArray array) {
        if(null==array||array.isEmpty()){
            throw new IllegalArgumentException("array参数不能为空!");
        }
        query.insertArray = array;
        return this;
    }

    @Override
    public Condition<T> addUpdate(String field, Object value) {
        query.setBuilder.append(query.quickDAOConfig.database.escape(query.entity.getColumnNameByFieldName(field)) + " = ?,");
        query.updateParameterList.add(value);
        return this;
    }

    @Override
    public Condition<T> union(Condition<T> condition) {
        return union(condition,UnionType.Union);
    }

    @Override
    public Condition<T> union(Condition<T> condition ,UnionType unionType) {
        AbstractCondition abstractCondition = (AbstractCondition) condition;
        abstractCondition.query.unionType = unionType;
        query.unionList.add(abstractCondition);
        return this;
    }

    @Override
    public Condition<T> or() {
        AbstractCondition orCondition = (AbstractCondition) query.quickDAOConfig.dao.query(query.entity.clazz);
        query.orList.add(orCondition);
        return orCondition;
    }

    @Override
    public Condition<T> or(String or, Object... parameterList) {
        if(query.whereBuilder.length()>0){
            query.whereBuilder.replace(query.whereBuilder.length()-5,query.whereBuilder.length()," or ");
        }
        query.whereBuilder.append("(" + or + ") and ");
        if(null!=parameterList&&parameterList.length>0){
            query.parameterList.addAll(Arrays.asList(parameterList));
        }
        return this;
    }

    @Override
    public Condition<T> groupBy(String... fields) {
        for(String field:fields){
            query.groupByBuilder.append(getQueryColumnNameByFieldName(field) + ",");
        }
        return this;
    }

    @Override
    public Condition<T> having(String having, Object... parameterList) {
        query.havingBuilder.append("(" + having + ") and ");
        if(null!=parameterList&&parameterList.length>0){
            query.havingParameterList.addAll(Arrays.asList(parameterList));
        }
        return this;
    }

    @Override
    public Condition<T> having(String field, String operator, Condition subQuery) {
        subQuery.execute();
        AbstractCondition abstractCondition = (AbstractCondition) subQuery;
        String limit = " "+((AbstractCondition) subQuery).query.limit;
        query.havingBuilder.append(getQueryColumnNameByFieldName(field) + " " + operator + " (" + query.dqlBuilder.getArraySQL(abstractCondition.query) + limit + ") and ");
        this.query.parameterList.addAll(abstractCondition.query.parameterList);
        return this;
    }

    @Override
    public <E> SubCondition<E> crossJoinTable(Class<E> clazz) {
        SubQuery<E> subQuery = new SubQuery<E>();
        subQuery.entity = query.quickDAOConfig.getEntityByClassName(clazz.getName());
        subQuery.tableAliasName = query.tableAliasName + (query.joinTableIndex++);
        subQuery.join = "cross join";
        subQuery.query = query;
        subQuery.condition = this;

        AbstractSubCondition<E> subCondition = (AbstractSubCondition) query.quickDAOConfig.database.getSubConditionInstance(subQuery);
        query.subQueryList.add(subQuery);
        return subCondition;
    }

    @Override
    public <E> SubCondition<E> crossJoinTable(String tableName) {
        SubQuery subQuery = new SubQuery();
        for(Entity entity:query.quickDAOConfig.dbEntityList){
            if(entity.tableName.equals(tableName)){
                subQuery.entity = entity;
                break;
            }
        }
        if(null==subQuery.entity){
            throw new IllegalArgumentException("关联表不存在!表名:"+tableName);
        }
        subQuery.tableAliasName = query.tableAliasName + (query.joinTableIndex++);
        subQuery.join = "cross join";
        subQuery.query = query;
        subQuery.condition = this;

        AbstractSubCondition subCondition = (AbstractSubCondition) query.quickDAOConfig.database.getSubConditionInstance(subQuery);
        query.subQueryList.add(subQuery);
        return subCondition;
    }

    @Override
    public <E> SubCondition<E> joinTable(Class<E> clazz, String primaryField, String joinTableField) {
        return joinTable(clazz,primaryField,joinTableField,query.entity.getCompositeFieldName(clazz.getName()));
    }

    @Override
    public <E> SubCondition<E> joinTable(Class<E> clazz, String primaryField, String joinTableField, String compositField) {
        SubQuery<E> subQuery = new SubQuery<E>();
        subQuery.entity = query.quickDAOConfig.getEntityByClassName(clazz.getName());
        subQuery.tableAliasName = query.tableAliasName + (query.joinTableIndex++);
        subQuery.primaryField = query.entity.getColumnNameByFieldName(primaryField);
        for(Property property:subQuery.entity.properties){
            if(property.name.equals(joinTableField)){
                subQuery.joinTableField = property.column;
                break;
            }
        }
        if(null==subQuery.joinTableField){
            subQuery.joinTableField = joinTableField;
        }
        subQuery.compositField = compositField;
        subQuery.query = query;
        subQuery.condition = this;

        AbstractSubCondition<E> subCondition = (AbstractSubCondition<E>) query.quickDAOConfig.database.getSubConditionInstance(subQuery);
        query.subQueryList.add(subQuery);
        return subCondition;
    }

    @Override
    public <E> SubCondition<E> joinTable(Condition<E> joinCondition, String primaryField, String joinConditionField) {
        joinCondition.execute();
        Query joinQuery = ((AbstractCondition) joinCondition).query;
        SubQuery<E> subQuery = new SubQuery();
        subQuery.entity = joinQuery.entity;
        subQuery.subQuerySQLBuilder = joinQuery.dqlBuilder.getArraySQL(joinQuery);

        subQuery.tableAliasName = query.tableAliasName + (query.joinTableIndex++);
        subQuery.primaryField = query.entity.getColumnNameByFieldName(primaryField);
        subQuery.joinTableField = joinConditionField;
        subQuery.subQuery = joinQuery;
        subQuery.condition = this;
        subQuery.query = query;

        AbstractSubCondition<E> subCondition = (AbstractSubCondition<E>) query.quickDAOConfig.database.getSubConditionInstance(subQuery);
        query.subQueryList.add(subQuery);
        return subCondition;
    }

    @Override
    public SubCondition<T> joinTable(String tableName, String primaryField, String joinTableField) {
        SubQuery subQuery = new SubQuery();
        for(Entity entity:query.quickDAOConfig.dbEntityList){
            if(entity.tableName.equals(tableName)){
                subQuery.entity = entity;
                break;
            }
        }
        if(null==subQuery.entity){
            throw new IllegalArgumentException("关联表不存在!表名:"+tableName);
        }
        subQuery.tableAliasName = query.tableAliasName + (query.joinTableIndex++);
        subQuery.primaryField = query.entity.getColumnNameByFieldName(primaryField);
        subQuery.joinTableField = joinTableField;
        subQuery.query = query;
        subQuery.condition = this;

        AbstractSubCondition subCondition = (AbstractSubCondition) query.quickDAOConfig.database.getSubConditionInstance(subQuery);
        query.subQueryList.add(subQuery);
        return subCondition;
    }

    @Override
    public Condition<T> orderBy(String... fields) {
        for(String field:fields){
            query.orderByBuilder.append(getQueryColumnNameByFieldName(field)+" asc,");
        }
        return this;
    }

    @Override
    public Condition<T> orderByDesc(String... fields) {
        for(String field:fields){
            query.orderByBuilder.append(getQueryColumnNameByFieldName(field)+" desc,");
        }
        return this;
    }

    @Override
    public Condition<T> limit(long offset, long limit) {
        query.limit = "limit " + offset + "," + limit;
        return this;
    }

    @Override
    public Condition<T> page(int pageNum, int pageSize) {
        query.limit = "limit " + (pageNum - 1) * pageSize + "," + pageSize;
        query.pageVo = new PageVo<>();
        query.pageVo.setPageSize(pageSize);
        query.pageVo.setCurrentPage(pageNum);
        return this;
    }

    @Override
    public Condition<T> compositField() {
        query.compositField = true;
        return this;
    }

    @Override
    public Response<T> execute() {
        if(hasExecute){
            throw new IllegalArgumentException("该Condition已经执行过,不能再次执行!");
        }
        if (query.columnBuilder.length() > 0) {
            query.columnBuilder.deleteCharAt(query.columnBuilder.length() - 1);
        }
        if (query.setBuilder.length() > 0) {
            query.setBuilder.deleteCharAt(query.setBuilder.length() - 1);
            query.setBuilder.insert(0, "set ");
        }
        if (query.insertBuilder.length() > 0) {
            query.insertBuilder.deleteCharAt(query.insertBuilder.length() - 1);
        }
        if (query.whereBuilder.length() > 0) {
            query.whereBuilder.delete(query.whereBuilder.length() - 5, query.whereBuilder.length());
        }
        if (query.groupByBuilder.length() > 0) {
            query.groupByBuilder.deleteCharAt(query.groupByBuilder.length()-1);
            query.groupByBuilder.insert(0, "group by ");
        }
        if (query.havingBuilder.length() > 0) {
            query.havingBuilder.delete(query.havingBuilder.length() - 5, query.havingBuilder.length());
            query.havingBuilder.insert(0, "having ");
        }
        if (query.orderByBuilder.length() > 0) {
            query.orderByBuilder.deleteCharAt(query.orderByBuilder.length() - 1);
            query.orderByBuilder.insert(0, "order by ");
        }
        //处理所有子查询的where语句
        for (SubQuery subQuery : query.subQueryList) {
            if (subQuery.whereBuilder.length() > 0) {
                subQuery.whereBuilder.delete(subQuery.whereBuilder.length() - 5, subQuery.whereBuilder.length());
            }
        }
        //处理所有union
        for(AbstractCondition condition:query.unionList){
            condition.execute();
        }
        for(AbstractCondition condition:query.orList){
            condition.execute();
        }
        hasExecute = true;
        AbstractResponse<T> abstractResponse = new AbstractResponse<T>(query);
        ResponseInvocationHandler invocationHandler = new ResponseInvocationHandler(abstractResponse);
        return (Response) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),new Class<?>[]{Response.class},invocationHandler);
    }

    @Override
    public Condition<T> clone(){
        Query query = this.query.clone();
        AbstractCondition<T> abstractCondition = new AbstractCondition<T>(query);
        for(int i=0;i<query.subQueryList.size();i++){
            query.subQueryList.get(i).condition = abstractCondition;
        }
        return abstractCondition;
    }

    @Override
    public Query getQuery() {
        return this.query;
    }

    /**添加in查询*/
    private void addInQuery(String field, Object[] values, String in) {
        if (null == values || values.length == 0) {
            query.whereBuilder.append("( 1 = 2 ) and ");
            return;
        }
        query.whereBuilder.append("(" + getQueryColumnNameByFieldName(field) + " " + in + " (");
        for (int i = 0; i < values.length; i++) {
            query.whereBuilder.append("?,");
        }
        query.whereBuilder.deleteCharAt(query.whereBuilder.length() - 1);
        query.whereBuilder.append(") ) and ");
        query.parameterList.addAll(Arrays.asList(values));
    }

    /**添加exist查询*/
    private void addExistSubQuery(Condition subQuery, String exist) {
        subQuery.execute();
        AbstractCondition abstractCondition = (AbstractCondition) subQuery;
        String limit = ((AbstractCondition) subQuery).query.limit;
        query.whereBuilder.append(exist +" (" + query.dqlBuilder.getArraySQL(abstractCondition.query) + " " + limit + ") and ");
        this.query.parameterList.addAll(abstractCondition.query.parameterList);
    }

    /**
     * 根据字段名查询数据库列名,返回表名加列名
     * */
    private String getQueryColumnNameByFieldName(String field) {
        Property property = query.entity.getPropertyByFieldName(field);
        if(null==property){
            return field;
        }
        if(query.unionList.isEmpty()){
            return query.tableAliasName+"."+query.quickDAOConfig.database.escape(property.column);
        }else{
            return query.quickDAOConfig.database.escape(property.column);
        }
    }

}
