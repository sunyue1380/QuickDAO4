package cn.schoolwow.quickdao.builder.ddl;

import cn.schoolwow.quickdao.annotation.IdStrategy;
import cn.schoolwow.quickdao.annotation.IndexType;
import cn.schoolwow.quickdao.domain.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLiteDDLBuilder extends AbstractDDLBuilder {
    public SQLiteDDLBuilder(QuickDAOConfig quickDAOConfig) {
        super(quickDAOConfig);
    }

    @Override
    public boolean hasTableExists(Entity entity) throws SQLException {
        ResultSet resultSet = connection.prepareStatement("select name from sqlite_master where type='table' and name = '"+entity.tableName+"';").executeQuery();
        boolean result = false;
        if(resultSet.next()){
            result = true;
        }
        resultSet.close();
        return result;
    }

    @Override
    public void createTable(Entity entity) throws SQLException {
        if (quickDAOConfig.openForeignKey&&null!=entity.foreignKeyProperties&&entity.foreignKeyProperties.size()>0) {
            //手动开启外键约束
            connection.prepareStatement("PRAGMA foreign_keys = ON;").executeUpdate();
        }
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
        if (null != entity.comment) {
            builder.append(" "+quickDAOConfig.database.comment(entity.comment));
        }
        ThreadLocalMap.put("name","生成新表");
        ThreadLocalMap.put("sql",builder.toString());
        connection.prepareStatement(ThreadLocalMap.get("sql")).executeUpdate();
        //创建索引
        for(IndexField indexField:entity.indexFieldList){
            createIndex(indexField);
        }
    }

    @Override
    protected String getAutoIncrementSQL(Property property){
        return property.column + " " + property.columnType + " primary key autoincrement";
    }

    @Override
    public void dropColumn(Property property) throws SQLException{
        throw new UnsupportedOperationException("SQLite不支持删除列");
    }

    @Override
    public boolean hasIndexExists(String tableName, String indexName) throws SQLException {
        String sql = "select count(1) from sqlite_master where type = 'index' and name = '"+indexName+"'";
        ThreadLocalMap.put("name","查看索引是否存在");
        ThreadLocalMap.put("sql",sql);

        ResultSet resultSet = connection.prepareStatement(sql).executeQuery();
        boolean result = false;
        if (resultSet.next()) {
            result = resultSet.getInt(1) > 0;
        }
        resultSet.close();
        return result;
    }

    @Override
    public void enableForeignConstraintCheck(boolean enable) throws SQLException {
        String foreignConstraintCheckSQL = "PRAGMA foreign_keys = " + enable;
        ThreadLocalMap.put("name",enable?"启用外键约束检查":"禁用外键约束检查");
        ThreadLocalMap.put("sql",foreignConstraintCheckSQL);
        connection.prepareStatement(ThreadLocalMap.get("sql")).executeUpdate();
    }

    @Override
    public Map<String, String> getTypeFieldMapping() {
        Map<String,String> fieldTypeMapping = new HashMap<>();
        fieldTypeMapping.put("byte","TINYINT");
        fieldTypeMapping.put("java.lang.Byte","TINYINT");
        fieldTypeMapping.put("[B","BLOB");
        fieldTypeMapping.put("boolean","BOOLEAN");
        fieldTypeMapping.put("java.lang.Boolean","BOOLEAN");
        fieldTypeMapping.put("char","TINYINT");
        fieldTypeMapping.put("java.lang.Character","TINYINT");
        fieldTypeMapping.put("short","SMALLINT");
        fieldTypeMapping.put("java.lang.Short","SMALLINT");
        fieldTypeMapping.put("int","INT");
        fieldTypeMapping.put("java.lang.Integer","INTEGER");
        fieldTypeMapping.put("float","FLOAT");
        fieldTypeMapping.put("java.lang.Float","FLOAT");
        fieldTypeMapping.put("long","INTEGER");
        fieldTypeMapping.put("java.lang.Long","INTEGER");
        fieldTypeMapping.put("double","DOUBLE");
        fieldTypeMapping.put("java.lang.Double","DOUBLE");
        fieldTypeMapping.put("java.lang.String","VARCHAR(255)");
        fieldTypeMapping.put("java.util.Date","DATETIME");
        fieldTypeMapping.put("java.sql.Date","DATE");
        fieldTypeMapping.put("java.sql.Time","");
        fieldTypeMapping.put("java.sql.Timestamp","DATETIME");
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
        fieldTypeMapping.put("java.io.InputStream","TEXT");
        fieldTypeMapping.put("java.io.Reader","TEXT");
        return fieldTypeMapping;
    }

    /**
     * 提取索引信息
     * */
    @Override
    protected void getIndex(Entity entity) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("select sql from sqlite_master where type='index' and sql is not null and tbl_name = '" + entity.tableName+"'");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            String sql = resultSet.getString("sql");
            String[] tokens = sql.split("`");
            IndexField indexField = new IndexField();
            if(tokens[0].contains("UNIQUE")){
                indexField.indexType = IndexType.UNIQUE;
            }else{
                indexField.indexType = IndexType.NORMAL;
            }
            indexField.indexName = tokens[1];
            indexField.tableName = tokens[3];
            for(int i=5;i<tokens.length-1;i++){
                indexField.columns.add(tokens[i]);
            }
            entity.indexFieldList.add(indexField);
        }
        resultSet.close();
        preparedStatement.close();
    }

    /**
     * 提取表字段信息
     * */
    @Override
    protected void getEntityPropertyList(Entity entity) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("PRAGMA table_info(`" + entity.tableName + "`)");
        ResultSet resultSet = preparedStatement.executeQuery();
        List<Property> propertyList = new ArrayList<>();
        while (resultSet.next()) {
            Property property = new Property();
            property.column = resultSet.getString("name");
            property.columnType = resultSet.getString("type");
            property.notNull = "1".equals(resultSet.getString("notnull"));
            if (null != resultSet.getString("dflt_value")) {
                property.defaultValue = resultSet.getString("dflt_value");
            }
            if(1==resultSet.getInt("pk")){
                property.id = true;
                property.strategy = IdStrategy.AutoIncrement;
            }
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
        PreparedStatement preparedStatement = connection.prepareStatement("select name from sqlite_master where type='table' and name != 'sqlite_sequence';");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            Entity entity = new Entity();
            entity.tableName = resultSet.getString("name");
            entityList.add(entity);
        }
        resultSet.close();
        preparedStatement.close();
        return entityList;
    }
}
