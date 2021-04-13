package cn.schoolwow.quickdao.builder.ddl;

import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.Property;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import cn.schoolwow.quickdao.domain.ThreadLocalMap;

import java.sql.PreparedStatement;
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
    public List<Entity> getDatabaseEntity() throws SQLException {
        PreparedStatement tablePs = connection.prepareStatement("select name from sysobjects where xtype='u' order by name;");
        ResultSet tableRs = tablePs.executeQuery();
        List<Entity> entityList = new ArrayList<>();
        while (tableRs.next()) {
            Entity entity = new Entity();
            entity.tableName = tableRs.getString(1);

            List<Property> propertyList = new ArrayList<>();
            PreparedStatement propertyPs = connection.prepareStatement("select column_name,data_type,is_nullable from information_schema.columns where table_name = '"+entity.tableName+"'");
            ResultSet propertiesRs = propertyPs.executeQuery();
            while (propertiesRs.next()) {
                Property property = new Property();
                property.column = propertiesRs.getString("column_name");
                property.columnType = propertiesRs.getString("data_type");
                property.notNull = "NO".equals(propertiesRs.getString("is_nullable"));
                propertyList.add(property);
            }
            //TODO 获取SQLServer的索引情况
            entity.properties = propertyList;
            entityList.add(entity);
            propertiesRs.close();
            propertyPs.close();
        }
        tableRs.close();
        return entityList;
    }

    @Override
    protected String getAutoIncrementSQL(Property property) {
        return property.column + " " + property.columnType + " identity(1,1) unique ";
    }

    @Override
    public boolean hasTableExists(Entity entity) throws SQLException {
        ResultSet resultSet = connection.prepareStatement("select name from sysobjects where xtype='u' and name = '"+entity.tableName+"';").executeQuery();
        boolean result = false;
        if(resultSet.next()){
            result = true;
        }
        resultSet.close();
        return result;
    }

    @Override
    public boolean hasIndexExists(String tableName, String indexName) throws SQLException {
        String sql = "EXEC Sp_helpindex '"+tableName+"'";
        ThreadLocalMap.put("name","查看索引是否存在");
        ThreadLocalMap.put("sql",sql);
        connection.prepareStatement(ThreadLocalMap.get("sql")).executeUpdate();

        ResultSet resultSet = connection.prepareStatement(sql).executeQuery();
        boolean result = false;
        while(resultSet.next()) {
            if(indexName.equals(resultSet.getString("index_name"))){
                result = true;
                break;
            }
        }
        resultSet.close();
        return result;
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
        fieldTypeMapping.put("java.util.Date","DATETIME");
        fieldTypeMapping.put("java.sql.Date","DATE");
        fieldTypeMapping.put("java.sql.Time","TIME");
        fieldTypeMapping.put("java.sql.Timestamp","TIMESTAMP");
        fieldTypeMapping.put("java.time.LocalDate","DATE");
        fieldTypeMapping.put("java.time.LocalDateTime","DATETIME");
        fieldTypeMapping.put("java.sql.Array","");
        fieldTypeMapping.put("java.math.BigDecimal","DECIMAL");
        fieldTypeMapping.put("java.sql.Blob","");
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
}
