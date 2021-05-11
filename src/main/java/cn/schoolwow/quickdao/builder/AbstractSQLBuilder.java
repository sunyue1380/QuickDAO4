package cn.schoolwow.quickdao.builder;

import cn.schoolwow.quickdao.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AbstractSQLBuilder implements SQLBuilder{
    protected final static Logger logger = LoggerFactory.getLogger(AbstractSQLBuilder.class);
    /**SQL参数占位符*/
    protected final static String PLACEHOLDER = "** NOT SPECIFIED **";
    /**格式化旧版本的java.sql.Date类型*/
    private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    /**格式化旧版本的java.sql.Time类型*/
    private final static SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("HH:mm:ss");
    /**格式化旧版本的Timestampt类型*/
    private final static SimpleDateFormat simpleDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
    /**格式化日期参数*/
    private final static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    /**格式化日期参数*/
    private final static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    /**数据库信息对象*/
    public QuickDAOConfig quickDAOConfig;
    /**SQL语句执行器*/
    public volatile ConnectionExecutor connectionExecutor;

    public AbstractSQLBuilder(QuickDAOConfig quickDAOConfig) {
        this.quickDAOConfig = quickDAOConfig;
    }

    @Override
    public ConnectionExecutorItem selectCountById(Object instance) throws Exception {
        String key = "selectCountById_" + instance.getClass().getName()+"_"+quickDAOConfig.database.getClass().getName();
        Entity entity = quickDAOConfig.getEntityByClassName(instance.getClass().getName());
        if (!quickDAOConfig.sqlCache.containsKey(key)) {
            StringBuilder builder = new StringBuilder();
            builder.append("select count(1) from " + entity.escapeTableName + " where ");
            builder.append(entity.id.column+" = "+(null==entity.id.function?"?":entity.id.function)+" ");
            quickDAOConfig.sqlCache.put(key, builder.toString());
        }

        String sql = quickDAOConfig.sqlCache.get(key);
        ConnectionExecutorItem connectionExecutorItem = connectionExecutor.newConnectionExecutorItem("根据id查询",sql);
        Field field = instance.getClass().getDeclaredField(entity.id.name);
        field.setAccessible(true);
        Object value = field.get(instance);
        connectionExecutorItem.preparedStatement.setObject(1,value);
        connectionExecutorItem.sql = sql.replace("?",value==null?"":value.toString());
        return connectionExecutorItem;
    }

    @Override
    public ConnectionExecutorItem selectCountByUniqueKey(Object instance) throws Exception {
        String key = "selectCountByUniqueKey_" + instance.getClass().getName()+"_"+quickDAOConfig.database.getClass().getName();
        Entity entity = quickDAOConfig.getEntityByClassName(instance.getClass().getName());
        if (!quickDAOConfig.sqlCache.containsKey(key)) {
            StringBuilder builder = new StringBuilder();
            builder.append("select count(1) from " + entity.escapeTableName + " where ");
            for(Property property:entity.uniqueProperties){
                builder.append(quickDAOConfig.database.escape(property.column)+ "= "+(null==property.function?"?":property.function)+" and ");
            }
            builder.delete(builder.length()-5,builder.length());
            quickDAOConfig.sqlCache.put(key, builder.toString());
        }

        String sql = quickDAOConfig.sqlCache.get(key);
        ConnectionExecutorItem connectionExecutorItem = connectionExecutor.newConnectionExecutorItem("根据唯一性约束查询",sql);
        StringBuilder builder = new StringBuilder(sql.replace("?", PLACEHOLDER));
        int parameterIndex = 1;
        for(Property property:entity.uniqueProperties){
            setParameter(instance,property,connectionExecutorItem.preparedStatement,parameterIndex, builder);
            parameterIndex++;
        }
        connectionExecutorItem.sql = builder.toString();
        return connectionExecutorItem;
    }

    /**
     * DQL查询操作设置参数
     * @param parameter 参数
     * @param ps SQL语句对象
     * @param parameterIndex 参数索引
     * @param sqlBuilder 记录SQL日志
     */
    protected void setParameter(Object parameter, PreparedStatement ps, int parameterIndex, StringBuilder sqlBuilder) throws SQLException {
        String parameterSQL = setPrepareStatementParameter(parameter,null,ps,parameterIndex);
        replaceFirst(sqlBuilder,parameterSQL);
    }

    /**
     * DML操作设置参数
     * @param instance 实例
     * @param property 字段属性信息
     * @param ps PrepareStatement对象
     * @param parameterIndex 参数索引
     * @param sqlBuilder 记录SQL日志
     */
    protected void setParameter(Object instance, Property property, PreparedStatement ps, int parameterIndex, StringBuilder sqlBuilder) throws Exception {
        Field field = getFieldFromInstance(instance,property);
        String parameterSQL = setPrepareStatementParameter(field.get(instance),property,ps,parameterIndex);
        replaceFirst(sqlBuilder,parameterSQL);
    }

    /**
     * 从实例从获取参数
     * @param instance 实例
     * @param property 字段信息
     * */
    protected Field getFieldFromInstance(Object instance, Property property) throws IllegalAccessException {
        Class tempClass = instance.getClass();
        Field field = null;
        while(null==field&&null!=tempClass){
            Field[] fields = tempClass.getDeclaredFields();
            for(Field field1:fields){
                if(field1.getName().equals(property.name)){
                    field = field1;
                    break;
                }
            }
            tempClass = tempClass.getSuperclass();
        }
        if(null==field){
            throw new IllegalArgumentException("字段不存在!字段名:"+property.name+",类名:"+instance.getClass().getName());
        }
        field.setAccessible(true);
        return field;
    }

    /**
     * 设置参数
     * @param parameter 参数
     * @param property 字段信息
     * @param ps PrepareStatement对象
     * @param parameterIndex 参数索引
     * @return SQL字段信息
     * */
    protected String setPrepareStatementParameter(Object parameter, Property property, PreparedStatement ps, int parameterIndex) throws SQLException{
        if(null==parameter){
            ps.setObject(parameterIndex,null);
            return "null";
        }
        String parameterSQL = parameter.toString();
        switch(parameter.getClass().getName()){
            case "byte":{
                ps.setByte(parameterIndex, (byte) parameter);
            }break;
            case "[B":{
                ps.setBytes(parameterIndex, (byte[]) parameter);
            }break;
            case "boolean":{
                boolean value = (boolean) parameter;
                ps.setBoolean(parameterIndex, value);
                parameterSQL = value?"1":"0";
            }break;
            case "short": {
                ps.setShort(parameterIndex, (short) parameter);
            }break;
            case "int": {
                ps.setInt(parameterIndex, (int) parameter);
            }break;
            case "float": {
                ps.setFloat(parameterIndex, (float) parameter);
            }break;
            case "long": {
                ps.setLong(parameterIndex, (long) parameter);
            }break;
            case "double": {
                ps.setDouble(parameterIndex, (double) parameter);
            }break;
            case "java.lang.String": {
                ps.setString(parameterIndex, (String) parameter);
                parameterSQL = "'"+parameter.toString()+"'";
            }break;
            case "java.util.Date": {
                java.util.Date date = (java.util.Date) parameter;
                ps.setDate(parameterIndex,new Date(date.getTime()));
                parameterSQL = "'"+simpleDateFormat.format(date)+"'";
            }break;
            case "java.sql.Date": {
                Date date = (Date) parameter;
                ps.setDate(parameterIndex, (Date) parameter);
                parameterSQL = "'"+simpleDateFormat.format(date)+"'";
            }break;
            case "java.sql.Time": {
                Time time = (Time) parameter;
                ps.setTime(parameterIndex, time);
                parameterSQL = "'"+simpleTimeFormat.format(time)+"'";
            }break;
            case "java.sql.Timestamp": {
                Timestamp timestamp = (Timestamp) parameter;
                ps.setTimestamp(parameterIndex, timestamp);
                parameterSQL = "'"+simpleDateTimeFormat.format(timestamp)+"'";
            }break;
            case "java.time.LocalDate": {
                LocalDate localDate = (LocalDate) parameter;
                ps.setObject(parameterIndex,localDate);
                parameterSQL = "'"+localDate.format(dateFormatter)+"'";
            }break;
            case "java.time.LocalDateTime": {
                LocalDateTime localDateTime = (LocalDateTime) parameter;
                ps.setObject(parameterIndex,localDateTime);
                parameterSQL = "'"+localDateTime.format(dateTimeFormatter)+"'";
            }break;
            case "java.sql.Array": {
                ps.setArray(parameterIndex, (Array) parameter);
            }break;
            case "java.math.BigDecimal": {
                ps.setBigDecimal(parameterIndex, (BigDecimal) parameter);
            }break;
            case "java.sql.Blob": {
                ps.setBlob(parameterIndex,(Blob) parameter);
            }break;
            case "java.sql.Clob": {
                ps.setClob(parameterIndex,(Clob) parameter);
            }break;
            case "java.sql.NClob": {
                ps.setNClob(parameterIndex,(NClob) parameter);
            }break;
            case "java.sql.Ref": {
                ps.setRef(parameterIndex,(Ref) parameter);
            }break;
            case "java.net.URL": {
                ps.setURL(parameterIndex,(URL) parameter);
            }break;
            case "java.sql.RowId": {
                ps.setRowId(parameterIndex,(RowId) parameter);
            }break;
            case "java.sql.SQLXML": {
                ps.setSQLXML(parameterIndex,(SQLXML) parameter);
            }break;
            case "java.io.InputStream": {
                ps.setBinaryStream(parameterIndex, (InputStream) parameter);
            }break;
            case "java.io.Reader": {
                ps.setCharacterStream(parameterIndex, (Reader) parameter);
            }break;
            default:{
                ps.setObject(parameterIndex,parameter);
            }
        }
        return parameterSQL;
    }

    /**替换SQL语句的第一个占位符*/
    protected static void replaceFirst(StringBuilder sqlBuilder,String parameter){
        int indexOf = sqlBuilder.indexOf(PLACEHOLDER);
        if (indexOf >= 0) {
            sqlBuilder.replace(indexOf, indexOf + PLACEHOLDER.length(), parameter);
        }
    }
}
