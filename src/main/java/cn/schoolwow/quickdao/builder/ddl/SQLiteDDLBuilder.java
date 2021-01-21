package cn.schoolwow.quickdao.builder.ddl;

import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.IndexType;
import cn.schoolwow.quickdao.domain.Property;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import org.slf4j.MDC;

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
    public List<Entity> getDatabaseEntity() throws SQLException {
        PreparedStatement tablePs = connection.prepareStatement("select name from sqlite_master where type='table' and name != 'sqlite_sequence';");
        ResultSet tableRs = tablePs.executeQuery();
        List<Entity> entityList = new ArrayList<>();
        while (tableRs.next()) {
            Entity entity = new Entity();
            entity.tableName = tableRs.getString(1);

            List<Property> propertyList = new ArrayList<>();
            //获取所有列
            {
                ResultSet propertiesRs = connection.prepareStatement("PRAGMA table_info(`" + entity.tableName + "`)").executeQuery();
                while (propertiesRs.next()) {
                    Property property = new Property();
                    property.column = propertiesRs.getString("name");
                    property.columnType = propertiesRs.getString("type");
                    property.notNull = "1".equals(propertiesRs.getString("notnull"));
                    if (null != propertiesRs.getString("dflt_value")) {
                        property.defaultValue = propertiesRs.getString("dflt_value");
                    }
                    propertyList.add(property);
                }
                propertiesRs.close();
            }
            updateTableIndex("SELECT sql FROM sqlite_master WHERE type='index' and tbl_name = '"+entity.tableName+"';",propertyList);
            entity.properties = propertyList;
            entityList.add(entity);
        }
        tableRs.close();
        tablePs.close();
        return entityList;
    }

    @Override
    public void createTable(Entity entity) throws SQLException {
        if (quickDAOConfig.openForeignKey&&null!=entity.foreignKeyProperties&&entity.foreignKeyProperties.size()>0) {
            //手动开启外键约束
            connection.prepareStatement("PRAGMA foreign_keys = ON;").executeUpdate();
        }
        super.createTable(entity);
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
    protected boolean hasIndexExists(Entity entity, IndexType indexType) throws SQLException {
        String indexName = entity.tableName+"_"+indexType.name();
        String sql = "select count(1) from sqlite_master where type = 'index' and name = '"+indexName+"'";
        MDC.put("name","查看索引是否存在");
        MDC.put("sql",sql);

        ResultSet resultSet = connection.prepareStatement(sql).executeQuery();
        boolean result = false;
        if (resultSet.next()) {
            result = resultSet.getInt(1) > 0;
        }
        resultSet.close();
        return result;
    }
}
