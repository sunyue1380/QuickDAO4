package cn.schoolwow.quickdao.query.condition;

import cn.schoolwow.quickdao.domain.*;
import cn.schoolwow.quickdao.query.response.AbstractResponse;
import cn.schoolwow.quickdao.query.response.Response;
import cn.schoolwow.quickdao.query.response.ResponseInvocationHandler;
import cn.schoolwow.quickdao.query.subCondition.AbstractSubCondition;
import cn.schoolwow.quickdao.query.subCondition.SubCondition;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class AbstractCondition<T> implements Condition<T>, Serializable,Cloneable {
    /**
     * 忽略标识
     * */
    private transient static final String IGNORE = "##IGNORE##";

    /**
     * or标识
     * */
    private transient static final String OR = "##OR##";

    /**
     * column字段
     * */
    protected List<String> columnList = new ArrayList<>();

    /**
     * where语句
     * */
    protected List<FieldFragmentEntry> whereList = new ArrayList<>();

    /**
     * groupBy字段
     * */
    protected List<String> groupByList = new ArrayList<>();

    /**
     * having字段
     * */
    protected List<FieldFragmentEntry> havingList = new ArrayList<>();

    /**
     * orderBy字段
     * */
    protected List<FieldFragmentEntry> orderByList = new ArrayList<>();

    /**
     * 用于拼接SQL字符串
     * */
    private StringBuilder builder = new StringBuilder();

    /**
     * 查询对象
     * */
    public transient Query query;

    /**
     * execute方法是否已经被调用过
     * */
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
        whereList.add(new FieldFragmentEntry(field,"{} is null"));
        return this;
    }

    @Override
    public Condition<T> addNotNullQuery(String field) {
        whereList.add(new FieldFragmentEntry(field,"{} is not null"));
        return this;
    }

    @Override
    public Condition<T> addEmptyQuery(String field) {
        whereList.add(new FieldFragmentEntry(field,"{} is not null and {} = ''"));
        return this;
    }

    @Override
    public Condition<T> addNotEmptyQuery(String field) {
        whereList.add(new FieldFragmentEntry(field,"{} is not null and {} != ''"));
        return this;
    }

    @Override
    public Condition<T> addInQuery(String field, String inQuery) {
        if(null==inQuery||inQuery.isEmpty()){
            whereList.add(new FieldFragmentEntry(field,"1 = 2"));
            return this;
        }
        whereList.add(new FieldFragmentEntry(field,"{} in ("+inQuery+")"));
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
            whereList.add(new FieldFragmentEntry(field,"1 = 2"));
            return this;
        }
        whereList.add(new FieldFragmentEntry(field,"{} not in ("+inQuery+")"));
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
        whereList.add(new FieldFragmentEntry(field,"{} between ? and ?"));
        query.parameterList.add(start);
        query.parameterList.add(end);
        return this;
    }

    @Override
    public Condition<T> addLikeQuery(String field, Object value) {
        if (value == null || value.toString().equals("")) {
            return this;
        }
        whereList.add(new FieldFragmentEntry(field,"{} like ?"));
        query.parameterList.add(value);
        return this;
    }

    @Override
    public Condition<T> addNotLikeQuery(String field, Object value) {
        if (value == null || value.toString().equals("")) {
            return this;
        }
        whereList.add(new FieldFragmentEntry(field,"{} not like ?"));
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
            whereList.add(new FieldFragmentEntry(field,"{} " + operator + " " + (null==property||null==property.function?"?":property.function)));
            query.parameterList.add(value);
        }
        return this;
    }

    @Override
    public Condition<T> addRawQuery(String query, Object... parameterList) {
        this.whereList.add(new FieldFragmentEntry(IGNORE,query));
        if(null!=parameterList&&parameterList.length>0){
            this.query.parameterList.addAll(Arrays.asList(parameterList));
        }
        return this;
    }

    @Override
    public Condition<T> addSubQuery(String field, String operator, Condition subQuery) {
        AbstractCondition abstractCondition = (AbstractCondition) subQuery;
        if(null==abstractCondition.query.tableAliasName){
            subQuery.tableAliasName("t" + (query.joinTableIndex++));
        }
        subQuery.execute();
        whereList.add(new FieldFragmentEntry(field," {} " + operator + " (" + query.dqlBuilder.getArraySQL(abstractCondition.query) + abstractCondition.query.limit +")"));
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
            columnList.add(field);
        }
        return this;
    }

    @Override
    public Condition<T> addColumnExclude(String... excludeFields) {
        List<Property> propertyList = query.entity.properties;
        for(Property property:propertyList){
            boolean contains = false;
            for(String excludeField:excludeFields){
                if(excludeField.equals(property.column)||excludeField.equals(property.name)){
                    contains = true;
                    break;
                }
            }
            if(!contains){
                addColumn(property.column);
            }
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
        columnList.add("( " + query.dqlBuilder.getArraySQL(selectQuery) + " " + ((AbstractCondition) subQuery).query.limit + ")");
        query.parameterList.addAll(selectQuery.parameterList);
        return this;
    }

    @Override
    public Condition<T> addColumn(Condition subQuery, String columnNameAlias) {
        subQuery.execute();
        Query selectQuery = ((AbstractCondition)subQuery).query;
        if(null==selectQuery.tableAliasName){
            subQuery.tableAliasName("t" + (query.joinTableIndex++));
        }
        columnList.add("( " + query.dqlBuilder.getArraySQL(selectQuery) + " " + ((AbstractCondition) subQuery).query.limit + ") " + columnNameAlias);
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
        orCondition.tableAliasName("t");
        query.orList.add(orCondition);
        return orCondition;
    }

    @Override
    public Condition<T> or(String or, Object... parameterList) {
        whereList.add(new FieldFragmentEntry(OR,or));
        if(null!=parameterList&&parameterList.length>0){
            query.parameterList.addAll(Arrays.asList(parameterList));
        }
        return this;
    }

    @Override
    public Condition<T> groupBy(String... fields) {
        groupByList.addAll(Arrays.asList(fields));
        return this;
    }

    @Override
    public Condition<T> having(String having, Object... parameterList) {
        havingList.add(new FieldFragmentEntry(IGNORE,having));
        if(null!=parameterList&&parameterList.length>0){
            query.havingParameterList.addAll(Arrays.asList(parameterList));
        }
        return this;
    }

    @Override
    public Condition<T> having(String field, String operator, Condition subQuery) {
        AbstractCondition abstractCondition = (AbstractCondition) subQuery;
        if(null==abstractCondition.query.tableAliasName){
            subQuery.tableAliasName("t" + (query.joinTableIndex++));
        }
        subQuery.execute();
        havingList.add(new FieldFragmentEntry(field,"{}" + operator + " (" + query.dqlBuilder.getArraySQL(abstractCondition.query) + ")"));
        this.query.parameterList.addAll(abstractCondition.query.parameterList);
        return this;
    }

    @Override
    public <E> SubCondition<E,T> crossJoinTable(Class<E> clazz) {
        SubQuery<E,T> subQuery = new SubQuery<E,T>();
        subQuery.entity = query.quickDAOConfig.getEntityByClassName(clazz.getName());
        subQuery.join = "cross join";
        subQuery.query = query;
        subQuery.condition = this;

        AbstractSubCondition<E,T> subCondition = (AbstractSubCondition) query.quickDAOConfig.database.getSubConditionInstance(subQuery);
        subQuery.subCondition = subCondition;
        query.subQueryList.add(subQuery);
        return subCondition;
    }

    @Override
    public SubCondition<?,T> crossJoinTable(String tableName) {
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
        subQuery.join = "cross join";
        subQuery.query = query;
        subQuery.condition = this;

        AbstractSubCondition subCondition = (AbstractSubCondition) query.quickDAOConfig.database.getSubConditionInstance(subQuery);
        subQuery.subCondition = subCondition;
        query.subQueryList.add(subQuery);
        return subCondition;
    }

    @Override
    public <E> SubCondition<E,T> joinTable(Class<E> clazz, String primaryField, String joinTableField) {
        return joinTable(clazz,primaryField,joinTableField,query.entity.getCompositeFieldName(clazz.getName()));
    }

    @Override
    public <E> SubCondition<E,T> joinTable(Class<E> clazz, String primaryField, String joinTableField, String compositField) {
        SubQuery<E,T> subQuery = new SubQuery<E,T>();
        subQuery.entity = query.quickDAOConfig.getEntityByClassName(clazz.getName());
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

        AbstractSubCondition<E,T> subCondition = (AbstractSubCondition<E,T>) query.quickDAOConfig.database.getSubConditionInstance(subQuery);
        subQuery.subCondition = subCondition;
        query.subQueryList.add(subQuery);
        return subCondition;
    }

    @Override
    public <E> SubCondition<E,T> joinTable(Condition<E> joinCondition, String primaryField, String joinConditionField) {
        joinCondition.execute();
        Query joinQuery = ((AbstractCondition) joinCondition).query;
        SubQuery<E,T> subQuery = new SubQuery();
        subQuery.entity = joinQuery.entity;
        subQuery.subQuerySQLBuilder = joinQuery.dqlBuilder.getArraySQL(joinQuery);

        subQuery.primaryField = query.entity.getColumnNameByFieldName(primaryField);
        subQuery.joinTableField = joinConditionField;
        subQuery.subQuery = joinQuery;
        subQuery.condition = this;
        subQuery.query = query;

        AbstractSubCondition<E,T> subCondition = (AbstractSubCondition<E,T>) query.quickDAOConfig.database.getSubConditionInstance(subQuery);
        subQuery.subCondition = subCondition;
        query.subQueryList.add(subQuery);
        return subCondition;
    }

    @Override
    public SubCondition<?,T> joinTable(String tableName, String primaryField, String joinTableField) {
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
        subQuery.primaryField = query.entity.getColumnNameByFieldName(primaryField);
        subQuery.joinTableField = joinTableField;
        subQuery.query = query;
        subQuery.condition = this;

        AbstractSubCondition subCondition = (AbstractSubCondition) query.quickDAOConfig.database.getSubConditionInstance(subQuery);
        subQuery.subCondition = subCondition;
        query.subQueryList.add(subQuery);
        return subCondition;
    }

    @Override
    public Condition<T> order(String field, String asc) {
        orderByList.add(new FieldFragmentEntry(field,"{} " + asc));
        return this;
    }

    @Override
    public Condition<T> orderBy(String... fields) {
        for(String field:fields){
            orderByList.add(new FieldFragmentEntry(field,"{} asc"));
        }
        return this;
    }

    @Override
    public Condition<T> orderByDesc(String... fields) {
        for(String field:fields){
            orderByList.add(new FieldFragmentEntry(field,"{} desc"));
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
    public LambdaCondition<T> lambdaCondition() {
        LambdaConditionInvocationHandler<T> invocationHandler = new LambdaConditionInvocationHandler<T>(this);
        LambdaCondition<T> lambdaCondition = (LambdaCondition<T>) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),new Class<?>[]{LambdaCondition.class},invocationHandler);
        return lambdaCondition;
    }

    @Override
    public Response<T> execute() {
        if(hasExecute){
            throw new IllegalArgumentException("该Condition已经执行过,不能再次执行!");
        }
        if(null==query.tableAliasName){
            query.tableAliasName = "t";
        }

        StringBuilder columnBuilder = new StringBuilder();
        StringBuilder whereBuilder = new StringBuilder();
        StringBuilder groupByBuilder = new StringBuilder();
        StringBuilder havingBuilder = new StringBuilder();
        StringBuilder orderByBuilder = new StringBuilder();
        //拼接主表SQL片段
        {
            columnBuilder.append(getSQLFragment(columnList,query.entity,query.tableAliasName));
            whereBuilder.append(getSQLFragment(whereList,query.entity,query.tableAliasName," and "));
            groupByBuilder.append(getSQLFragment(groupByList,query.entity,query.tableAliasName));
            havingBuilder.append(getSQLFragment(havingList,query.entity,query.tableAliasName," and "));
            orderByBuilder.append(getSQLFragment(orderByList,query.entity,query.tableAliasName,","));
        }

        //拼接子表SQL片段
        for (SubQuery subQuery : query.subQueryList) {
            if(null==subQuery.tableAliasName){
                subQuery.tableAliasName = "t" + (query.joinTableIndex++);
            }

            columnBuilder.append(getSQLFragment(subQuery.subCondition.columnList,subQuery.entity,subQuery.tableAliasName));
            whereBuilder.append(getSQLFragment(subQuery.subCondition.whereList,subQuery.entity,subQuery.tableAliasName," and "));
            query.parameterList.addAll(subQuery.parameterList);
            groupByBuilder.append(getSQLFragment(subQuery.subCondition.groupByList,subQuery.entity,subQuery.tableAliasName));
            orderByBuilder.append(getSQLFragment(subQuery.subCondition.orderByList,subQuery.entity,subQuery.tableAliasName,","));
        }

        //拼接or查询条件
        for(AbstractCondition orCondition:query.orList){
            orCondition.execute();
            if(whereBuilder.charAt(whereBuilder.length()-2)=='d'){
                whereBuilder.delete(whereBuilder.length()-5,whereBuilder.length());
                whereBuilder.append(" or ");
            }
            whereBuilder.append(getSQLFragment(orCondition.whereList,orCondition.query.entity,orCondition.query.tableAliasName," or "));
            query.parameterList.addAll(orCondition.query.parameterList);
        }

        if (query.setBuilder.length() > 0) {
            query.setBuilder.deleteCharAt(query.setBuilder.length() - 1);
            query.setBuilder.insert(0, "set ");
        }
        if (query.insertBuilder.length() > 0) {
            query.insertBuilder.deleteCharAt(query.insertBuilder.length() - 1);
        }
        if(columnBuilder.length()>0){
            columnBuilder.deleteCharAt(columnBuilder.length()-1);
            query.column = columnBuilder.toString();
        }
        if(whereBuilder.length()>0){
            char ch = whereBuilder.charAt(whereBuilder.length()-2);
            if(ch=='r'){
                //以 or 结尾
                whereBuilder.delete(whereBuilder.length()-4,whereBuilder.length());
            }else if(ch=='d'){
                //以 and 结尾
                whereBuilder.delete(whereBuilder.length()-5,whereBuilder.length());
            }
            whereBuilder.insert(0,"where ");
            query.where = whereBuilder.toString();
        }
        if(groupByBuilder.length()>0){
            groupByBuilder.deleteCharAt(groupByBuilder.length()-1);
            groupByBuilder.insert(0,"group by ");
            query.groupBy = groupByBuilder.toString();
        }
        if(havingBuilder.length()>0){
            havingBuilder.delete(havingBuilder.length()-5,havingBuilder.length());
            havingBuilder.insert(0,"having ");
            query.having = havingBuilder.toString();
        }
        if(orderByBuilder.length()>0){
            orderByBuilder.deleteCharAt(orderByBuilder.length()-1);
            orderByBuilder.insert(0,"order by ");
            query.orderBy = orderByBuilder.toString();
        }
        //处理所有union
        for(AbstractCondition condition:query.unionList){
            condition.execute();
        }
        hasExecute = true;
        AbstractResponse<T> abstractResponse = new AbstractResponse<T>(query);
        ResponseInvocationHandler invocationHandler = new ResponseInvocationHandler(abstractResponse);
        return (Response) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),new Class<?>[]{Response.class},invocationHandler);
    }

    @Override
    public AbstractCondition clone(){
        Query query = this.query.clone();
        ByteArrayInputStream bais = null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()){
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);
            oos.close();

            bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            AbstractCondition abstractCondition = (AbstractCondition) ois.readObject();
            abstractCondition.query = query;
            for(int i=0;i<query.subQueryList.size();i++){
                query.subQueryList.get(i).condition = abstractCondition;
            }
            bais.close();
            return abstractCondition;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(null!=bais){
                try {
                    bais.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    public Query getQuery() {
        return this.query;
    }

    /**添加in查询*/
    private void addInQuery(String field, Object[] values, String in) {
        if (null == values || values.length == 0) {
            whereList.add(new FieldFragmentEntry(IGNORE,"1 = 2"));
            return;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(" {} " + in + " (");
        for (int i = 0; i < values.length; i++) {
            builder.append("?,");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append(")");
        whereList.add(new FieldFragmentEntry(field,builder.toString()));
        query.parameterList.addAll(Arrays.asList(values));
    }

    /**添加exist查询*/
    private void addExistSubQuery(Condition subQuery, String exist) {
        AbstractCondition abstractCondition = (AbstractCondition) subQuery;
        if(null==abstractCondition.query.tableAliasName){
            subQuery.tableAliasName("t" + (query.joinTableIndex++));
        }
        subQuery.execute();
        whereList.add(new FieldFragmentEntry(IGNORE,exist +" (" + query.dqlBuilder.getArraySQL(abstractCondition.query) + " " + abstractCondition.query.limit + ")"));
        this.query.parameterList.addAll(abstractCondition.query.parameterList);
    }

    /**
     * 获取SQL片段
     * @param fields 字段信息
     * @param entity 实体类信息
     * @param tableAliasName 表别名
     * */
    private String getSQLFragment(List<String> fields, Entity entity, String tableAliasName){
        builder.setLength(0);
        for(String field:fields){
            Property property = entity.getPropertyByFieldName(field);
            if(null==property){
                builder.append(field);
            }else {
                if(!query.unionList.isEmpty()){
                    builder.append(query.quickDAOConfig.database.escape(property.column));
                }else{
                    builder.append(tableAliasName + "." + query.quickDAOConfig.database.escape(property.column));
                }
                if(field.contains(" ")){
                    builder.append(field.substring(field.indexOf(" ")));
                }
            }
            builder.append(",");
        }
        return builder.toString();
    }

    /**
     * 获取SQL片段
     * @param fragmentEntryList 字段pianduan信息
     * @param entity 实体类信息
     * @param tableAliasName 表别名
     * */
    private String getSQLFragment(List<FieldFragmentEntry> fragmentEntryList, Entity entity, String tableAliasName, String separator){
        builder.setLength(0);
        for(FieldFragmentEntry fieldFragmentEntry:fragmentEntryList){
            if(IGNORE.equals(fieldFragmentEntry.field)){
                builder.append("( " + fieldFragmentEntry.fragment + " ) " + separator);
                continue;
            }else if(OR.equals(fieldFragmentEntry.field)){
                if(builder.length()>3&&builder.charAt(builder.length()-2)=='d'){
                    builder.delete(builder.length()-5,builder.length());
                    builder.append(" or ");
                }
                builder.append("( " + fieldFragmentEntry.fragment + " ) or ");
                continue;
            }
            Property property = entity.getPropertyByFieldName(fieldFragmentEntry.field);
            if(null==property){
                builder.append(fieldFragmentEntry.fragment.replace("{}", fieldFragmentEntry.field));
            }else if(!query.unionList.isEmpty()){
                builder.append(fieldFragmentEntry.fragment.replace("{}", query.quickDAOConfig.database.escape(property.column)));
            }else{
                builder.append(fieldFragmentEntry.fragment.replace("{}", tableAliasName + "." + query.quickDAOConfig.database.escape(property.column)));
            }
            builder.append(separator);
        }
        return builder.toString();
    }
}
