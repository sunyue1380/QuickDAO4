package cn.schoolwow.quickdao.builder.ddl;

import cn.schoolwow.quickdao.annotation.IdStrategy;
import cn.schoolwow.quickdao.annotation.IndexType;
import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.IndexField;
import cn.schoolwow.quickdao.domain.Property;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class MySQLDDLBuilder extends AbstractDDLBuilder {
    public MySQLDDLBuilder(QuickDAOConfig quickDAOConfig) {
        super(quickDAOConfig);
    }

    @Override
    public void getDatabaseName() throws SQLException{
        String getDatabaseNameSQL = "select database();";
        ResultSet resultSet = connectionExecutor.executeQuery("获取数据库名称",getDatabaseNameSQL);
        if(resultSet.next()){
            quickDAOConfig.databaseName = resultSet.getString(1);
        }
        resultSet.close();
    }

    @Override
    protected String getAutoIncrementSQL(Property property) {
        return property.column + " " + property.columnType + " primary key auto_increment";
    }

    @Override
    public boolean hasTableExists(Entity entity) throws SQLException {
        String hasTableExistsSQL = "show tables like '%"+entity.tableName+"%';";
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
        for(IndexField indexField:entity.indexFieldList){
            if(null==indexField.columns||indexField.columns.isEmpty()){
                logger.warn("[忽略索引]该索引字段信息为空!表:{},索引名称:{}",entity.tableName,indexField.indexName);
                continue;
            }
            switch (indexField.indexType){
                case NORMAL:{}break;
                case UNIQUE:{builder.append("unique");}break;
                case FULLTEXT:{builder.append("fulltext");}break;
            }
            builder.append(" index " + quickDAOConfig.database.escape(indexField.indexName) + " (");
            for(String column:indexField.columns){
                builder.append(quickDAOConfig.database.escape(column)+",");
            }
            builder.deleteCharAt(builder.length()-1);
            builder.append(")");
            if(null!=indexField.using&&!indexField.using.isEmpty()){
                builder.append(" using "+indexField.using);
            }
            if(null!=indexField.comment&&!indexField.comment.isEmpty()){
                builder.append(" "+quickDAOConfig.database.comment(indexField.comment));
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
        if (null != entity.comment) {
            builder.append(" "+quickDAOConfig.database.comment(entity.comment));
        }
        //添加表引擎
        String engine = entity.engine;
        if(null==engine||engine.isEmpty()){
            engine = quickDAOConfig.engine;
        }
        if(null!=engine&&!engine.isEmpty()) {
            builder.append(" ENGINE="+engine);
        }
        //指定表编码
        String charset = entity.charset;
        if(null==charset||charset.isEmpty()){
            charset = quickDAOConfig.charset;
        }
        if(null!=charset&&!charset.isEmpty()){
            builder.append(" DEFAULT CHARSET="+charset);
        }
        connectionExecutor.executeUpdate("生成新表",builder.toString());
    }

    @Override
    public boolean hasIndexExists(String tableName, String indexName) throws SQLException {
        String hasIndexExistsSQL = "show index from "+quickDAOConfig.database.escape(tableName)+" where key_name = '"+indexName+"'";
        ResultSet resultSet = connectionExecutor.executeQuery("查看索引是否存在",hasIndexExistsSQL);
        boolean result = false;
        if (resultSet.next()) {
            result = true;
        }
        resultSet.close();
        return result;
    }

    @Override
    public void dropIndex(String tableName, String indexName) throws SQLException{
        String dropIndexSQL = "drop index "+quickDAOConfig.database.escape(indexName)+" on "+quickDAOConfig.database.escape(tableName);
        connectionExecutor.executeUpdate("删除索引",dropIndexSQL);
    }

    @Override
    public void enableForeignConstraintCheck(boolean enable) throws SQLException {
        String foreignConstraintCheckSQL = "set foreign_key_checks = " + (enable?1:0);
        connectionExecutor.executeUpdate(enable?"启用外键约束检查":"禁用外键约束检查",foreignConstraintCheckSQL);
    }

    @Override
    public Map<String, String> getTypeFieldMapping() {
        Map<String,String> fieldTypeMapping = new HashMap<>();
        fieldTypeMapping.put("byte","TINYINT");
        fieldTypeMapping.put("java.lang.Byte","TINYINT");
        fieldTypeMapping.put("[B","LONGBLOB");
        fieldTypeMapping.put("boolean","TINYINT");
        fieldTypeMapping.put("java.lang.Boolean","TINYINT");
        fieldTypeMapping.put("char","TINYINT");
        fieldTypeMapping.put("java.lang.Character","TINYINT");
        fieldTypeMapping.put("short","SMALLINT");
        fieldTypeMapping.put("java.lang.Short","SMALLINT");
        fieldTypeMapping.put("int","INT");
        fieldTypeMapping.put("java.lang.Integer","INTEGER(11)");
        fieldTypeMapping.put("float","FLOAT(4,2)");
        fieldTypeMapping.put("java.lang.Float","FLOAT(4,2)");
        fieldTypeMapping.put("long","BIGINT");
        fieldTypeMapping.put("java.lang.Long","BIGINT");
        fieldTypeMapping.put("double","DOUBLE(5,2)");
        fieldTypeMapping.put("java.lang.Double","DOUBLE(5,2)");
        fieldTypeMapping.put("java.lang.String","VARCHAR(255)");
        fieldTypeMapping.put("java.util.Date","DATETIME");
        fieldTypeMapping.put("java.sql.Date","DATE");
        fieldTypeMapping.put("java.sql.Time","TIME");
        fieldTypeMapping.put("java.sql.Timestamp","TIMESTAMP");
        fieldTypeMapping.put("java.time.LocalDate","DATE");
        fieldTypeMapping.put("java.time.LocalDateTime","DATETIME");
        fieldTypeMapping.put("java.sql.Array","");
        fieldTypeMapping.put("java.math.BigDecimal","DECIMAL");
        fieldTypeMapping.put("java.sql.Blob","BLOB");
        fieldTypeMapping.put("java.sql.Clob","TEXT");
        fieldTypeMapping.put("java.sql.NClob","TEXT");
        fieldTypeMapping.put("java.sql.Ref","");
        fieldTypeMapping.put("java.net.URL","");
        fieldTypeMapping.put("java.sql.RowId","");
        fieldTypeMapping.put("java.sql.SQLXML","");
        fieldTypeMapping.put("java.io.InputStream","LONGTEXT");
        fieldTypeMapping.put("java.io.Reader","LONGTEXT");
        return fieldTypeMapping;
    }

    @Override
    protected List<Entity> getVirtualEntity(){
        Entity entity = new Entity();
        entity.tableName = "dual";
        entity.escapeTableName = "dual";
        entity.properties = new ArrayList<>();
        return Arrays.asList(entity);
    }

    @Override
    protected void getIndex(List<Entity> entityList) throws SQLException {
        String getIndexSQL = "select table_name, index_name, non_unique, column_name, index_type, index_comment from information_schema.`statistics` where table_schema = '" + quickDAOConfig.databaseName + "'";
        ResultSet resultSet = connectionExecutor.executeQuery("获取索引信息",getIndexSQL);
        while (resultSet.next()) {
            for(Entity entity : entityList) {
                if (!entity.tableName.equalsIgnoreCase(resultSet.getString("table_name"))) {
                    continue;
                }
                String indexName = resultSet.getString("index_name");
                IndexField indexField = null;
                for(IndexField indexField1:entity.indexFieldList){
                    if(indexField1.indexName.equals(indexName)){
                        indexField = indexField1;
                        break;
                    }
                }
                if(null==indexField) {
                    indexField = new IndexField();
                    indexField.indexType = resultSet.getInt("non_unique")==0?IndexType.UNIQUE:IndexType.NORMAL;
                    indexField.indexName = resultSet.getString("index_name");
                    indexField.columns.add(resultSet.getString("column_name"));
                    indexField.using = resultSet.getString("index_type");
                    indexField.comment = resultSet.getString("index_comment");
                    entity.indexFieldList.add(indexField);
                }else{
                    indexField.columns.add(resultSet.getString("column_name"));
                }
                break;
            }

        }
        resultSet.close();
    }

    @Override
    protected void getEntityPropertyList(List<Entity> entityList) throws SQLException {
        String getEntityPropertyListSQL = "select table_name, column_name, data_type, character_maximum_length, is_nullable, column_key, extra, column_default, column_comment from information_schema.`columns` where table_schema = '" + quickDAOConfig.databaseName + "'";
        ResultSet resultSet = connectionExecutor.executeQuery("获取表字段信息",getEntityPropertyListSQL);
        while (resultSet.next()) {
            for(Entity entity : entityList){
                if(!entity.tableName.equalsIgnoreCase(resultSet.getString("table_name"))){
                    continue;
                }
                //添加字段信息
                Property property = new Property();
                property.column = resultSet.getString("column_name");
                //无符号填充0 => float unsigned zerofill
                property.columnType = resultSet.getString("data_type");
                if(property.columnType.contains(" ")){
                    property.columnType = property.columnType.substring(0,property.columnType.indexOf(" "));
                }
                if(null!=resultSet.getString("character_maximum_length")){
                    property.columnType += "("+resultSet.getString("character_maximum_length")+")";
                }
                property.notNull = "NO".equals(resultSet.getString("is_nullable"));
                String key = resultSet.getString("column_key");
                if("PRI".equals(key)){
                    property.id = true;
                }
                if("auto_increment".equals(resultSet.getString("extra"))){
                    property.id = true;
                    property.strategy = IdStrategy.AutoIncrement;
                }else{
                    property.strategy = IdStrategy.None;
                }
                if (null != resultSet.getString("column_default")) {
                    property.defaultValue = resultSet.getString("column_default");
                }
                property.comment = resultSet.getString("column_comment");
                entity.properties.add(property);
                break;
            }
        }
        resultSet.close();
    }

    @Override
    protected List<Entity> getEntityList() throws SQLException {
        String getEntityListSQL = "show table status;";
        ResultSet resultSet = connectionExecutor.executeQuery("获取表列表",getEntityListSQL);

        List<Entity> entityList = new ArrayList<>();
        while (resultSet.next()) {
            Entity entity = new Entity();
            entity.tableName = resultSet.getString("name");
            entity.comment = resultSet.getString("comment");
            entity.engine = resultSet.getString("engine");
            entity.charset = resultSet.getString("collation");
            entityList.add(entity);
        }
        resultSet.close();
        return entityList;
    }
}
