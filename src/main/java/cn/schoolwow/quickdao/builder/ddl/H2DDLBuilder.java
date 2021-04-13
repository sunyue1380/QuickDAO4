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

public class H2DDLBuilder extends MySQLDDLBuilder {

    public H2DDLBuilder(QuickDAOConfig quickDAOConfig) {
        super(quickDAOConfig);
    }

    @Override
    public List<Entity> getDatabaseEntity() throws SQLException {
        List<Entity> entityList = getEntityList();
        for(Entity entity:entityList){
            getEntityPropertyList(entity);
            getIndex(entity);
        }
        return entityList;
    }

    @Override
    public boolean hasTableExists(Entity entity) throws SQLException {
        ResultSet resultSet = connection.prepareStatement("select table_name from information_schema.tables where table_name = '"+entity.tableName.toUpperCase()+"'").executeQuery();
        boolean result = false;
        if(resultSet.next()){
            result = true;
        }
        resultSet.close();
        return result;
    }

    @Override
    public boolean hasIndexExists(String tableName, String indexName) throws SQLException {
        String sql = "select count(1) from information_schema.indexes where index_name = '"+indexName.toUpperCase()+"'";
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
    public Map<String, String> getTypeFieldMapping() {
        Map<String,String> fieldTypeMapping = new HashMap<>();
        fieldTypeMapping.put("byte","TINYINT");
        fieldTypeMapping.put("java.lang.Byte","TINYINT");
        fieldTypeMapping.put("[B","BINARY");
        fieldTypeMapping.put("boolean","BOOLEAN");
        fieldTypeMapping.put("java.lang.Boolean","BOOLEAN");
        fieldTypeMapping.put("char","CHAR");
        fieldTypeMapping.put("java.lang.Character","CHARACTER");
        fieldTypeMapping.put("short","SMALLINT");
        fieldTypeMapping.put("java.lang.Short","SMALLINT");
        fieldTypeMapping.put("int","INT");
        fieldTypeMapping.put("java.lang.Integer","INTEGER");
        fieldTypeMapping.put("float","REAL");
        fieldTypeMapping.put("java.lang.Float","REAL");
        fieldTypeMapping.put("long","BIGINT");
        fieldTypeMapping.put("java.lang.Long","BIGINT");
        fieldTypeMapping.put("double","DOUBLE");
        fieldTypeMapping.put("java.lang.Double","DOUBLE");
        fieldTypeMapping.put("java.lang.String","VARCHAR(255)");
        fieldTypeMapping.put("java.util.Date","DATETIME");
        fieldTypeMapping.put("java.sql.Date","DATE");
        fieldTypeMapping.put("java.sql.Time","TIME");
        fieldTypeMapping.put("java.sql.Timestamp","TIMESTAMP");
        fieldTypeMapping.put("java.time.LocalDate","DATE");
        fieldTypeMapping.put("java.time.LocalDateTime","DATETIME");
        fieldTypeMapping.put("java.sql.Array","ARRAY");
        fieldTypeMapping.put("java.math.BigDecimal","DECIMAL");
        fieldTypeMapping.put("java.sql.Blob","BLOB");
        fieldTypeMapping.put("java.sql.Clob","CLOB");
        fieldTypeMapping.put("java.sql.NClob","NCLOB");
        fieldTypeMapping.put("java.sql.Ref","");
        fieldTypeMapping.put("java.net.URL","");
        fieldTypeMapping.put("java.sql.RowId","");
        fieldTypeMapping.put("java.sql.SQLXML","");
        fieldTypeMapping.put("java.io.InputStream","LONGTEXT");
        fieldTypeMapping.put("java.io.Reader","LONGTEXT");
        return fieldTypeMapping;
    }

    /**
     * 提取索引信息
     * */
    private void getIndex(Entity entity) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("select sql from information_schema.indexes where table_name ='" + entity.tableName+"'");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            String sql = resultSet.getString("sql");
            String[] tokens = sql.split("\"");
            IndexField indexField = new IndexField();
            if(tokens[0].contains("UNIQUE")){
                indexField.indexType = IndexType.UNIQUE;
            }else{
                indexField.indexType = IndexType.NORMAL;
            }
            indexField.indexName = tokens[3];
            indexField.tableName = tokens[7];
            for(int i=9;i<tokens.length-1;i++){
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
    private void getEntityPropertyList(Entity entity) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("show columns from " + quickDAOConfig.database.escape(entity.tableName));
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
                property.strategy = IdStrategy.AutoIncrement;
            }
            if (null != resultSet.getString("Default")) {
                property.defaultValue = resultSet.getString("Default");
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
    private List<Entity> getEntityList() throws SQLException {
        List<Entity> entityList = new ArrayList<>();
        PreparedStatement preparedStatement = connection.prepareStatement("show tables;");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            Entity entity = new Entity();
            entity.tableName = resultSet.getString(1);
            entityList.add(entity);
        }
        resultSet.close();
        preparedStatement.close();
        return entityList;
    }
}
