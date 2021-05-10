package cn.schoolwow.quickdao.builder.ddl;

import cn.schoolwow.quickdao.annotation.IdStrategy;
import cn.schoolwow.quickdao.annotation.IndexType;
import cn.schoolwow.quickdao.domain.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class MySQLDDLBuilder extends AbstractDDLBuilder {
    public MySQLDDLBuilder(QuickDAOConfig quickDAOConfig) {
        super(quickDAOConfig);
    }

    @Override
    public String getDatabaseName() throws SQLException{
        ResultSet resultSet = connection.prepareStatement("select database();").executeQuery();
        String databaseName = null;
        if(resultSet.next()){
            databaseName = resultSet.getString(1);
        }
        resultSet.close();
        return databaseName;
    }

    @Override
    protected String getAutoIncrementSQL(Property property) {
        return property.column + " " + property.columnType + " primary key auto_increment";
    }

    @Override
    public boolean hasTableExists(Entity entity) throws SQLException {
        ResultSet resultSet = connection.prepareStatement("show tables like '%"+entity.tableName+"%';").executeQuery();
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
        ThreadLocalMap.put("name","生成新表");
        ThreadLocalMap.put("sql",builder.toString());
        connection.prepareStatement(ThreadLocalMap.get("sql")).executeUpdate();
    }

    @Override
    public boolean hasIndexExists(String tableName, String indexName) throws SQLException {
        String sql = "show index from "+quickDAOConfig.database.escape(tableName)+" where key_name = '"+indexName+"'";

        ThreadLocalMap.put("name","查看索引是否存在");
        ThreadLocalMap.put("sql",sql);
        ResultSet resultSet = connection.prepareStatement(sql).executeQuery();
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
        ThreadLocalMap.put("name","删除索引");
        ThreadLocalMap.put("sql",dropIndexSQL);
        connection.prepareStatement(ThreadLocalMap.get("sql")).executeUpdate();
    }

    @Override
    public void enableForeignConstraintCheck(boolean enable) throws SQLException {
        String foreignConstraintCheckSQL = "set foreign_key_checks = " + (enable?1:0);
        ThreadLocalMap.put("name",enable?"启用外键约束检查":"禁用外键约束检查");
        ThreadLocalMap.put("sql",foreignConstraintCheckSQL);
        connection.prepareStatement(ThreadLocalMap.get("sql")).executeUpdate();
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

    /**获取虚拟表信息*/
    @Override
    protected List<Entity> getVirtualEntity(){
        Entity entity = new Entity();
        entity.tableName = "dual";
        entity.escapeTableName = "dual";
        entity.properties = new ArrayList<>();
        return Arrays.asList(entity);
    }

    /**
     * 提取索引信息
     * */
    @Override
    protected void getIndex(Entity entity) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("show index from " + quickDAOConfig.database.escape(entity.tableName));
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            String indexName = resultSet.getString("Key_name");
            IndexField indexField = null;
            for(IndexField indexField1:entity.indexFieldList){
                if(indexField1.indexName.equals(indexName)){
                    indexField = indexField1;
                    break;
                }
            }
            if(null==indexField) {
                indexField = new IndexField();
                indexField.indexType = resultSet.getInt("Non_unique")==0?IndexType.UNIQUE:IndexType.NORMAL;
                indexField.indexName = resultSet.getString("Key_name");
                indexField.columns.add(resultSet.getString("Column_name"));
                indexField.using = resultSet.getString("Index_type");
                indexField.comment = resultSet.getString("Index_comment");
                entity.indexFieldList.add(indexField);
            }else{
                indexField.columns.add(resultSet.getString("Column_name"));
            }
        }
        resultSet.close();
        preparedStatement.close();
    }

    /**
     * 提取表字段信息
     * */
    @Override
    protected void getEntityPropertyList(Entity entity) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("show full columns from " + quickDAOConfig.database.escape(entity.tableName));
        ResultSet resultSet = preparedStatement.executeQuery();
        List<Property> propertyList = new ArrayList<>();
        while (resultSet.next()) {
            Property property = new Property();
            property.column = resultSet.getString("Field");
            //无符号填充0 => float unsigned zerofill
            property.columnType = resultSet.getString("Type");
            if(property.columnType.contains(" ")){
                property.columnType = property.columnType.substring(0,property.columnType.indexOf(" "));
            }
            property.notNull = "NO".equals(resultSet.getString("Null"));
            String key = resultSet.getString("Key");
            if("PRI".equals(key)){
                property.id = true;
            }
            if("auto_increment".equals(resultSet.getString("Extra"))){
                property.id = true;
                property.strategy = IdStrategy.AutoIncrement;
            }else{
                property.strategy = IdStrategy.None;
            }
            if (null != resultSet.getString("Default")) {
                property.defaultValue = resultSet.getString("Default");
            }
            property.comment = resultSet.getString("Comment");
            propertyList.add(property);
        }
        resultSet.close();
        preparedStatement.close();
        entity.properties = propertyList;
    }

    /**
     * 从数据库提取表信息
     * */
    @Override
    protected List<Entity> getEntityList() throws SQLException {
        List<Entity> entityList = new ArrayList<>();
        PreparedStatement preparedStatement = connection.prepareStatement("show table status;");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            Entity entity = new Entity();
            entity.tableName = resultSet.getString("Name");
            entity.comment = resultSet.getString("Comment");
            entity.engine = resultSet.getString("Engine");
            entity.charset = resultSet.getString("Collation");
            entityList.add(entity);
        }
        resultSet.close();
        preparedStatement.close();
        return entityList;
    }
}
