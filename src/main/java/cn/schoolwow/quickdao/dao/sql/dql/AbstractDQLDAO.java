package cn.schoolwow.quickdao.dao.sql.dql;

import cn.schoolwow.quickdao.builder.dql.AbstractDQLBuilder;
import cn.schoolwow.quickdao.dao.sql.AbstractSQLDAO;
import cn.schoolwow.quickdao.domain.ConnectionExecutorItem;
import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import cn.schoolwow.quickdao.domain.SFunction;
import cn.schoolwow.quickdao.exception.SQLRuntimeException;
import cn.schoolwow.quickdao.util.LambdaUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

public class AbstractDQLDAO extends AbstractSQLDAO implements DQLDAO {
    private AbstractDQLBuilder dqlBuilder;

    public AbstractDQLDAO(QuickDAOConfig quickDAOConfig) {
        super(quickDAOConfig);
        this.dqlBuilder = quickDAOConfig.database.getDQLBuilderInstance(quickDAOConfig);
        super.sqlBuilder = dqlBuilder;
    }

    @Override
    public <T> T fetch(Class<T> clazz, long id) {
        return fetch(clazz,quickDAOConfig.getEntityByClassName(clazz.getName()).id.column,id);
    }

    @Override
    public <T> T fetch(Class<T> clazz, String field, Object value) {
        List<T> list = fetchList(clazz,field,value);
        if(null==list||list.isEmpty()){
            return null;
        }
        return list.get(0);
    }

    @Override
    public <T> List<T> fetchList(Class<T> clazz, String field, Object value) {
        try {
            ConnectionExecutorItem connectionExecutorItem = null;
            if(null==value){
                connectionExecutorItem = dqlBuilder.fetchNull(clazz,field);
            }else{
                connectionExecutorItem = dqlBuilder.fetch(clazz,field,value);
            }
            Entity entity = quickDAOConfig.getEntityByClassName(clazz.getName());
            ResultSet resultSet = sqlBuilder.connectionExecutor.executeQuery(connectionExecutorItem);
            JSONArray array = new JSONArray();
            while(resultSet.next()){
                array.add(quickDAOConfig.database.getObject(entity, "t",resultSet));
            }
            resultSet.close();
            return array.toJavaList(clazz);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public <T> T fetch(Class<T> clazz, SFunction<T, ?> field, Object value) {
        try {
            String convertField = LambdaUtils.resolveLambdaProperty(field);
            return fetch(clazz,convertField,value);
        } catch (Exception e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public <T> List<T> fetchList(Class<T> clazz, SFunction<T, ?> field, Object value) {
        try {
            String convertField = LambdaUtils.resolveLambdaProperty(field);
            return fetchList(clazz,convertField,value);
        } catch (Exception e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public JSONObject fetch(String tableName, String field, Object value) {
        JSONArray array = fetchList(tableName,field,value);
        if(null==array||array.isEmpty()){
            return null;
        }
        return array.getJSONObject(0);
    }

    @Override
    public JSONArray fetchList(String tableName, String field, Object value) {
        try {
            ConnectionExecutorItem connectionExecutorItem = null;
            if(null==value){
                connectionExecutorItem = dqlBuilder.fetchNull(tableName,field);
            }else{
                connectionExecutorItem = dqlBuilder.fetch(tableName,field,value);
            }
            Entity dbEntity = quickDAOConfig.getDbEntityByTableName(tableName);
            ResultSet resultSet = sqlBuilder.connectionExecutor.executeQuery(connectionExecutorItem);
            JSONArray array = new JSONArray();
            while(resultSet.next()){
                array.add(quickDAOConfig.database.getObject(dbEntity, "t",resultSet));
            }
            resultSet.close();
            return array;
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public JSONArray select(String selectSQL, Object... parameters) {
        try {
            ConnectionExecutorItem connectionExecutorItem = dqlBuilder.select(selectSQL,parameters);
            ResultSet resultSet = dqlBuilder.connectionExecutor.executeQuery(connectionExecutorItem);
            ResultSetMetaData metaData = resultSet.getMetaData();
            String[] columnLables = new String[metaData.getColumnCount()];
            for(int i=1;i<=columnLables.length;i++){
                columnLables[i-1] = metaData.getColumnLabel(i);
            }
            JSONArray array = new JSONArray();
            while(resultSet.next()){
                JSONObject o = new JSONObject();
                for(int i=1;i<=columnLables.length;i++){
                    o.put(columnLables[i-1],resultSet.getObject(i));
                }
                array.add(o);
            }
            return array;
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }
}
