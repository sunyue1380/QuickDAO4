package cn.schoolwow.quickdao.builder.dml;

import cn.schoolwow.quickdao.annotation.IdStrategy;
import cn.schoolwow.quickdao.builder.AbstractSQLBuilder;
import cn.schoolwow.quickdao.domain.ConnectionExecutorItem;
import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.Property;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

public class  AbstractDMLBuilder extends AbstractSQLBuilder implements DMLBuilder {

    public AbstractDMLBuilder(QuickDAOConfig quickDAOConfig) {
        super(quickDAOConfig);
    }

    @Override
    public ConnectionExecutorItem insert(Object instance) throws Exception {
        String sql = insert(instance.getClass());
        ConnectionExecutorItem connectionExecutorItem = connectionExecutor.newConnectionExecutorItem("插入对象",sql);
        StringBuilder builder = new StringBuilder(sql.replace("?", PLACEHOLDER));
        insert(connectionExecutorItem.preparedStatement,instance, builder);
        connectionExecutorItem.sql = builder.toString();
        return connectionExecutorItem;
    }

    @Override
    public ConnectionExecutorItem[] insert(Object[] instances) throws Exception {
        String sql = insert(instances[0].getClass());
        ConnectionExecutorItem[] connectionExecutorItems = new ConnectionExecutorItem[instances.length];
        connectionExecutor.connection.setAutoCommit(false);
        for(int i=0;i<instances.length;i++){
            ConnectionExecutorItem connectionExecutorItem = connectionExecutor.newConnectionExecutorItem("批量插入对象",sql);
            StringBuilder sqlBuilder = new StringBuilder(sql.replace("?", PLACEHOLDER));
            insert(connectionExecutorItem.preparedStatement,instances[i],sqlBuilder);
            connectionExecutorItem.sql = sqlBuilder.toString();
            connectionExecutorItems[i] = connectionExecutorItem;
        }
        return connectionExecutorItems;
    }

    @Override
    public ConnectionExecutorItem insertBatch(Object[] instances) throws Exception {
        String sql = insert(instances[0].getClass());
        ConnectionExecutorItem connectionExecutorItem = connectionExecutor.newConnectionExecutorItem("批量插入对象",sql);
        connectionExecutor.connection.setAutoCommit(false);
        StringBuilder builder = new StringBuilder();
        for(Object instance : instances){
            StringBuilder sqlBuilder = new StringBuilder(sql.replace("?", PLACEHOLDER));
            insert(connectionExecutorItem.preparedStatement,instance,sqlBuilder);
            builder.append(sqlBuilder.toString()+";");
            connectionExecutorItem.preparedStatement.addBatch();
        }
        connectionExecutorItem.sql = builder.toString();
        return connectionExecutorItem;
    }

    @Override
    public ConnectionExecutorItem updateByUniqueKey(Object instance) throws Exception{
        String sql = updateByUniqueKey(instance.getClass());
        ConnectionExecutorItem connectionExecutorItem = connectionExecutor.newConnectionExecutorItem("根据唯一性约束更新对象",sql);
        StringBuilder builder = new StringBuilder(sql.replace("?", PLACEHOLDER));
        updateByUniqueKey(connectionExecutorItem.preparedStatement,instance, builder);
        connectionExecutorItem.sql = builder.toString();
        return connectionExecutorItem;
    }

    @Override
    public ConnectionExecutorItem updateByUniqueKey(Object[] instances) throws Exception {
        String sql = updateByUniqueKey(instances[0].getClass());
        ConnectionExecutorItem connectionExecutorItem = connectionExecutor.newConnectionExecutorItem("根据唯一性约束批量更新对象",sql);
        connectionExecutor.connection.setAutoCommit(false);
        StringBuilder builder = new StringBuilder();
        for(Object instance : instances){
            StringBuilder sqlBuilder = new StringBuilder(sql.replace("?", PLACEHOLDER));
            updateByUniqueKey(connectionExecutorItem.preparedStatement,instance,sqlBuilder);
            builder.append(sqlBuilder.toString()+";");
            connectionExecutorItem.preparedStatement.addBatch();
        }
        connectionExecutorItem.sql = builder.toString();
        return connectionExecutorItem;
    }

