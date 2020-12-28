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
import java.util.List;

public class H2DDLBuilder extends MySQLDDLBuilder {

    public H2DDLBuilder(QuickDAOConfig quickDAOConfig) {
        super(quickDAOConfig);
    }

    @Override
    public List<Entity> getDatabaseEntity() throws SQLException {
        PreparedStatement tablePs = connection.prepareStatement("show tables;");
        ResultSet tableRs = tablePs.executeQuery();
        List<Entity> entityList = new ArrayList<>();
        while (tableRs.next()) {
            Entity entity = new Entity();
            entity.tableName = tableRs.getString(1);

            List<Property> propertyList = new ArrayList<>();
            //获取所有列
            {
                ResultSet propertiesRs = connection.prepareStatement("show columns from " + quickDAOConfig.database.escape(entity.tableName)).executeQuery();
                while (propertiesRs.next()) {
                    Property property = new Property();
                    property.column = propertiesRs.getString("Field");
                    property.columnType = propertiesRs.getString("Type");
                    property.notNull = "NO".equals(propertiesRs.getString("Null"));
                    property.unique = "UNI".equals(propertiesRs.getString("Key"));
                    if (null != propertiesRs.getString("Default")) {
                        property.defaultValue = propertiesRs.getString("Default");
                    }
                    propertyList.add(property);
                }
                propertiesRs.close();
            }
            //处理索引
            updateTableIndex("SELECT SQL FROM INFORMATION_SCHEMA.INDEXES WHERE TABLE_NAME = '"+entity.tableName.toUpperCase()+"'",propertyList);
            entity.properties = propertyList;
            entityList.add(entity);
        }
        tableRs.close();
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
    public boolean hasIndexExists(Entity entity, IndexType indexType) throws SQLException {
        String indexName = (entity.tableName+"_"+indexType.name()).toUpperCase();
        String sql = "select count(1) from information_schema.indexes where index_name = '"+indexName+"'";
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
