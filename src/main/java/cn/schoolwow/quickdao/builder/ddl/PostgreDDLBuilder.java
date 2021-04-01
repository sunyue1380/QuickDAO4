package cn.schoolwow.quickdao.builder.ddl;

import cn.schoolwow.quickdao.annotation.IdStrategy;
import cn.schoolwow.quickdao.annotation.IndexType;
import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.IndexField;
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

public class PostgreDDLBuilder extends AbstractDDLBuilder {
    public PostgreDDLBuilder(QuickDAOConfig quickDAOConfig){
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
    protected String getAutoIncrementSQL(Property property) {
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
        MDC.put("name","生成新表");
        MDC.put("sql",builder.toString());
        connection.prepareStatement(MDC.get("sql")).executeUpdate();
        //创建索引
        for(IndexField indexField:entity.indexFieldList){
            createIndex(indexField);
        }
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
    public boolean hasIndexExists(String tableName, String indexName) throws SQLException {
        String sql = "select count(1) from pg_indexes where tablename = '"+tableName+"' and indexname = '"+indexName+"'";
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

    @Override
    public void enableForeignConstraintCheck(boolean enable) throws SQLException {
    }

    @Override
    public Map<String, String> getTypeFieldMapping() {
        Map<String,String> fieldTypeMapping = new HashMap<>();
        fieldTypeMapping.put("byte","BOOLEAN");
        fieldTypeMapping.put("java.lang.Byte","BOOLEAN");
        fieldTypeMapping.put("[B","BIT");
        fieldTypeMapping.put("boolean","BOOLEAN");
        fieldTypeMapping.put("java.lang.Boolean","BOOLEAN");
        fieldTypeMapping.put("char","CHAR");
        fieldTypeMapping.put("java.lang.Character","CHARACTER");
        fieldTypeMapping.put("short","SMALLINT");
        fieldTypeMapping.put("java.lang.Short","SMALLINT");
        fieldTypeMapping.put("int","INT");
        fieldTypeMapping.put("java.lang.Integer","INTEGER");
        fieldTypeMapping.put("float","FLOAT4");
        fieldTypeMapping.put("java.lang.Float","FLOAT4");
        fieldTypeMapping.put("long","BIGINT");
        fieldTypeMapping.put("java.lang.Long","BIGINT");
        fieldTypeMapping.put("double","FLOAT8");
        fieldTypeMapping.put("java.lang.Double","FLOAT8");
        fieldTypeMapping.put("java.lang.String","VARCHAR(255)");
        fieldTypeMapping.put("java.util.Date","TIMESTAMP");
        fieldTypeMapping.put("java.sql.Date","DATE");
        fieldTypeMapping.put("java.sql.Time","TIME");
        fieldTypeMapping.put("java.sql.Timestamp","TIMESTAMP");
        fieldTypeMapping.put("java.time.LocalDate","DATE");
        fieldTypeMapping.put("java.time.LocalDateTime","TIMESTAMP");
        fieldTypeMapping.put("java.sql.Array","");
        fieldTypeMapping.put("java.math.BigDecimal","DECIMAL");
        fieldTypeMapping.put("java.sql.Blob","TEXT");
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
    private void getIndex(Entity entity) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("select tablename,indexname,indexdef from pg_indexes where tablename = '"+entity.tableName+"'");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            IndexField indexField = new IndexField();
            indexField.tableName = resultSet.getString("tablename");
            indexField.indexName = resultSet.getString("indexname");

            String def = resultSet.getString("indexdef");
            if(def.contains("UNIQUE INDEX")){
                indexField.indexType = IndexType.UNIQUE;
            }else{
                indexField.indexType = IndexType.NORMAL;
            }
            indexField.using = def.substring(def.indexOf("USING")+"USING".length(),def.indexOf("(")).replace("\"","");
            String[] columns = def.substring(def.indexOf("(")+1,def.indexOf(")")).split(",");
            for(int i=0;i<columns.length;i++){
                indexField.columns.add(columns[i]);
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
        List<Property> propertyList = new ArrayList<>();
        {
            PreparedStatement preparedStatement = connection.prepareStatement("select attname as column_name , attnum as oridinal_position, attnotnull as notnull, format_type(atttypid,atttypmod) as type, col_description(attrelid, attnum) as comment from pg_attribute join pg_class on pg_attribute.attrelid = pg_class.oid where attnum > 0 and atttypid > 0 and pg_class.relname='"+entity.tableName+"'");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Property property = new Property();
                property.column = resultSet.getString("column_name");
                property.columnType = resultSet.getString("type");
                property.notNull = "t".equals(resultSet.getString("notnull"));
                property.comment = resultSet.getString("comment");
                property.position = resultSet.getInt("oridinal_position");
                propertyList.add(property);
            }
            resultSet.close();
            preparedStatement.close();
        }
        {
            //提取默认值和主键信息
            PreparedStatement preparedStatement = connection.prepareStatement("select ordinal_position,column_name,column_default,is_nullable,udt_name,character_maximum_length,column_default from information_schema.columns where table_name = '" + entity.tableName + "'");
            final ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Property property = propertyList.stream().filter(property1 -> {
                    try {
                        return property1.column.equals(resultSet.getString("column_name"));
                    } catch (SQLException e) {
                        e.printStackTrace();
                        return false;
                    }
                }).findFirst().orElse(null);
                if(null==property){
                    continue;
                }
                property.columnType = resultSet.getString("udt_name");
                if(null!=resultSet.getObject("character_maximum_length")){
                    property.columnType += "("+resultSet.getInt("character_maximum_length")+")";
                }
                if (null != resultSet.getString("column_default")) {
                    property.defaultValue = resultSet.getString("column_default");
                }
            }
            resultSet.close();
            preparedStatement.close();
        }
        //获取主键约束
        {
            PreparedStatement preparedStatement = connection.prepareStatement("select conkey from pg_constraint join pg_class on pg_class.oid = pg_constraint.conrelid where contype = 'p' and relname = '" + entity.tableName + "'");
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String conkey = resultSet.getString("conkey");
                for(Property property:propertyList){
                    if(conkey.contains(property.position+"")){
                        property.id = true;
                        property.strategy = IdStrategy.AutoIncrement;
                    }
                }
            }
            resultSet.close();
            preparedStatement.close();
        }
        entity.properties = propertyList;
    }

    /**
     * 从数据库提取表信息
     * */
    private List<Entity> getEntityList() throws SQLException {
        List<Entity> entityList = new ArrayList<>();
        PreparedStatement preparedStatement = connection.prepareStatement("select relname as name,cast(obj_description(relfilenode,'pg_class') as varchar) as comment from pg_class c where  relkind = 'r' and relname not like 'pg_%' and relname not like 'sql_%' order by relname");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            Entity entity = new Entity();
            entity.tableName = resultSet.getString("name");
            entity.comment = resultSet.getString("comment");
            entityList.add(entity);
        }
        resultSet.close();
        preparedStatement.close();
        return entityList;
    }
}
