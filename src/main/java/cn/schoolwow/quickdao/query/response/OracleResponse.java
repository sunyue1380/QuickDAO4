package cn.schoolwow.quickdao.query.response;

import cn.schoolwow.quickdao.domain.*;
import cn.schoolwow.quickdao.exception.SQLRuntimeException;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OracleResponse<T> extends AbstractResponse<T>{

    public OracleResponse(Query query) {
        super(query);
    }

    @Override
    public List getList() {
        return getList(getResponseQuery().entity.clazz);
    }

    @Override
    public PageVo<T> getPagingList() {
        return getPagingList(getResponseQuery().entity.clazz);
    }

    /**
     * 执行SQL查询语句
     * */
    @Override
    public JSONArray getArray(){
        JSONArray array = null;
        try {
            array = new JSONArray(query.dqlBuilder.getResultSetRowCount(query));
            ConnectionExecutorItem connectionExecutorItem = query.dqlBuilder.getArray(query);
            ResultSet resultSet = query.dqlBuilder.connectionExecutor.executeQuery(connectionExecutorItem);
            Query query = getResponseQuery();
            if(query.column.length()>0){
                if(null==query.columnTypeMapping){
                    query.columnTypeMapping = query.quickDAOConfig.columnTypeMapping;
                }

                ResultSetMetaData metaData = resultSet.getMetaData();
                Property[] properties = new Property[metaData.getColumnCount()];
                for (int i = 1; i <= properties.length; i++) {
                    properties[i-1] = new Property();
                    properties[i-1].columnLabel = metaData.getColumnLabel(i);
                    properties[i-1].column = metaData.getColumnName(i);
                    properties[i-1].columnType = metaData.getColumnTypeName(i);
                    properties[i-1].className = metaData.getColumnClassName(i);
                    if(null!=query.columnTypeMapping){
                        Class type = query.columnTypeMapping.columnMappingType(properties[i-1]);
                        if(null!=type){
                            properties[i-1].clazz = type;
                        }
                    }
                }

                while (resultSet.next()) {
                    JSONObject o = new JSONObject(true);
                    for (int i = 1; i <= properties.length; i++) {
                        if(null==properties[i-1].clazz){
                            o.put(properties[i-1].columnLabel, resultSet.getObject(i));
                        }else{
                            o.put(properties[i-1].columnLabel, resultSet.getObject(i,properties[i-1].clazz));
                        }
                    }
                    array.add(o);
                }
            }else{
                while (resultSet.next()) {
                    JSONObject o = query.quickDAOConfig.database.getObject(query.entity, query.tableAliasName, resultSet);
                    if(query.compositField){
                        getCompositObject(resultSet,o,query);
                    }
                    array.add(o);
                }
            }
            resultSet.close();
            connectionExecutorItem.preparedStatement.close();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
        return array;
    }

    /**
     * 获取复杂对象
     * @param resultSet 结果集
     * @param o 复杂对象
     * */
    private void getCompositObject(ResultSet resultSet, JSONObject o, Query query) throws SQLException {
        for (SubQuery subQuery : query.subQueryList) {
            if(null==subQuery.compositField||subQuery.compositField.isEmpty()) {
                continue;
            }
            JSONObject subObject = query.quickDAOConfig.database.getObject(subQuery.entity, subQuery.tableAliasName, resultSet);
            SubQuery parentSubQuery = subQuery.parentSubQuery;
            if (parentSubQuery == null) {
                o.put(subQuery.compositField, subObject);
            } else {
                List<String> fieldNames = new ArrayList<>();
                while (parentSubQuery != null) {
                    fieldNames.add(parentSubQuery.compositField);
                    parentSubQuery = parentSubQuery.parentSubQuery;
                }
                JSONObject oo = o;
                for (int i = fieldNames.size() - 1; i >= 0; i--) {
                    oo = oo.getJSONObject(fieldNames.get(i));
                }
                oo.put(subQuery.compositField, subObject);
            }
        }
    }

    /**
     * 获取返回结果Query
     * */
    private Query getResponseQuery(){
        //oracle分页操作
        if("where rn >= ?".equals(query.where)){
            return query.fromQuery.fromQuery;
        }
        return this.query;
    }
}