    @Override
    public ConnectionExecutorItem updateById(Object instance) throws Exception {
        String sql = updateById(instance.getClass());
        ConnectionExecutorItem connectionExecutorItem = connectionExecutor.newConnectionExecutorItem("根据ID更新对象",sql);
        StringBuilder builder = new StringBuilder(sql.replace("?", PLACEHOLDER));
        updateById(connectionExecutorItem.preparedStatement,instance, builder);
        connectionExecutorItem.sql = builder.toString();
        return connectionExecutorItem;
    }

    @Override
    public ConnectionExecutorItem updateById(Object[] instances) throws Exception {
        String sql = updateById(instances[0].getClass());
        ConnectionExecutorItem connectionExecutorItem = connectionExecutor.newConnectionExecutorItem("根据ID批量更新对象",sql);
        connectionExecutor.connection.setAutoCommit(false);
        StringBuilder builder = new StringBuilder();
        for(Object instance : instances){
            StringBuilder sqlBuilder = new StringBuilder(sql.replace("?", PLACEHOLDER));
            updateById(connectionExecutorItem.preparedStatement,instance,sqlBuilder);
            builder.append(sqlBuilder.toString()+";");
            connectionExecutorItem.preparedStatement.addBatch();
        }
        connectionExecutorItem.sql = builder.toString();
        return connectionExecutorItem;
    }

    @Override
    public ConnectionExecutorItem deleteByProperty(Class clazz, String property, Object value) throws SQLException {
        String key = "deleteByProperty_" + clazz.getName()+"_"+property+"_"+quickDAOConfig.database.getClass().getSimpleName();
        if (!quickDAOConfig.sqlCache.containsKey(key)) {
            Entity entity = quickDAOConfig.getEntityByClassName(clazz.getName());
            StringBuilder builder = new StringBuilder();
            builder.append("delete from " + entity.escapeTableName + " where " + quickDAOConfig.database.escape(entity.getColumnNameByFieldName(property)) + " = ?");
            quickDAOConfig.sqlCache.put(key, builder.toString());
        }
        String sql = quickDAOConfig.sqlCache.get(key);
        ConnectionExecutorItem connectionExecutorItem = connectionExecutor.newConnectionExecutorItem("根据单个字段删除",sql);
        connectionExecutorItem.preparedStatement.setObject(1, value);
        connectionExecutorItem.sql = sql.replace("?",(value instanceof String)?"'"+value.toString()+"'":value.toString());
        return connectionExecutorItem;
    }

    @Override
    public ConnectionExecutorItem deleteByProperty(String tableName, String property, Object value) throws SQLException {
        String sql = "delete from " + quickDAOConfig.database.escape(tableName) + " where " + quickDAOConfig.database.escape(property) + " = ?";
        ConnectionExecutorItem connectionExecutorItem = connectionExecutor.newConnectionExecutorItem("根据单个字段删除",sql);
        connectionExecutorItem.preparedStatement.setObject(1, value);
        connectionExecutorItem.sql = sql.replace("?",(value instanceof String)?"'"+value.toString()+"'":value.toString());
        return connectionExecutorItem;
    }

    @Override
    public ConnectionExecutorItem clear(Class clazz) throws SQLException {
        String key = "clear_" + clazz.getName()+"_"+quickDAOConfig.database.getClass().getSimpleName();
        if (!quickDAOConfig.sqlCache.containsKey(key)) {
            Entity entity = quickDAOConfig.getEntityByClassName(clazz.getName());
            quickDAOConfig.sqlCache.put(key, "delete from "+entity.escapeTableName);
        }
        ConnectionExecutorItem connectionExecutorItem = connectionExecutor.newConnectionExecutorItem("清空表",quickDAOConfig.sqlCache.get(key));
        return connectionExecutorItem;
    }

