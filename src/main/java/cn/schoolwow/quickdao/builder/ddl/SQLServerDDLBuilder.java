package cn.schoolwow.quickdao.builder.ddl;

import cn.schoolwow.quickdao.annotation.IdStrategy;
import cn.schoolwow.quickdao.annotation.IndexType;
import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.IndexField;
import cn.schoolwow.quickdao.domain.Property;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLServerDDLBuilder extends AbstractDDLBuilder {

    public SQLServerDDLBuilder(QuickDAOConfig quickDAOConfig) {
        super(quickDAOConfig);
    }

    @Override
    protected String getAutoIncrementSQL(Property property) {
        return property.column + " " + property.columnType + " identity(1,1) unique ";
    }

    @Override
    public boolean hasTableExists(Entity entity) throws SQLException {
        String hasTableExistsSQL = "select name from sysobjects where xtype='u' and name = '"+entity.tableName+"';";
        ResultSet resultSet = connectionExecutor.executeQuery("判断表是否存在",hasTableExistsSQL);
        boolean result = false;
        if(resultSet.next()){
            result = true;
        }
        resultSet.close();
        return result;
    }

    @Override
    public void createTable(Entity entity) throws SQLException {
        StringBuilder builder = new StringBuilder("create table " + entity.escapeTableName + "(");
        for (Property property : entity.properties) {
            if(property.id&&property.strategy== IdStrategy.AutoIncrement){
                builder.append(getAutoIncrementSQL(property));
            }else{
                builder.append(quickDAOConfig.database.escape(property.column) + " " + property.columnType);
                if (property.notNull) {
                    builder.append(" not null");
                }
                if (null!=property.defaultValue&&!property.defaultValue.isEmpty()) {
                    builder.append(" default " + property.defaultValue);
                }
                if (null != property.comment) {
                    builder.append(" "+quickDAOConfig.database.comment(property.comment));
                }
                if (null!=property.escapeCheck&&!property.escapeCheck.isEmpty()) {
                    builder.append(" check " + property.escapeCheck);
                }
            }
            builder.append(",");
        }
        if (quickDAOConfig.openForeignKey&&null!=entity.foreignKeyProperties&&entity.foreignKeyProperties.size()>0) {
            for (Property property : entity.foreignKeyProperties) {
                builder.append("foreign key(" + quickDAOConfig.database.escape(property.column) + ") references ");
                String operation = property.foreignKey.foreignKeyOption().getOperation();
                builder.append(quickDAOConfig.database.escape(quickDAOConfig.getEntityByClassName(property.foreignKey.table().getName()).tableName) + "(" + quickDAOConfig.database.escape(property.foreignKey.field()) + ") ON DELETE " + operation+ " ON UPDATE " + operation);
                builder.append(",");
            }
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append(")");
        connectionExecutor.executeUpdate("生成新表", builder.toString());
        //添加注释
        if (null != entity.comment) {
            String entityCommentSQL = "EXEC sp_addextendedproperty 'MS_Description',N'"+entity.comment+"','SCHEMA','dbo','table',N'"+entity.tableName+"';";
            connectionExecutor.executeUpdate("创建表注释", entityCommentSQL);
        }
        for(Property property:entity.properties){
            if(null != property.comment){
                String columnCommentSQL = "EXEC sp_addextendedproperty 'MS_Description',N'"+property.comment+"','SCHEMA','dbo','table',N'"+entity.tableName+"','column',N'"+property.column+"';";
                connectionExecutor.executeUpdate("创建表字段注释", columnCommentSQL);
            }
        }
        //创建索引
        for(IndexField indexField:entity.indexFieldList){
            createIndex(indexField);
        }
    }

    @Override
    public boolean hasIndexExists(String tableName, String indexName) throws SQLException {
        String hasIndexExistsSQL = "select count(1) from sys.indexes WHERE object_id=OBJECT_ID('"+tableName+"', N'U') and name = '"+indexName+"'";
        ResultSet resultSet = connectionExecutor.executeQuery("查看索引是否存在",hasIndexExistsSQL);
        boolean result = false;
        if (resultSet.next()) {
            result = resultSet.getInt(1) > 0;
        }
        resultSet.close();
        return result;
    }

    @Override
    public void dropIndex(String tableName, String indexName) throws SQLException{
        String dropIndexSQL = "drop index " + quickDAOConfig.database.escape(tableName) + "." + quickDAOConfig.database.escape(indexName);
        connectionExecutor.executeUpdate("删除索引",dropIndexSQL);
    }

    @Override
    public void enableForeignConstraintCheck(boolean enable) throws SQLException {
    }

    @Override
    public Map<String, String> getTypeFieldMapping() {
        Map<String,String> fieldTypeMapping = new HashMap<>();
        fieldTypeMapping.put("byte","TINYINT");
        fieldTypeMapping.put("java.lang.Byte","TINYINT");
        fieldTypeMapping.put("[B","BINARY");
        fieldTypeMapping.put("boolean","TINYINT");
        fieldTypeMapping.put("java.lang.Boolean","TINYINT");
        fieldTypeMapping.put("char","TINYINT");
        fieldTypeMapping.put("java.lang.Character","TINYINT");
        fieldTypeMapping.put("short","SMALLINT");
        fieldTypeMapping.put("java.lang.Short","SMALLINT");
        fieldTypeMapping.put("int","INT");
        fieldTypeMapping.put("java.lang.Integer","INTEGER(11)");
        fieldTypeMapping.put("float","REAL");
        fieldTypeMapping.put("java.lang.Float","REAL");
        fieldTypeMapping.put("long","BIGINT");
        fieldTypeMapping.put("java.lang.Long","BIGINT");
        fieldTypeMapping.put("double","FLOAT");
        fieldTypeMapping.put("java.lang.Double","FLOAT");
        fieldTypeMapping.put("java.lang.String","VARCHAR(255)");
        fieldTypeMapping.put("java.util.Date","DATETIME");
        fieldTypeMapping.put("java.sql.Date","DATE");
        fieldTypeMapping.put("java.sql.Time","TIME");
        fieldTypeMapping.put("java.sql.Timestamp","TIMESTAMP");
        fieldTypeMapping.put("java.time.LocalDate","DATE");
        fieldTypeMapping.put("java.time.LocalDateTime","DATETIME");
        fieldTypeMapping.put("java.sql.Array","");
        fieldTypeMapping.put("java.math.BigDecimal","DECIMAL");
        fieldTypeMapping.put("java.sql.Blob","BINARY");
        fieldTypeMapping.put("java.sql.Clob","TEXT");
        fieldTypeMapping.put("java.sql.NClob","TEXT");
        fieldTypeMapping.put("java.sql.Ref","");
        fieldTypeMapping.put("java.net.URL","");
        fieldTypeMapping.put("java.sql.RowId","");
        fieldTypeMapping.put("java.sql.SQLXML","");
        fieldTypeMapping.put("java.io.InputStream","TEXT");
        fieldTypeMapping.put("java.io.Reader","TEXT");
        return fieldTypeMapping;
    }

    @Override
    protected void getIndex(Entity entity) throws SQLException {
        String getIndexSQL = "select i.is_unique,i.name,col.name col_name from sys.indexes i left join sys.index_columns ic on ic.object_id = i.object_id and ic.index_id = i.index_id left join (select * from sys.all_columns where object_id = object_id( '"+entity.tableName+"', N'U' )) col on ic.column_id = col.column_id where i.object_id = object_id('"+entity.tableName+"', N'U' ) and i.index_id > 0";
        ResultSet resultSet = connectionExecutor.executeQuery("获取索引信息",getIndexSQL);
        while (resultSet.next()) {
            IndexField indexField = new IndexField();
            if(resultSet.getBoolean("is_unique")){
                indexField.indexType = IndexType.UNIQUE;
            }else{
                indexField.indexType = IndexType.NORMAL;
            }
            indexField.indexName = resultSet.getString("name");
            //判断是否已经存在该索引
            IndexField existIndexField = entity.indexFieldList.stream().filter(indexField1 -> indexField1.indexName.equals(indexField.indexName)).findFirst().orElse(null);
            if(null!=existIndexField){
                existIndexField.columns.add(resultSet.getNString("col_name"));
            }else{
                indexField.columns.add(resultSet.getNString("col_name"));
                entity.indexFieldList.add(indexField);
            }
        }
        resultSet.close();
    }

    @Override
    protected void getEntityPropertyList(Entity entity) throws SQLException {
        {
            //获取表注释
            String getEntityCommentSQL = "select isnull(convert(varchar(255),value),'') comment from sys.extended_properties ex_p where ex_p.minor_id=0 and ex_p.major_id in (select id from sys.sysobjects a where a.name='"+entity.tableName+"')";
            ResultSet resultSet = connectionExecutor.executeQuery("获取表注释",getEntityCommentSQL);
            if(resultSet.next()){
                entity.comment = resultSet.getString("comment");
            }
            resultSet.close();
        }
        {
            //获取字段信息
            String getEntityPropertyTypeListSQL = "select ordinal_position,column_name,data_type,is_nullable from information_schema.columns where table_name = '" + entity.tableName + "'";
            ResultSet resultSet = connectionExecutor.executeQuery("获取表字段类型信息", getEntityPropertyTypeListSQL);
            List<Property> propertyList = new ArrayList<>();
            while (resultSet.next()) {
                Property property = new Property();
                property.column = resultSet.getString("column_name");
                property.columnType = resultSet.getString("data_type");
                if(resultSet.getInt("ordinal_position")==1&&"bigint".equalsIgnoreCase(property.columnType)){
                    property.id = true;
                    property.strategy = IdStrategy.AutoIncrement;
                }
                property.notNull = "NO".equals(resultSet.getString("is_nullable"));
                propertyList.add(property);
            }
            resultSet.close();
            entity.properties = propertyList;
        }
        {
            //获取字段注释
            String getPropertyCommentList = "select c.name ,convert(varchar(255),a.value) value from sys.extended_properties a, sysobjects b, sys.columns c where a.major_id = b.id and c.object_id = b.id and c.column_id = a.minor_id and b.name = '"+entity.tableName+"'";
            ResultSet resultSet = connectionExecutor.executeQuery("获取字段注释", getPropertyCommentList);
            while(resultSet.next()){
                String name = resultSet.getString("name");
                for(Property property:entity.properties){
                    if(property.column.equalsIgnoreCase(name)){
                        property.comment = resultSet.getString("value");
                        break;
                    }
                }
            }
            resultSet.close();
        }
    }

    @Override
    protected List<Entity> getEntityList() throws SQLException {
        String getEntityListSQL = "select name from sysobjects where xtype='u' order by name;";
        ResultSet resultSet = connectionExecutor.executeQuery("获取表列表",getEntityListSQL);

        List<Entity> entityList = new ArrayList<>();
        while (resultSet.next()) {
            Entity entity = new Entity();
            entity.tableName = resultSet.getString("name");
            entityList.add(entity);
        }
        resultSet.close();
        return entityList;
    }
}
