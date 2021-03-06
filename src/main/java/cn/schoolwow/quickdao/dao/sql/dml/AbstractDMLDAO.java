package cn.schoolwow.quickdao.dao.sql.dml;

import cn.schoolwow.quickdao.annotation.IdStrategy;
import cn.schoolwow.quickdao.builder.dml.AbstractDMLBuilder;
import cn.schoolwow.quickdao.dao.sql.AbstractSQLDAO;
import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import cn.schoolwow.quickdao.domain.SFunction;
import cn.schoolwow.quickdao.exception.SQLRuntimeException;
import cn.schoolwow.quickdao.util.LambdaUtils;
import org.slf4j.MDC;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
            PreparedStatement ps = dmlBuilder.insert(instance);
            effect = ps.executeUpdate();
            Entity entity = quickDAOConfig.getEntityByClassName(instance.getClass().getName());
            if (effect>0&&null!=entity.id&&entity.id.strategy.equals(IdStrategy.AutoIncrement)) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    Field idField = instance.getClass().getDeclaredField(entity.id.name);
                    idField.setAccessible(true);
                    switch(idField.getType().getSimpleName().toLowerCase()){
                        case "int":
                        case "integer":{
                            if(idField.getType().isPrimitive()){
                                idField.setInt(instance,rs.getInt(1));
                            }else{
                                idField.set(instance,Integer.valueOf(rs.getInt(1)));
                            }
                        }break;
                        case "long":{
                            if(idField.getType().isPrimitive()){
                                idField.setLong(instance,rs.getLong(1));
                            }else{
                                idField.set(instance,Long.valueOf(rs.getLong(1)));
                            }
                        }break;
                        case "string":{
                            idField.set(instance,rs.getString(1));
                        }break;
                    }
                }
                rs.close();
            }
            ps.close();
        } catch (Exception e) {
            throw new SQLRuntimeException(e);
        }
        MDC.put("count",effect+"");
        return effect;
    }

    @Override
    public int insert(Object[] instances) {
        if(null==instances||instances.length==0){
            return 0;
        }
        int effect = 0;
        try {
            PreparedStatement[] preparedStatements = dmlBuilder.insert(instances);
            Entity entity = quickDAOConfig.getEntityByClassName(instances[0].getClass().getName());
            Field idField = null;
            if(null!=entity.id&&entity.id.strategy.equals(IdStrategy.AutoIncrement)){
                idField = instances[0].getClass().getDeclaredField(entity.id.name);
                idField.setAccessible(true);
            }
            for(int i=0;i<preparedStatements.length;i++){
                effect += preparedStatements[i].executeUpdate();
                if(effect>0&&null!=idField){
                    ResultSet rs = preparedStatements[i].getGeneratedKeys();
                    if(rs.next()){
                        switch(idField.getType().getSimpleName().toLowerCase()){
                            case "int":
                            case "integer":{
                                if(idField.getType().isPrimitive()){
                                    idField.setInt(instances[i],rs.getInt(1));
                                }else{
                                    idField.set(instances[i],Integer.valueOf(rs.getInt(1)));
                                }
                            }break;
                            case "long":{
                                if(idField.getType().isPrimitive()){
                                    idField.setLong(instances[i],rs.getLong(1));
                                }else{
                                    idField.set(instances[i],Long.valueOf(rs.getLong(1)));
                                }
                            }break;
                            case "string":{
                                idField.set(instances[i],rs.getString(1));
                            }break;
                        }
                    }
                    rs.close();
                }
                preparedStatements[i].close();
            }
            dmlBuilder.connection.commit();
        } catch (Exception e) {
            throw new SQLRuntimeException(e);
        }
        MDC.put("count",effect+"");
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
            PreparedStatement ps = dmlBuilder.insertBatch(instances);
            int[] batches = ps.executeBatch();
            for (int batch : batches) {
                effect += batch;
            }
            ps.close();
            dmlBuilder.connection.commit();
        } catch (Exception e) {
            throw new SQLRuntimeException(e);
        }
        MDC.put("count",effect+"");
        return effect;
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
        PreparedStatement ps = null;
        Entity entity = quickDAOConfig.getEntityByClassName(instance.getClass().getName());
        try {
            if(!entity.uniqueProperties.isEmpty()){
                ps = dmlBuilder.updateByUniqueKey(instance);
                effect = ps.executeUpdate();
            }else if(null!=entity.id){
                ps = dmlBuilder.updateById(instance);
                effect = ps.executeUpdate();
            }else{
                logger.warn("[忽略更新操作]该实例无唯一性约束又无id,忽略该实例的更新操作!");
            }
            if(null!=ps){
                ps.close();
            }
        } catch (Exception e) {
            throw new SQLRuntimeException(e);
        }
        MDC.put("count",effect+"");
        return effect;
    }

    @Override
    public int update(Object[] instances) {
        if(null==instances||instances.length==0){
            return 0;
        }
        int effect = 0;
        PreparedStatement ps = null;
        try {
            Entity entity = quickDAOConfig.getEntityByClassName(instances[0].getClass().getName());
            if(!entity.uniqueProperties.isEmpty()){
                //根据唯一性约束更新
                ps = dmlBuilder.updateByUniqueKey(instances);
            }else if(null!=entity.id){
                //根据id更新
                ps = dmlBuilder.updateById(instances);
            }
            int[] batches = ps.executeBatch();
            for (int batch : batches) {
                effect += batch;
            }
            ps.close();
            dmlBuilder.connection.commit();
        } catch (Exception e) {
            throw new SQLRuntimeException(e);
        }
        MDC.put("count",effect+"");
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
        MDC.put("count",effect+"");
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
            PreparedStatement ps = dmlBuilder.deleteByProperty(clazz,field,value);
            effect = ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
        MDC.put("count",effect+"");
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
            PreparedStatement ps = dmlBuilder.deleteByProperty(tableName,field,value);
            effect = ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
        MDC.put("count",effect+"");
        return effect;
    }

    @Override
    public int clear(Class clazz) {
        int effect = 0;
        try {
            PreparedStatement ps = dmlBuilder.clear(clazz);
            effect = ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
        MDC.put("count",effect+"");
        return effect;
    }
}