    /**
     * 获取插入语句
     * @param clazz 实体类对象
     * */
    private String insert(Class clazz){
        String key = "insert_" + clazz.getName()+"_"+quickDAOConfig.database.getClass().getSimpleName();
        if (!quickDAOConfig.sqlCache.containsKey(key)) {
            StringBuilder builder = new StringBuilder();
            Entity entity = quickDAOConfig.getEntityByClassName(clazz.getName());
            builder.append("insert into " + entity.escapeTableName + "(");
            for (Property property : entity.properties) {
                if (property.id&&property.strategy== IdStrategy.AutoIncrement) {
                    continue;
                }
                builder.append(quickDAOConfig.database.escape(property.column) + ",");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(") values(");
            for (Property property : entity.properties) {
                if (property.id&&property.strategy== IdStrategy.AutoIncrement) {
                    continue;
                }
                builder.append((null==property.function?"?":property.function)+",");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(")");
            quickDAOConfig.sqlCache.put(key, builder.toString());
        }
        return quickDAOConfig.sqlCache.get(key);
    }

    /**
     * 设置插入参数值
     * @param preparedStatement SQL语句
     * @param instance 实例对象
     * @param sqlBuilder sql日志
     * */
    private void insert(PreparedStatement preparedStatement,Object instance, StringBuilder sqlBuilder) throws Exception {
        int parameterIndex = 1;
        Entity entity = quickDAOConfig.getEntityByClassName(instance.getClass().getName());
        for (Property property : entity.properties) {
            if (property.id&&property.strategy== IdStrategy.AutoIncrement) {
                continue;
            }
            if(property.id&&property.strategy== IdStrategy.IdGenerator){
                Field idField = instance.getClass().getDeclaredField(property.name);
                idField.setAccessible(true);
                String value = quickDAOConfig.idGenerator.getNextId();
                switch (idField.getType().getName()){
                    case "int":{idField.setInt(instance, Integer.parseInt(value));}break;
                    case "java.lang.Integer":{idField.set(instance, Integer.parseInt(value));}break;
                    case "long":{idField.setLong(instance, Long.parseLong(value));}break;
                    case "java.lang.Long":{idField.set(instance, Long.parseLong(value));}break;
                    case "java.lang.String":{idField.set(instance, value);}break;
                    default:{
                        throw new IllegalArgumentException("当前仅支持int,long,String类型的自增主键!自增字段名称:"+idField.getName()+",类型:"+idField.getType().getName()+"!");
                    }
                }
            }
            if(property.createdAt||property.updateAt){
                setCurrentDateTime(property,instance);
            }
            setParameter(instance, property, preparedStatement, parameterIndex,sqlBuilder);
            parameterIndex++;
        }
    }

    /**
     * 根据唯一性约束更新语句
     * @param clazz 实例类对象
     * */
    private String updateByUniqueKey(Class clazz){
        String key = "updateByUniqueKey_" + clazz.getName()+"_"+quickDAOConfig.database.getClass().getSimpleName();
        if (!quickDAOConfig.sqlCache.containsKey(key)) {
            StringBuilder builder = new StringBuilder();
            Entity entity = quickDAOConfig.getEntityByClassName(clazz.getName());
            builder.append("update " + entity.escapeTableName + " set ");
            for (Property property : entity.properties) {
                if (property.id || entity.uniqueProperties.contains(property)) {
                    continue;
                }
                if(property.createdAt){
                    continue;
                }
                builder.append(quickDAOConfig.database.escape(property.column) + " = "+(null==property.function?"?":property.function)+",");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(" where ");
            for (Property property : entity.properties) {
                if (entity.uniqueProperties.contains(property)&&!property.id) {
                    builder.append(quickDAOConfig.database.escape(property.column) + " = ? and ");
                }
            }
            builder.delete(builder.length() - 5, builder.length());
            quickDAOConfig.sqlCache.put(key, builder.toString());
        }
        return quickDAOConfig.sqlCache.get(key);
    }

    /**
     * 设置根据唯一性约束插入参数值
     * @param preparedStatement SQL语句
     * @param instance 实例对象
     * @param sqlBuilder sql日志
     * */
    private void updateByUniqueKey(PreparedStatement preparedStatement,Object instance, StringBuilder sqlBuilder) throws Exception {
        int parameterIndex = 1;
        Entity entity = quickDAOConfig.getEntityByClassName(instance.getClass().getName());
        for (Property property : entity.properties) {
            if (property.id || entity.uniqueProperties.contains(property)) {
                continue;
            }
            if(property.createdAt){
                continue;
            }
            if(property.updateAt){
                setCurrentDateTime(property,instance);
            }
            setParameter(instance, property, preparedStatement, parameterIndex, sqlBuilder);
            parameterIndex++;
        }
        for (Property property : entity.properties) {
            if (entity.uniqueProperties.contains(property)&&!property.id) {
                setParameter(instance, property, preparedStatement, parameterIndex, sqlBuilder);
                parameterIndex++;
            }
        }
    }

    /**
     * 根据id更新语句
     * @param clazz 实例类对象
     * */
    private String updateById(Class clazz){
        String key = "updateById_" + clazz.getName()+"_"+quickDAOConfig.database.getClass().getSimpleName();
        if (!quickDAOConfig.sqlCache.containsKey(key)) {
            StringBuilder builder = new StringBuilder();
            Entity entity = quickDAOConfig.getEntityByClassName(clazz.getName());
            builder.append("update " + entity.escapeTableName + " set ");
            for (Property property : entity.properties) {
                if (property.id) {
                    continue;
                }
                if(property.createdAt){
                    continue;
                }
                builder.append(quickDAOConfig.database.escape(property.column) + " = ?,");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(" where " + quickDAOConfig.database.escape(entity.id.column) + " = ?");
            quickDAOConfig.sqlCache.put(key, builder.toString());
        }
        return quickDAOConfig.sqlCache.get(key);
    }

    /**
     * 设置根据id更新参数值
     * @param preparedStatement SQL语句
     * @param instance 实例对象
     * @param sqlBuilder sql日志
     * */
    private void updateById(PreparedStatement preparedStatement,Object instance, StringBuilder sqlBuilder) throws Exception {
        int parameterIndex = 1;
        Entity entity = quickDAOConfig.getEntityByClassName(instance.getClass().getName());
        for (Property property : entity.properties) {
            if (property.id) {
                continue;
            }
            if(property.createdAt){
                continue;
            }
            if(property.updateAt){
                setCurrentDateTime(property,instance);
            }
            setParameter(instance, property, preparedStatement, parameterIndex,sqlBuilder);
            parameterIndex++;
        }
        //再设置id属性
        setParameter(instance, entity.id , preparedStatement, parameterIndex,sqlBuilder);
    }

    /**
     * 设置字段值为当前日期
     * @param property 字段属性
     * @param instance 实例
     * */
    private void setCurrentDateTime(Property property, Object instance) throws Exception {
        Field field = getFieldFromInstance(instance,property);
        switch(property.className){
            case "java.util.Date":{
                field.set(instance,new Date(System.currentTimeMillis()));
            }break;
            case "java.sql.Date":{
                field.set(instance,new java.sql.Date(System.currentTimeMillis()));
            }break;
            case "java.sql.Timestamp":{
                field.set(instance,new Timestamp(System.currentTimeMillis()));
            }break;
            case "java.util.Calendar":{
                field.set(instance, Calendar.getInstance());
            }break;
            case "java.time.LocalDate":{field.set(instance, LocalDate.now());}break;
            case "java.time.LocalDateTime":{field.set(instance, LocalDateTime.now());}break;
            default:{
                throw new IllegalArgumentException("不支持该日期类型,目前支持的类型为Date,Calendar,LocalDate,LocalDateTime!当前类型:"+property.className);
            }
        }
    }
}
