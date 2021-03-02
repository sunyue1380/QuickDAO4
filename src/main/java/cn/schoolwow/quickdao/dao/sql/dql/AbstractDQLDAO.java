package cn.schoolwow.quickdao.dao.sql.dql;

import cn.schoolwow.quickdao.builder.dql.AbstractDQLBuilder;
import cn.schoolwow.quickdao.dao.sql.AbstractSQLDAO;
import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import cn.schoolwow.quickdao.domain.SFunction;
import cn.schoolwow.quickdao.exception.SQLRuntimeException;
import cn.schoolwow.quickdao.util.LambdaUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.MDC;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
            PreparedStatement ps = null;
            if(null==value){
                ps = dqlBuilder.fetchNull(clazz,field);
            }else{
                ps = dqlBuilder.fetch(clazz,field,value);
            }
            Entity entity = quickDAOConfig.getEntityByClassName(clazz.getName());
            ResultSet resultSet = ps.executeQuery();
            JSONArray array = new JSONArray();
            while(resultSet.next()){
                array.add(quickDAOConfig.database.getObject(entity, "t",resultSet));
            }
            resultSet.close();
            ps.close();
            MDC.put("count",array.size()+"");
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
            PreparedStatement ps = null;
            if(null==value){
                ps = dqlBuilder.fetchNull(tableName,field);
            }else{
                ps = dqlBuilder.fetch(tableName,field,value);
            }
            Entity dbEntity = quickDAOConfig.getDbEntityByTableName(tableName);
            ResultSet resultSet = ps.executeQuery();
            JSONArray array = new JSONArray();
            while(resultSet.next()){
                array.add(quickDAOConfig.database.getObject(dbEntity, "t",resultSet));
            }
            resultSet.close();
            ps.close();
            MDC.put("count",array.size()+"");
            return array;
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }
}
