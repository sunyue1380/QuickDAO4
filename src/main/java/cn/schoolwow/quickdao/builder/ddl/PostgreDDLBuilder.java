package cn.schoolwow.quickdao.builder.ddl;

import cn.schoolwow.quickdao.annotation.IdStrategy;
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

public class PostgreDDLBuilder extends AbstractDDLBuilder {
    public PostgreDDLBuilder(QuickDAOConfig quickDAOConfig){
        super(quickDAOConfig);
    }

    @Override
    public List<Entity> getDatabaseEntity() throws SQLException {
        List<Entity> entityList = new ArrayList<>();
        PreparedStatement tablePs = connection.prepareStatement("select tablename from pg_tables where schemaname='public';");
        ResultSet tableRs = tablePs.executeQuery();
        while (tableRs.next()) {
            Entity entity = new Entity();
            entity.tableName = tableRs.getString("tablename");
            List<Property> propertyList = new ArrayList<>();
            //获取列
            {
                ResultSet propertiesRs = connection.prepareStatement("select ordinal_position,column_name,column_default,is_nullable,udt_name,character_maximum_length from information_schema.columns where table_name = '" + entity.tableName + "'").executeQuery();
                while (propertiesRs.next()) {
                    Property property = new Property();
                    property.column = propertiesRs.getString("column_name");
                    property.columnType = propertiesRs.getString("udt_name");
                    if(null!=propertiesRs.getString("character_maximum_length")){
                        property.columnType += "("+propertiesRs.getString("character_maximum_length")+")";
                    }
                    property.notNull = "NO".equals(propertiesRs.getString("is_nullable"));
                    if (null != propertiesRs.getString("column_default")) {
                        property.defaultValue = propertiesRs.getString("column_default");
                    }
                    property.position = propertiesRs.getInt("ordinal_position");
                    propertyList.add(property);
                }
                propertiesRs.close();
            }
            //获取主键约束
            {
                ResultSet primaryKeyRs = connection.prepareStatement("select conkey from pg_constraint join pg_class on pg_class.oid = pg_constraint.conrelid where contype = 'p' and relname = '" + entity.tableName + "'").executeQuery();
                while (primaryKeyRs.next()) {
                    String conkey = primaryKeyRs.getString("conkey");
                    for(Property property:propertyList){
                        if(conkey.contains(property.position+"")){
                            property.id = true;
                            property.strategy = IdStrategy.AutoIncrement;
                        }
                    }
                }
                primaryKeyRs.close();
            }
            //获取注释
            {
                ResultSet commentRs = connection.prepareStatement("select objsubid,description from pg_description join pg_class on pg_description.objoid = pg_class.oid where relname = '" + entity.tableName + "'").executeQuery();
                while (commentRs.next()) {
                    int position = commentRs.getInt("objsubid");
                    if(position==0){
                        entity.comment = commentRs.getString("description");
                    }else{
                        for(Property property:propertyList){
                            if(property.position==position){
                                property.comment = commentRs.getString("description");
                                break;
                            }
                        }
                    }
                }
                commentRs.close();
            }
            //获取索引信息
            updateTableIndex("select indexdef from pg_indexes where tablename='"+entity.tableName+"';",propertyList);
            entity.properties = propertyList;
            entityList.add(entity);
        }
        tableRs.close();
        return entityList;
    }

    @Override
    public String getAutoIncrementSQL(Property property) {
        return property.column + " SERIAL UNIQUE PRIMARY KEY";
    }

    @Override
    public boolean hasTableExists(Entity entity) throws SQLException {
        ResultSet resultSet = connection.prepareStatement("select tablename from pg_tables where schemaname='public' and tablename = '"+entity.tableName+"';").executeQuery();
        boolean result = false;
        if(resultSet.next()){
            result = true;
        }
        resultSet.close();
        return result;
    }

    @Override
    public void createTable(Entity entity) throws SQLException {
        super.createTable(entity);
        //创建注释
        String entityCommentSQL = "comment on table \"" + entity.tableName + "\" is '" + entity.comment + "'";
        connection.prepareStatement(entityCommentSQL).executeUpdate();
        for (Property property : entity.properties) {
            if (property.comment == null) {
                continue;
            }
            String columnCommentSQL = "comment on column \"" + entity.tableName + "\".\"" + property.column + "\" is '" + property.comment + "'";
            connection.prepareStatement(columnCommentSQL).executeUpdate();
        }
    }

    @Override
    public boolean hasIndexExists(Entity entity, IndexType indexType) throws SQLException {
        String indexName = entity.tableName+"_"+indexType.name();
        String sql = "select count(1) from pg_indexes where tablename = '"+entity.tableName+"' and indexname = '"+indexName+"'";
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
