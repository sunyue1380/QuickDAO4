package cn.schoolwow.quickdao.query.response;

import cn.schoolwow.quickdao.domain.*;
import cn.schoolwow.quickdao.exception.SQLRuntimeException;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AbstractResponse<T> implements Response<T>{
    private Logger logger = LoggerFactory.getLogger(AbstractResponse.class);
    /**查询对象参数*/
    public Query query;

    public AbstractResponse(Query query) {
        this.query = query;
    }

    @Override
    public long count() {
        long count = 0;
        query.parameterIndex = 1;
        try {
            ConnectionExecutorItem connectionExecutorItem = query.dqlBuilder.count(query);
            ResultSet resultSet = query.dqlBuilder.connectionExecutor.executeQuery(connectionExecutorItem);
            if (resultSet.next()) {
                count = resultSet.getLong(1);
            }
            resultSet.close();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
        query.parameterIndex = 1;
        return count;
    }

    @Override
    public int insert() {
        int count = 0;
        try {
            if(null!=query.insertArray){
                ConnectionExecutorItem[] connectionExecutorItems = query.dqlBuilder.insertArray(query);
                for(int i=0;i<connectionExecutorItems.length;i++){
                    count += query.dqlBuilder.connectionExecutor.executeUpdate(connectionExecutorItems[i]);
                    if(count>0){
                        ResultSet rs = connectionExecutorItems[i].preparedStatement.getGeneratedKeys();
                        if (rs.next()) {
                            query.insertArray.getJSONObject(i).put("generatedKeys",rs.getString(1));
                        }
                        rs.close();
                    }
                    connectionExecutorItems[i].preparedStatement.close();
                }
                query.dqlBuilder.connectionExecutor.connection.commit();
            }else{
                ConnectionExecutorItem connectionExecutorItem = query.dqlBuilder.insert(query);
                count = query.dqlBuilder.connectionExecutor.executeUpdate(connectionExecutorItem);
                if (count>0&&null!=query.insertValue) {
                    ResultSet rs = connectionExecutorItem.preparedStatement.getGeneratedKeys();
                    if (rs.next()) {
                        query.insertValue.put("generatedKeys",rs.getString(1));
                    }
                    rs.close();
                }
                connectionExecutorItem.preparedStatement.close();
            }
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
        return count;
    }

    @Override
    public int update() {
        int count = 0;
        try {
            ConnectionExecutorItem connectionExecutorItem = query.dqlBuilder.update(query);
            count = query.dqlBuilder.connectionExecutor.executeUpdate(connectionExecutorItem);
            connectionExecutorItem.preparedStatement.close();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
        return count;
    }

    @Override
    public int delete() {
        int count = 0;
        try {
            ConnectionExecutorItem connectionExecutorItem = query.dqlBuilder.delete(query);
            count = query.dqlBuilder.connectionExecutor.executeUpdate(connectionExecutorItem);
            connectionExecutorItem.preparedStatement.close();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
        return count;
    }

    @Override
    public T getOne() {
        List<T> list = getList();
        if (list == null || list.size() == 0) {
            return null;
        } else {
            return list.get(0);
        }
    }

    @Override
    public <E> E getOne(Class<E> clazz) {
        List<E> list = getList(clazz);
        if (list == null || list.size() == 0) {
            return null;
        } else {
            return list.get(0);
        }
    }

    @Override
    public <E> E getSingleColumn(Class<E> clazz) {
        List<E> list = getSingleColumnList(clazz);
        if (list == null || list.size() == 0) {
            return null;
        } else {
            return list.get(0);
        }
    }

    @Override
    public <E> List<E> getSingleColumnList(Class<E> clazz) {
        try {
            JSONArray array = new JSONArray(query.dqlBuilder.getResultSetRowCount(query));
            ConnectionExecutorItem connectionExecutorItem = query.dqlBuilder.getArray(query);
            ResultSet resultSet = query.dqlBuilder.connectionExecutor.executeQuery(connectionExecutorItem);
            while (resultSet.next()) {
                array.add(resultSet.getObject(1));
            }
            resultSet.close();
            connectionExecutorItem.preparedStatement.close();
            return array.toJavaList(clazz);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public List getList() {
        return getList(query.entity.clazz);
    }

    @Override
    public <E> List<E> getList(Class<E> clazz) {
        return getArray().toJavaList(clazz);
    }

    @Override
    public PageVo<T> getPagingList() {
        return getPagingList(query.entity.clazz);
    }

    @Override
    public <E> PageVo<E> getPagingList(Class<E> clazz) {
        query.pageVo.setList(getArray().toJavaList(clazz));
        setPageVo();
        return query.pageVo;
    }

    @Override
    public JSONObject getObject() {
        JSONArray array = getArray();
        if(null==array||array.isEmpty()){
            return null;
        }
        return array.getJSONObject(0);
    }

    @Override
    public JSONArray getArray() {
        JSONArray array = null;
        try {
            array = new JSONArray(query.dqlBuilder.getResultSetRowCount(query));
            ConnectionExecutorItem connectionExecutorItem = query.dqlBuilder.getArray(query);
            ResultSet resultSet = query.dqlBuilder.connectionExecutor.executeQuery(connectionExecutorItem);
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
                        getCompositObject(resultSet,o);
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

    @Override
    public String toString() {
        return query.toString();
    }

    /**设置分页对象*/
    private void setPageVo() {
        if (query.pageVo == null) {
            throw new IllegalArgumentException("请先调用page()函数!");
        }
        query.pageVo.setTotalSize(count());
        query.pageVo.setTotalPage((int)(query.pageVo.getTotalSize() / query.pageVo.getPageSize() + (query.pageVo.getTotalSize() % query.pageVo.getPageSize() > 0 ? 1 : 0)));
        query.pageVo.setHasMore(query.pageVo.getCurrentPage() < query.pageVo.getTotalPage());
    }

    /**
     * 获取复杂对象
     * @param resultSet 结果集
     * @param o 复杂对象
     * */
    private void getCompositObject(ResultSet resultSet, JSONObject o) throws SQLException {
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
}
