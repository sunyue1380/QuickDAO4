package cn.schoolwow.quickdao.builder.dql;

import cn.schoolwow.quickdao.annotation.IdStrategy;
import cn.schoolwow.quickdao.builder.AbstractSQLBuilder;
import cn.schoolwow.quickdao.domain.*;
import cn.schoolwow.quickdao.query.condition.AbstractCondition;
import com.alibaba.fastjson.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AbstractDQLBuilder extends AbstractSQLBuilder implements DQLBuilder {
    public AbstractDQLBuilder(QuickDAOConfig quickDAOConfig) {
        super(quickDAOConfig);
    }

    @Override
    public ConnectionExecutorItem fetchNull(Class clazz, String field) throws SQLException {
        String key = "fetchNull_" + clazz.getName()+"_"+field+"_"+quickDAOConfig.database.getClass().getSimpleName();
        if (!quickDAOConfig.sqlCache.containsKey(key)) {
            Entity entity = quickDAOConfig.getEntityByClassName(clazz.getName());
            StringBuilder builder = new StringBuilder("select ");
            builder.append(columns(entity,"t"));
            builder.append(" from " + entity.escapeTableName + " as t where t." + quickDAOConfig.database.escape(entity.getColumnNameByFieldName(field)) +" is null");
            quickDAOConfig.sqlCache.put(key, builder.toString());
        }
        String sql = quickDAOConfig.sqlCache.get(key);
        ConnectionExecutorItem connectionExecutorItem = connectionExecutor.newConnectionExecutorItem("Null查询",sql);
        return connectionExecutorItem;
    }

    @Override
    public ConnectionExecutorItem fetch(Class clazz, long id) throws SQLException {
        Entity entity = quickDAOConfig.getEntityByClassName(clazz.getName());
        return fetch(clazz,entity.id.column,id+"");
    }

    @Override
    public ConnectionExecutorItem fetch(Class clazz, String field, Object value) throws SQLException {
        String key = "fetch_" + clazz.getName()+"_"+field+"_"+quickDAOConfig.database.getClass().getSimpleName();
        if (!quickDAOConfig.sqlCache.containsKey(key)) {
            Entity entity = quickDAOConfig.getEntityByClassName(clazz.getName());
            StringBuilder builder = new StringBuilder("select ");
            builder.append(columns(entity,"t"));
            Property property = entity.getPropertyByFieldName(field);
            builder.append(" from " + entity.escapeTableName + " t where t." + quickDAOConfig.database.escape(entity.getColumnNameByFieldName(field)) + " = "+(null==property||null==property.function?"?":property.function)+"");
            quickDAOConfig.sqlCache.put(key, builder.toString());
        }
        String sql = quickDAOConfig.sqlCache.get(key);
        ConnectionExecutorItem connectionExecutorItem = connectionExecutor.newConnectionExecutorItem("根据单个字段查询",sql);
        connectionExecutorItem.preparedStatement.setObject(1,value);
        connectionExecutorItem.sql = sql.replace("?",(value instanceof String)?"'"+value.toString()+"'":value.toString());
        return connectionExecutorItem;
    }

    @Override
    public ConnectionExecutorItem fetchNull(String tableName, String field) throws SQLException {
        String key = "fetchNull_" + tableName+"_"+field+"_"+quickDAOConfig.database.getClass().getSimpleName();
        if (!quickDAOConfig.sqlCache.containsKey(key)) {
            Entity dbEntity = quickDAOConfig.getDbEntityByTableName(tableName);
            StringBuilder builder = new StringBuilder("select ");
            builder.append(columns(dbEntity,"t"));
            builder.append(" from " + dbEntity.escapeTableName + " as t where t." + quickDAOConfig.database.escape(dbEntity.getColumnNameByFieldName(field)) +" is null");
            quickDAOConfig.sqlCache.put(key, builder.toString());
        }
        String sql = quickDAOConfig.sqlCache.get(key);
        ConnectionExecutorItem connectionExecutorItem = connectionExecutor.newConnectionExecutorItem("Null查询",sql);
        return connectionExecutorItem;
    }

    @Override
    public ConnectionExecutorItem fetch(String tableName, String field, Object value) throws SQLException {
        String key = "fetch_" + tableName+"_"+field+"_"+quickDAOConfig.database.getClass().getSimpleName();
        if (!quickDAOConfig.sqlCache.containsKey(key)) {
            Entity dbEntity = quickDAOConfig.getDbEntityByTableName(tableName);
            StringBuilder builder = new StringBuilder("select ");
            builder.append(columns(dbEntity,"t"));
            builder.append(" from " + dbEntity.escapeTableName + " t where t." + quickDAOConfig.database.escape(dbEntity.getColumnNameByFieldName(field)) + " = ?");
            quickDAOConfig.sqlCache.put(key, builder.toString());
        }
        String sql = quickDAOConfig.sqlCache.get(key);
        ConnectionExecutorItem connectionExecutorItem = connectionExecutor.newConnectionExecutorItem("根据单个字段查询",sql);
        connectionExecutorItem.preparedStatement.setObject(1,value);
        connectionExecutorItem.sql = sql.replace("?",(value instanceof String)?"'"+value.toString()+"'":value.toString());
        return connectionExecutorItem;
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
            builder.append(" " + query.tableAliasName);
        }
        addJoinTableStatement(query,builder);
        builder.append(" " + query.where + " " + query.groupBy + " " + query.having + " ) foo");
        String sql = builder.toString();
        ConnectionExecutorItem connectionExecutorItem = connectionExecutor.newConnectionExecutorItem("获取行数",sql);
        builder = new StringBuilder(sql.replace("?",PLACEHOLDER));
        addArraySQLParameters(connectionExecutorItem.preparedStatement,query,query,builder);
        connectionExecutorItem.sql = builder.toString();
        ResultSet resultSet = connectionExecutor.executeQuery(connectionExecutorItem);
        int count = -1;
        if (resultSet.next()) {
            count = resultSet.getInt(1);
        }
        connectionExecutor.count = count;
        resultSet.close();
        query.parameterIndex = 1;
        return count;
    }

    @Override
    public ConnectionExecutorItem count(Query query) throws SQLException {
        StringBuilder builder = new StringBuilder("select count(1) from ( select " + query.distinct + " ");
        //如果有指定列,则添加指定列
        if(query.column.length()>0){
            builder.append(query.column);
        }else{
            builder.append(columns(query.entity, query.tableAliasName));
        }
        builder.append(" from " + query.entity.escapeTableName);
        if(null!=query.entity.clazz){
            builder.append(" " + query.tableAliasName);
        }
        addJoinTableStatement(query,builder);
        builder.append(" " + query.where + " " + query.groupBy + " " + query.having + " ) foo");
        String sql = builder.toString();

        ConnectionExecutorItem connectionExecutorItem = connectionExecutor.newConnectionExecutorItem("获取总行数",sql);
        builder = new StringBuilder(sql.replace("?",PLACEHOLDER));
        addArraySQLParameters(connectionExecutorItem.preparedStatement,query,query,builder);
        connectionExecutorItem.sql = builder.toString();
        return connectionExecutorItem;
    }

    @Override
    public ConnectionExecutorItem insert(Query query) throws SQLException {
        StringBuilder builder = new StringBuilder("insert into " + query.entity.escapeTableName + "(");
        builder.append(query.insertBuilder.toString()+") values(");
        for(int i=0;i<query.insertParameterList.size();i++){
            builder.append("?,");
        }
        builder.deleteCharAt(builder.length()-1);
        builder.append(")");
        String sql = builder.toString();
        ConnectionExecutorItem connectionExecutorItem = connectionExecutor.newConnectionExecutorItem("插入记录",sql);
        builder = new StringBuilder(sql.replace("?",PLACEHOLDER));
        for (Object parameter : query.insertParameterList) {
            setParameter(parameter,connectionExecutorItem.preparedStatement,query.parameterIndex++,builder);
        }
        connectionExecutorItem.sql = builder.toString();
        return connectionExecutorItem;
    }

    @Override
    public ConnectionExecutorItem[] insertArray(Query query) throws SQLException {
        String sql = insertArraySQL(query);
        connectionExecutor.connection.setAutoCommit(false);
        ConnectionExecutorItem[] connectionExecutorItems = new ConnectionExecutorItem[query.insertArray.size()];
        StringBuilder builder = new StringBuilder();
        for(int i=0;i<connectionExecutorItems.length;i++){
            ConnectionExecutorItem connectionExecutorItem = connectionExecutor.newConnectionExecutorItem("插入记录",sql);
            StringBuilder sqlBuilder = new StringBuilder(sql.replace("?",PLACEHOLDER));
            JSONObject o = query.insertArray.getJSONObject(i);
            int parameterIndex = 1;
            for(int j=0;j<query.entity.properties.size();j++){
                Property property = query.entity.properties.get(j);
                if(property.id&&property.strategy.equals(IdStrategy.AutoIncrement)){
                    continue;
                }
                setParameter(o.get(property.column),connectionExecutorItem.preparedStatement,parameterIndex++,sqlBuilder);
            }
            builder.append(sqlBuilder.toString()+";");
            connectionExecutorItem.sql = sqlBuilder.toString();
            connectionExecutorItems[i] = connectionExecutorItem;
        }
        return connectionExecutorItems;
    }

    @Override
    public int insertArrayBatch(Query query) throws SQLException {
        String sql = insertArraySQL(query);

        connectionExecutor.connection.setAutoCommit(false);
        ConnectionExecutorItem connectionExecutorItem = connectionExecutor.newConnectionExecutorItem("批量插入记录",sql);
        int effect = 0;
        int perBatchCommit = Math.max(query.perBatchCommit,quickDAOConfig.perBatchCommit);
        int length = query.insertArray.size();
        for(int i=0;i<length;i++){
            StringBuilder sqlBuilder = new StringBuilder(sql.replace("?",PLACEHOLDER));
            JSONObject o = query.insertArray.getJSONObject(i);
            int parameterIndex = 1;
            for(int j=0;j<query.entity.properties.size();j++){
                Property property = query.entity.properties.get(j);
                if(property.id&&property.strategy.equals(IdStrategy.AutoIncrement)){
                    continue;
                }
                setParameter(o.get(property.column),connectionExecutorItem.preparedStatement,parameterIndex++,sqlBuilder);
            }
            connectionExecutorItem.preparedStatement.addBatch();
            if((i!=0&&i%perBatchCommit==0)||i==length-1){
                int[] batches = connectionExecutorItem.preparedStatement.executeBatch();
                for(int batch:batches){
                    switch (batch){
                        case Statement.SUCCESS_NO_INFO:{
                            effect += 1;
                        }break;
                        case Statement.EXECUTE_FAILED:{}break;
                        default:{
                            effect += batch;
                        };
                    }
                }
                connectionExecutor.connection.commit();
                connectionExecutorItem.preparedStatement.clearBatch();
            }
        }
        connectionExecutorItem.preparedStatement.close();
        return effect;
    }

    @Override
    public ConnectionExecutorItem update(Query query) throws SQLException {
        StringBuilder builder = new StringBuilder("update " + query.entity.escapeTableName + " ");
        builder.append(query.setBuilder.toString() + " " + query.where.replace(query.tableAliasName + ".",""));

        String sql = builder.toString();
        ConnectionExecutorItem connectionExecutorItem = connectionExecutor.newConnectionExecutorItem("批量更新",sql);
        builder = new StringBuilder(sql.replace("?",PLACEHOLDER));
        for (Object parameter : query.updateParameterList) {
            setParameter(parameter,connectionExecutorItem.preparedStatement,query.parameterIndex++,builder);
        }
        addMainTableParameters(connectionExecutorItem.preparedStatement,query,query,builder);
        connectionExecutorItem.sql = builder.toString();
        return connectionExecutorItem;
    }

    @Override
    public ConnectionExecutorItem delete(Query query) throws SQLException {
        StringBuilder builder = new StringBuilder("delete from " + query.quickDAOConfig.database.escape(query.entity.tableName));
        builder.append(" " + query.where.replace(query.tableAliasName+".",""));

        String sql = builder.toString();
        ConnectionExecutorItem connectionExecutorItem = connectionExecutor.newConnectionExecutorItem("批量删除",sql);
        builder = new StringBuilder(sql.replace("?",PLACEHOLDER));
        addMainTableParameters(connectionExecutorItem.preparedStatement,query,query,builder);
        connectionExecutorItem.sql = builder.toString();
        return connectionExecutorItem;
    }

    @Override
    public ConnectionExecutorItem getArray(Query query) throws SQLException {
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
                    default:{
                        throw new IllegalArgumentException("不支持的Union类型!当前类型:"+abstractCondition.query.unionType);
                    }
                }
                builder.append(getUnionArraySQL(abstractCondition.query));
            }
            builder.append(" " + query.orderBy + " " + query.limit);
        }
        String sql = builder.toString();

        ConnectionExecutorItem connectionExecutorItem = connectionExecutor.newConnectionExecutorItem("获取列表",sql);
        builder = new StringBuilder(sql.replace("?",PLACEHOLDER));
        addArraySQLParameters(connectionExecutorItem.preparedStatement,query,query,builder);
        //添加union语句
        for(AbstractCondition abstractCondition:query.unionList){
            Query unionQuery = abstractCondition.query;
            for(SubQuery subQuery:unionQuery.subQueryList){
                if(null!=subQuery.subQuery){
                    addMainTableParameters(connectionExecutorItem.preparedStatement,subQuery.subQuery,query,builder);
                }
            }
            addMainTableParameters(connectionExecutorItem.preparedStatement,unionQuery,query,builder);
            for (Object parameter : unionQuery.havingParameterList) {
                setParameter(parameter,connectionExecutorItem.preparedStatement,query.parameterIndex++,builder);
            }
        }
        connectionExecutorItem.sql = builder.toString();
        return connectionExecutorItem;
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
            builder.append(" " + query.tableAliasName);
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
            sqlBuilder.append(" " + subQuery.tableAliasName);
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
     * 获取批量插入SQL语句
     * */
    private String insertArraySQL(Query query){
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
        return builder.toString();
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
