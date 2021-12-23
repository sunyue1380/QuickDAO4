package cn.schoolwow.quickdao.dao.sql.dml;

import cn.schoolwow.quickdao.annotation.IdStrategy;
import cn.schoolwow.quickdao.builder.dml.AbstractDMLBuilder;
import cn.schoolwow.quickdao.dao.sql.AbstractSQLDAO;
import cn.schoolwow.quickdao.domain.ConnectionExecutorItem;
import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import cn.schoolwow.quickdao.domain.SFunction;
import cn.schoolwow.quickdao.exception.SQLRuntimeException;
import cn.schoolwow.quickdao.util.LambdaUtils;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AbstractDMLDAO extends AbstractSQLDAO implements DMLDAO{
    private AbstractDMLBuilder dmlBuilder;

    public AbstractDMLDAO(QuickDAOConfig quickDAOConfig) {
        super(quickDAOConfig);
        this.dmlBuilder = new AbstractDMLBuilder(quickDAOConfig);
        super.sqlBuilder = dmlBuilder;
    }

    @Override
    public int insert(Object instance) {
        if(null==instance){
            return 0;
        }
        int effect = 0;
        try {
            ConnectionExecutorItem connectionExecutorItem = dmlBuilder.insert(instance);
            effect = sqlBuilder.connectionExecutor.executeUpdate(connectionExecutorItem);
            Entity entity = quickDAOConfig.getEntityByClassName(instance.getClass().getName());
            if (effect>0&&null!=entity.id&&entity.id.strategy.equals(IdStrategy.AutoIncrement)) {
                setAutoIncrementPrimaryKeyValue(instance,entity,connectionExecutorItem.preparedStatement);
            }
            connectionExecutorItem.preparedStatement.close();
        } catch (Exception e) {
            throw new SQLRuntimeException(e);
        }
        return effect;
    }

    @Override
    public int insert(Object[] instances) {
        if(null==instances||instances.length==0){
            return 0;
        }
        int effect = 0;
        try {
            Entity entity = quickDAOConfig.getEntityByClassName(instances[0].getClass().getName());
            ConnectionExecutorItem[] connectionExecutorItems = dmlBuilder.insert(instances);
            for(int i=0;i<connectionExecutorItems.length;i++){
                effect += dmlBuilder.connectionExecutor.executeUpdate(connectionExecutorItems[i]);
                if(effect>0&&null!=entity.id&&entity.id.strategy.equals(IdStrategy.AutoIncrement)){
                    setAutoIncrementPrimaryKeyValue(instances[i],entity,connectionExecutorItems[i].preparedStatement);
                }
                connectionExecutorItems[i].preparedStatement.close();
            }
            dmlBuilder.connectionExecutor.connection.commit();
        } catch (Exception e) {
            throw new SQLRuntimeException(e);
        }
        return effect;
    }

    @Override
    public int insert(Collection instanceCollection) {
        return insert(instanceCollection.toArray(new Object[0]));
    }

    @Override
    public int insertIgnore(Object instance) {
        if(null==instance){
            return 0;
        }
        if(!exist(instance)){
            return insert(instance);
        }
        return 0;
    }

    @Override
    public int insertIgnore(Object[] instances) {
        if(null==instances||instances.length==0){
            return 0;
        }
        List insertList = new ArrayList();
        for(Object instance:instances){
            if(!exist(instance)){
                insertList.add(instance);
            }
        }
        return insert(insertList);
    }

    @Override
    public int insertIgnore(Collection instanceCollection) {
        return insertIgnore(instanceCollection.toArray(new Object[0]));
    }

    @Override
    public int insertBatch(Object[] instances) {
        if(null==instances||instances.length==0){
            return 0;
        }
        int effect = 0;
        try {
            dmlBuilder.connectionExecutor.connection.setAutoCommit(false);
            for(int i=0;i<instances.length;i+=quickDAOConfig.perBatchCommit){
                ConnectionExecutorItem connectionExecutorItem = dmlBuilder.insertBatch(instances,i,Math.min(i+quickDAOConfig.perBatchCommit,instances.length));
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
                dmlBuilder.connectionExecutor.connection.commit();
                connectionExecutorItem.preparedStatement.clearBatch();
                connectionExecutorItem.preparedStatement.close();
            }
            return effect;
        }catch (Exception e){
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public int insertBatch(Collection instanceCollection) {
        return insertBatch(instanceCollection.toArray(new Object[0]));
    }

    @Override
    public int update(Object instance) {
        if(null==instance){
            return 0;
        }
        int effect = 0;
        Entity entity = quickDAOConfig.getEntityByClassName(instance.getClass().getName());
        try {
            ConnectionExecutorItem connectionExecutorItem = null;
            if(!entity.uniqueProperties.isEmpty()){
                connectionExecutorItem = dmlBuilder.updateByUniqueKey(instance);
            }else if(null!=entity.id){
                connectionExecutorItem = dmlBuilder.updateById(instance);
            }else{
                logger.warn("[忽略更新操作]该实例无唯一性约束又无id,忽略该实例的更新操作!");
            }
            if(null!=connectionExecutorItem){
                effect = sqlBuilder.connectionExecutor.executeUpdate(connectionExecutorItem);
                connectionExecutorItem.preparedStatement.close();
            }
        } catch (Exception e) {
            throw new SQLRuntimeException(e);
        }
        return effect;
    }

    @Override
    public int update(Object[] instances) {
        if(null==instances||instances.length==0){
            return 0;
        }
        int effect = 0;
        try {
            Entity entity = quickDAOConfig.getEntityByClassName(instances[0].getClass().getName());
            ConnectionExecutorItem connectionExecutorItem = null;
            if(!entity.uniqueProperties.isEmpty()){
                //根据唯一性约束更新
                connectionExecutorItem = dmlBuilder.updateByUniqueKey(instances);
            }else if(null!=entity.id){
                //根据id更新
                connectionExecutorItem = dmlBuilder.updateById(instances);
            }else{
                logger.warn("[忽略更新操作]该实例无唯一性约束又无id,忽略该实例的更新操作!");
            }
            if(null!=connectionExecutorItem){
                int[] batches = connectionExecutorItem.preparedStatement.executeBatch();
                for (int batch : batches) {
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
                connectionExecutorItem.preparedStatement.close();
            }
            dmlBuilder.connectionExecutor.connection.commit();
        } catch (Exception e) {
            throw new SQLRuntimeException(e);
        }
        return effect;
    }

    @Override
    public int update(Collection instanceCollection) {
        return update(instanceCollection.toArray(new Object[0]));
    }

    @Override
    public int save(Object instance) {
        if(null==instance){
            return 0;
        }
        if(exist(instance)){
            return update(instance);
        }else{
            return insert(instance);
        }
    }

    @Override
    public int save(Object[] instances) {
        if(null==instances||instances.length==0){
            return 0;
        }
        List insertList = new ArrayList();
        List updateList = new ArrayList();
        int effect = 0;
        for(Object instance:instances){
            if(exist(instance)){
                updateList.add(instance);
            }else{
                insertList.add(instance);
            }
        }
        effect += update(updateList);
        effect += insert(insertList);
        return effect;
    }

    @Override
    public int save(Collection instanceCollection) {
        return save(instanceCollection.toArray(new Object[0]));
    }

    @Override
    public int delete(Class clazz, long id) {
        Entity entity = quickDAOConfig.getEntityByClassName(clazz.getName());
        return delete(clazz,entity.id.column,id);
    }

    @Override
    public int delete(Class clazz, String id) {
        Entity entity = quickDAOConfig.getEntityByClassName(clazz.getName());
        return delete(clazz,entity.id.column,id);
    }

    @Override
    public int delete(Class clazz, String field, Object value) {
        int effect = 0;
        try {
            ConnectionExecutorItem connectionExecutorItem = dmlBuilder.deleteByProperty(clazz,field,value);
            effect = sqlBuilder.connectionExecutor.executeUpdate(connectionExecutorItem);
            connectionExecutorItem.preparedStatement.close();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
        return effect;
    }

    @Override
    public <T> int delete(Class<T> clazz, SFunction<T, ?> field, Object value) {
        try {
            String convertField = LambdaUtils.resolveLambdaProperty(field);
            return delete(clazz,convertField,value);
        } catch (Exception e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public int delete(String tableName, String field, Object value) {
        int effect = 0;
        try {
            ConnectionExecutorItem connectionExecutorItem = dmlBuilder.deleteByProperty(tableName,field,value);
            effect = sqlBuilder.connectionExecutor.executeUpdate(connectionExecutorItem);
            connectionExecutorItem.preparedStatement.close();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
        return effect;
    }

    @Override
    public int delete(Object instance) {
        if(null==instance){
            return 0;
        }
        int effect = 0;
        Entity entity = quickDAOConfig.getEntityByClassName(instance.getClass().getName());
        try {
            ConnectionExecutorItem connectionExecutorItem = null;
            if(!entity.uniqueProperties.isEmpty()){
                connectionExecutorItem = dmlBuilder.deleteByUniqueKey(instance);
            }else if(null!=entity.id){
                connectionExecutorItem = dmlBuilder.deleteById(instance);
            }else{
                logger.warn("[忽略删除操作]该实例无唯一性约束又无id,忽略该实例的删除操作!");
            }
            if(null!=connectionExecutorItem){
                effect = sqlBuilder.connectionExecutor.executeUpdate(connectionExecutorItem);
                connectionExecutorItem.preparedStatement.close();
            }
        } catch (Exception e) {
            throw new SQLRuntimeException(e);
        }
        return effect;
    }

    @Override
    public int delete(Object[] instances) {
        if(null==instances||instances.length==0){
            return 0;
        }
        int effect = 0;
        Entity entity = quickDAOConfig.getEntityByClassName(instances[0].getClass().getName());
        try {
            ConnectionExecutorItem connectionExecutorItem = null;
            if(!entity.uniqueProperties.isEmpty()){
                connectionExecutorItem = dmlBuilder.deleteByUniqueKey(instances);
                if(null!=connectionExecutorItem){
                    int[] batches = connectionExecutorItem.preparedStatement.executeBatch();
                    for (int batch : batches) {
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
                }
                dmlBuilder.connectionExecutor.connection.commit();
            }else if(null!=entity.id){
                connectionExecutorItem = dmlBuilder.deleteById(instances);
                effect = sqlBuilder.connectionExecutor.executeUpdate(connectionExecutorItem);
            }else{
                logger.warn("[忽略删除操作]该实例无唯一性约束又无id,忽略该实例的删除操作!");
            }
        } catch (Exception e) {
            throw new SQLRuntimeException(e);
        }
        return effect;
    }

    @Override
    public int delete(Collection instanceCollection) {
        return delete(instanceCollection.toArray(new Object[0]));
    }

    @Override
    public int clear(Class clazz) {
        Entity entity = quickDAOConfig.getEntityByClassName(clazz.getName());
        return clear(entity.escapeTableName);
    }

    @Override
    public int clear(String tableName) {
        int effect = 0;
        try {
            ConnectionExecutorItem connectionExecutorItem = dmlBuilder.clear(tableName);
            effect = sqlBuilder.connectionExecutor.executeUpdate(connectionExecutorItem);
            connectionExecutorItem.preparedStatement.close();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
        return effect;
    }

    @Override
    public int rawUpdate(String updateSQL, Object... parameters) {
        int effect = 0;
        try {
            ConnectionExecutorItem connectionExecutorItem = dmlBuilder.execute(updateSQL, parameters);
            effect = dmlBuilder.connectionExecutor.executeUpdate(connectionExecutorItem);
        }catch (SQLException e){
            throw new SQLRuntimeException(e);
        }
        return effect;
    }

    /**设置主键自增id值*/
    private void  setAutoIncrementPrimaryKeyValue(Object instance, Entity entity, PreparedStatement preparedStatement) throws Exception{
        Field idField = instance.getClass().getDeclaredField(entity.id.name);
        idField.setAccessible(true);
        ResultSet rs = null;
        switch (quickDAOConfig.database){
            case Oracle:{
                String getIdValueSQL = "select " + entity.tableName + "_seq.currVal from dual";
                rs = sqlBuilder.connectionExecutor.executeQuery("获取自增id",getIdValueSQL);
            }break;
            default:{
                rs = preparedStatement.getGeneratedKeys();
            };break;
        }
        if(rs.next()){
            switch(idField.getType().getName()){
                case "int":{idField.setInt(instance, rs.getInt(1));}break;
                case "java.lang.Integer":{idField.set(instance, rs.getInt(1));}break;
                case "long":{idField.setLong(instance, rs.getLong(1));}break;
                case "java.lang.Long":{idField.set(instance, rs.getLong(1));}break;
                case "java.lang.String":{idField.set(instance, rs.getString(1));}break;
                default:{
                    throw new IllegalArgumentException("当前仅支持int,long,String类型的自增主键!自增字段名称:"+idField.getName()+",类型:"+idField.getType().getName()+"!");
                }
            }
        }
        rs.close();
    }
}
