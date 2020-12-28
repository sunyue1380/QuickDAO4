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
    public String getAutoIncrementSQL(Property property) {
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
    public boolean hasIndexExists(Entity entity, IndexType indexType) throws SQLException {
        String indexName = entity.tableName+"_"+indexType.name();
        String sql = "EXEC Sp_helpindex '"+entity.tableName+"'";
        MDC.put("name","查看索引是否存在");
        MDC.put("sql",sql);
        connection.prepareStatement(MDC.get("sql")).executeUpdate();

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
}
