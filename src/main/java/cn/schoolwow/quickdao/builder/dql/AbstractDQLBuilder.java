package cn.schoolwow.quickdao.builder.dql;

import cn.schoolwow.quickdao.annotation.IdStrategy;
import cn.schoolwow.quickdao.builder.AbstractSQLBuilder;
import cn.schoolwow.quickdao.domain.*;
import cn.schoolwow.quickdao.query.condition.AbstractCondition;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.MDC;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AbstractDQLBuilder extends AbstractSQLBuilder implements DQLBuilder {
    public AbstractDQLBuilder(QuickDAOConfig quickDAOConfig) {
        super(quickDAOConfig);
    }

    @Override
    public PreparedStatement fetchNull(Class clazz, String field) throws SQLException {
        String key = "fetchNull_" + clazz.getName()+"_"+field+"_"+quickDAOConfig.database.getClass().getSimpleName();
        if (!quickDAOConfig.sqlCache.containsKey(key)) {
            Entity entity = quickDAOConfig.getEntityByClassName(clazz.getName());
            StringBuilder builder = new StringBuilder("select ");
            builder.append(columns(entity,"t"));
            builder.append(" from " + entity.escapeTableName + " as t where t." + quickDAOConfig.database.escape(entity.getColumnNameByFieldName(field)) +" is null");
            quickDAOConfig.sqlCache.put(key, builder.toString());
        }
        String sql = quickDAOConfig.sqlCache.get(key);
        MDC.put("name","Null查询");
        MDC.put("sql",sql);
        PreparedStatement ps = connection.prepareStatement(sql);
        return ps;
    }

    @Override
    public PreparedStatement fetch(Class clazz, long id) throws SQLException {
        Entity entity = quickDAOConfig.getEntityByClassName(clazz.getName());
        return fetch(clazz,entity.id.column,id+"");
    }

    @Override
    public PreparedStatement fetch(Class clazz, String field, Object value) throws SQLException {
        String key = "fetch_" + clazz.getName()+"_"+field+"_"+quickDAOConfig.database.getClass().getSimpleName();
        if (!quickDAOConfig.sqlCache.containsKey(key)) {
            Entity entity = quickDAOConfig.getEntityByClassName(clazz.getName());
            StringBuilder builder = new StringBuilder("select ");
            builder.append(columns(entity,"t"));
            Property property = entity.getPropertyByFieldName(field);
            builder.append(" from " + entity.escapeTableName + " as t where t." + quickDAOConfig.database.escape(entity.getColumnNameByFieldName(field)) + " = "+(null==property||null==property.function?"?":property.function)+"");
            quickDAOConfig.sqlCache.put(key, builder.toString());
        }
        String sql = quickDAOConfig.sqlCache.get(key);
        MDC.put("name","字段查询");
        MDC.put("sql",sql);
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setObject(1,value);
        MDC.put("sql",sql.replace("?",(value instanceof String)?"'"+value.toString()+"'":value.toString()));
        return ps;
    }

    @Override
    public PreparedStatement fetchNull(String tableName, String field) throws SQLException {
        String key = "fetchNull_" + tableName+"_"+field+"_"+quickDAOConfig.database.getClass().getSimpleName();
        if (!quickDAOConfig.sqlCache.containsKey(key)) {
            Entity dbEntity = quickDAOConfig.getDbEntityByTableName(tableName);
            StringBuilder builder = new StringBuilder("select ");
            builder.append(columns(dbEntity,"t"));
            builder.append(" from " + dbEntity.escapeTableName + " as t where t." + quickDAOConfig.database.escape(dbEntity.getColumnNameByFieldName(field)) +" is null");
            quickDAOConfig.sqlCache.put(key, builder.toString());
        }
        String sql = quickDAOConfig.sqlCache.get(key);
        MDC.put("name","Null查询");
        MDC.put("sql",sql);
        PreparedStatement ps = connection.prepareStatement(sql);
        return ps;
    }

    @Override
    public PreparedStatement fetch(String tableName, String field, Object value) throws SQLException {
        String key = "fetch_" + tableName+"_"+field+"_"+quickDAOConfig.database.getClass().getSimpleName();
        if (!quickDAOConfig.sqlCache.containsKey(key)) {
            Entity dbEntity = quickDAOConfig.getDbEntityByTableName(tableName);
            StringBuilder builder = new StringBuilder("select ");
            builder.append(columns(dbEntity,"t"));
            builder.append(" from " + dbEntity.escapeTableName + " as t where t." + quickDAOConfig.database.escape(dbEntity.getColumnNameByFieldName(field)) + " = ?");
            quickDAOConfig.sqlCache.put(key, builder.toString());
        }
        String sql = quickDAOConfig.sqlCache.get(key);
        MDC.put("name","字段查询");
        MDC.put("sql",sql);
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setObject(1,value);
        MDC.put("sql",sql.replace("?",(value instanceof String)?"'"+value.toString()+"'":value.toString()));
        return ps;
    }

    @Override
    public int getResultSetRowCount(Query query) throws SQLException {
        query.parameterIndex = 1;
        StringBuilder builder = new StringBuilder("select count(1) from ( select " + query.distinct + " ");
        //如果有指定列,则添加指定列
        if(query.column.length()>0){
            builder.append(query.column);
        }else{
            builder.append(columns(query.entity, query.tableAliasName));
        }
        builder.append(" from " + query.entity.escapeTableName);
        if(null!=query.entity.clazz){
            builder.append(" as " + query.tableAliasName);
        }
        addJoinTableStatement(query,builder);
        builder.append(" " + query.where + " " + query.groupBy + " " + query.having);
        builder.append(" "+query.limit);
        builder.append(") as foo");

        PreparedStatement ps = connection.prepareStatement(builder.toString());
        builder = new StringBuilder(builder.toString().replace("?",PLACEHOLDER));
        addArraySQLParameters(ps,query,query,builder);
        ResultSet resultSet = ps.executeQuery();
        int count = -1;
        if (resultSet.next()) {
            count = resultSet.getInt(1);
        }
        resultSet.close();
        ps.close();
        MDC.put("count",count+"");
        query.parameterIndex = 1;
        return count;
    }

    @Override
    public PreparedStatement count(Query query) throws SQLException {
        StringBuilder builder = new StringBuilder("select count(1) from ( select " + query.distinct + " ");
        //如果有指定列,则添加指定列
        if(query.column.length()>0){
            builder.append(query.column);
        }else{
            builder.append(columns(query.entity, query.tableAliasName));
        }
        builder.append(" from " + query.entity.escapeTableName);
        if(null!=query.entity.clazz){
            builder.append(" as " + query.tableAliasName);
        }
        addJoinTableStatement(query,builder);
        builder.append(" " + query.where + " " + query.groupBy + " " + query.having);
        builder.append(") as foo");

        PreparedStatement ps = connection.prepareStatement(builder.toString());
        builder = new StringBuilder(builder.toString().replace("?",PLACEHOLDER));
        addArraySQLParameters(ps,query,query,builder);
        return ps;
    }

    @Override
    public PreparedStatement insert(Query query) throws SQLException {
        StringBuilder builder = new StringBuilder("insert into " + query.entity.escapeTableName + "(");
        builder.append(query.insertBuilder.toString()+") values(");
        for(int i=0;i<query.insertParameterList.size();i++){
            builder.append("?,");
        }
        builder.deleteCharAt(builder.length()-1);
        builder.append(")");
        MDC.put("name","插入记录");
        String sql = builder.toString();
        MDC.put("sql",sql);

        PreparedStatement ps = connection.prepareStatement(sql,PreparedStatement.RETURN_GENERATED_KEYS);
        builder = new StringBuilder(sql.replace("?",PLACEHOLDER));
        for (Object parameter : query.insertParameterList) {
            setParameter(parameter,ps,query.parameterIndex++,builder);
        }
        MDC.put("sql",builder.toString());
        return ps;
    }

    @Override
    public PreparedStatement[] insertArray(Query query) throws SQLException {
        StringBuilder builder = new StringBuilder("insert into " + query.entity.escapeTableName + "(");
        List<Property> properties = query.entity.properties;
        for(Property property:properties){
            if(property.id&&property.strategy.equals(IdStrategy.AutoIncrement)){
                continue;
            }
            builder.append(query.quickDAOConfig.database.escape(property.column) + ",");
        }
        builder.deleteCharAt(builder.length()-1);
        builder.append(") values(");
        for(Property property:properties){
            if(property.id&&property.strategy.equals(IdStrategy.AutoIncrement)){
                continue;
            }
            builder.append("?,");
        }
        builder.deleteCharAt(builder.length()-1);
        builder.append(")");
        MDC.put("name","插入记录");
        String sql = builder.toString();
        MDC.put("sql",sql);

        connection.setAutoCommit(false);
        JSONArray array = query.insertArray;
        PreparedStatement[] preparedStatements = new PreparedStatement[array.size()];

        builder.setLength(0);
        for(int i=0;i<array.size();i++){
            PreparedStatement ps = connection.prepareStatement(sql,PreparedStatement.RETURN_GENERATED_KEYS);
            StringBuilder sqlBuilder = new StringBuilder(sql.replace("?",PLACEHOLDER));
            JSONObject o = array.getJSONObject(i);
            int parameterIndex = 1;
            for(int j=0;j<properties.size();j++){
                Property property = properties.get(j);
                if(property.id&&property.strategy.equals(IdStrategy.AutoIncrement)){
                    continue;
                }
                setParameter(o.get(property.column),ps,parameterIndex++,sqlBuilder);
            }
            builder.append(sqlBuilder.toString()+";");
            preparedStatements[i] = ps;
        }
        MDC.put("sql",builder.toString());
        return preparedStatements;
    }

    @Override
    public PreparedStatement update(Query query) throws SQLException {
        StringBuilder builder = new StringBuilder("update " + query.entity.escapeTableName + " as t ");
        addJoinTableStatement(query,builder);
        builder.append(query.setBuilder.toString() + " " + query.where);
        MDC.put("name","批量更新");
        String sql = builder.toString();
        MDC.put("sql",sql);

        PreparedStatement ps = connection.prepareStatement(sql);
        builder = new StringBuilder(sql.replace("?",PLACEHOLDER));
        for (Object parameter : query.updateParameterList) {
            setParameter(parameter,ps,query.parameterIndex++,builder);
        }
        addMainTableParameters(ps,query,query,builder);
        MDC.put("sql",builder.toString());
        return ps;
    }

    @Override
    public PreparedStatement delete(Query query) throws SQLException {
        StringBuilder builder = new StringBuilder("delete t from " + query.entity.escapeTableName + " as t");
        addJoinTableStatement(query,builder);
        builder.append(" " + query.where);

        MDC.put("name","批量删除");
        MDC.put("sql",builder.toString());

        PreparedStatement ps = connection.prepareStatement(builder.toString());
        builder = new StringBuilder(builder.toString().replace("?",PLACEHOLDER));
        addMainTableParameters(ps,query,query,builder);
        MDC.put("sql",builder.toString());
        return ps;
    }

    @Override
    public PreparedStatement getArray(Query query) throws SQLException {
        StringBuilder builder = null;
        if(query.unionList.isEmpty()){
            builder = getArraySQL(query);
        }else{
            builder = getUnionArraySQL(query);
            for(AbstractCondition abstractCondition:query.unionList){
                switch(abstractCondition.query.unionType){
                    case Union:{
                        builder.append(" union ");
                    }break;
                    case UnionAll:{
                        builder.append(" union all ");
                    }break;
                }
                builder.append(getUnionArraySQL(abstractCondition.query));
            }
            builder.append(" " + query.orderBy + " " + query.limit);
        }
        MDC.put("name","获取列表");
        String sql = builder.toString();
        MDC.put("sql",sql);

        PreparedStatement ps = connection.prepareStatement(sql);
        builder = new StringBuilder(builder.toString().replace("?",PLACEHOLDER));
        addArraySQLParameters(ps,query,query,builder);
        //添加union语句
        for(AbstractCondition abstractCondition:query.unionList){
            Query unionQuery = abstractCondition.query;
            for(SubQuery subQuery:unionQuery.subQueryList){
                if(null!=subQuery.subQuery){
                    addMainTableParameters(ps,subQuery.subQuery,query,builder);
                }
            }
            addMainTableParameters(ps,unionQuery,query,builder);
            for (Object parameter : unionQuery.havingParameterList) {
                setParameter(parameter,ps,query.parameterIndex++,builder);
            }
        }
        MDC.put("sql",builder.toString());
        return ps;
    }

    @Override
    public StringBuilder getArraySQL(Query query) {
        StringBuilder builder = getUnionArraySQL(query);
        builder.append(" " + query.orderBy + " " + query.limit);
        return builder;
    }

    /**
     * 获取Union查询时的SQL语句
     * */
    private StringBuilder getUnionArraySQL(Query query){
        StringBuilder builder = new StringBuilder("select " + query.distinct + " ");
        //如果有指定列,则添加指定列
        if(query.column.length()>0){
            builder.append(query.column);
        }else{
            builder.append(columns(query.entity, query.tableAliasName));
        }
        if(query.compositField){
            for (SubQuery subQuery : query.subQueryList) {
                builder.append("," + columns(subQuery.entity, subQuery.tableAliasName));
            }
        }
        builder.append(" from " + query.entity.escapeTableName);
        if(null!=query.entity.clazz){
            builder.append(" as " + query.tableAliasName);
        }
        addJoinTableStatement(query,builder);
        builder.append(" " + query.where + " " + query.groupBy + " " + query.having);
        return builder;
    }

    /**
     * 添加外键关联查询条件
     */
    private void addJoinTableStatement(Query query, StringBuilder sqlBuilder) {
        for (SubQuery subQuery : query.subQueryList) {
            sqlBuilder.append(" " + subQuery.join + " ");
            if(null==subQuery.subQuerySQLBuilder){
                sqlBuilder.append(query.quickDAOConfig.database.escape(subQuery.entity.tableName));
            }else{
                sqlBuilder.append("(" + subQuery.subQuerySQLBuilder.toString() + ")");
            }
            sqlBuilder.append(" as " + subQuery.tableAliasName);
            if(null!=subQuery.primaryField&&null!=subQuery.joinTableField){
                sqlBuilder.append(" on ");
                if (subQuery.parentSubQuery == null) {
                    sqlBuilder.append(query.tableAliasName + "." + query.quickDAOConfig.database.escape(subQuery.primaryField) + " = " + subQuery.tableAliasName + "." + query.quickDAOConfig.database.escape(subQuery.joinTableField) + " ");
                }else{
                    sqlBuilder.append(subQuery.tableAliasName + "." + query.quickDAOConfig.database.escape(subQuery.joinTableField) + " = " + subQuery.parentSubQuery.tableAliasName + "." + query.quickDAOConfig.database.escape(subQuery.primaryField) + " ");
                }
                if(!subQuery.onConditionMap.isEmpty()){
                    Set<Map.Entry<String,String>> entrySet = subQuery.onConditionMap.entrySet();
                    for(Map.Entry<String,String> entry:entrySet){
                        sqlBuilder.append(" and ");
                        if (subQuery.parentSubQuery == null) {
                            sqlBuilder.append(query.tableAliasName + "." + query.quickDAOConfig.database.escape(entry.getKey()) + " = " + subQuery.tableAliasName + "." + query.quickDAOConfig.database.escape(entry.getValue()) + " ");
                        }else{
                            sqlBuilder.append(subQuery.tableAliasName + "." + query.quickDAOConfig.database.escape(entry.getValue()) + " = " + subQuery.parentSubQuery.tableAliasName + "." + query.quickDAOConfig.database.escape(entry.getKey()) + " ");
                        }
                    }
                }
            }
        }
    }

    /**
     * 添加Array语句参数
     * @param ps prepareStatment对象
     * @param query 当前query对象
     * @param mainQuery 主query
     * @param builder SQL语句拼接
     * */
    private void addArraySQLParameters(PreparedStatement ps, Query query, Query mainQuery, StringBuilder builder) throws SQLException {
        for(Query selectQuery:query.selectQueryList){
            addArraySQLParameters(ps,selectQuery,mainQuery,builder);
        }
        //from子查询
        if(null!=query.fromQuery){
            addArraySQLParameters(ps,query.fromQuery,mainQuery,builder);
        }
        //关联子查询
        for(SubQuery subQuery:query.subQueryList){
            if(null!=subQuery.subQuery){
                addArraySQLParameters(ps,subQuery.subQuery,mainQuery,builder);
            }
        }
        addMainTableParameters(ps,query,mainQuery,builder);
    }

    /**
     * 添加主表参数
     */
    protected void addMainTableParameters(PreparedStatement ps, Query query, Query mainQuery, StringBuilder sqlBuilder) throws SQLException {
        for (Object parameter : query.parameterList) {
            setParameter(parameter,ps,mainQuery.parameterIndex++,sqlBuilder);
        }
        for (Object parameter : query.havingParameterList) {
            setParameter(parameter,ps,mainQuery.parameterIndex++,sqlBuilder);
        }
    }

    /**
     * 返回列名的SQL语句
     */
    private String columns(Entity entity, String tableAlias) {
        String key = "columns_" + entity.tableName + "_" + tableAlias+quickDAOConfig.database.getClass().getName();
        if (!quickDAOConfig.sqlCache.containsKey(key)) {
            StringBuilder builder = new StringBuilder();
            for (Property property : entity.properties) {
                builder.append(tableAlias + "." + quickDAOConfig.database.escape(property.column) + " as " + tableAlias + "_" + property.column + ",");
            }
            builder.deleteCharAt(builder.length() - 1);
            quickDAOConfig.sqlCache.put(key, builder.toString());
        }
        return quickDAOConfig.sqlCache.get(key);
    }
}
