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
import java.util.Arrays;
import java.util.List;

public class MySQLDDLBuilder extends AbstractDDLBuilder {
    public MySQLDDLBuilder(QuickDAOConfig quickDAOConfig) {
        super(quickDAOConfig);
    }

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
    public List<Entity> getDatabaseEntity() throws SQLException {
        PreparedStatement tablePs = connection.prepareStatement("select table_name,table_comment from information_schema.tables where table_schema = database()");
        ResultSet tableRs = tablePs.executeQuery();
        List<Entity> entityList = new ArrayList<>();
        while (tableRs.next()) {
            Entity entity = new Entity();
            entity.tableName = tableRs.getString(1);
            entity.comment = tableRs.getString(2);

            List<Property> propertyList = new ArrayList<>();
            //获取所有列
            {
                connection.getMetaData().getUserName();
                ResultSet propertiesRs = connection.prepareStatement("show full columns from " + quickDAOConfig.database.escape(entity.tableName)).executeQuery();
                while (propertiesRs.next()) {
                    Property property = new Property();
                    property.column = propertiesRs.getString("Field");
                    //无符号填充0 => float unsigned zerofill
                    property.columnType = propertiesRs.getString("Type");
                    if(property.columnType.contains(" ")){
                        property.columnType = property.columnType.substring(0,property.columnType.indexOf(" "));
                    }
                    property.notNull = "NO".equals(propertiesRs.getString("Null"));
                    String key = propertiesRs.getString("Key");
                    if(null!=key){
                        switch(key){
                            case "PRI":{property.id = true;}break;
                            case "UNI":{property.unique = true;}break;
                        }
                    }
                    if("auto_increment".equals(propertiesRs.getString("Extra"))){
                        property.id = true;
                        property.strategy = IdStrategy.AutoIncrement;
                    }else{
                        property.strategy = IdStrategy.None;
                    }
                    if (null != propertiesRs.getString("Default")) {
                        property.defaultValue = propertiesRs.getString("Default");
                    }
                    property.comment = propertiesRs.getString("Comment");
                    propertyList.add(property);
                }
                propertiesRs.close();
            }
            //处理索引
            {
                ResultSet resultSet = connection.prepareStatement("show index from " + quickDAOConfig.database.escape(entity.tableName)).executeQuery();
                while (resultSet.next()) {
                    String columnName = resultSet.getString("Column_name");
                    for(Property property:propertyList){
                        if(property.column.equals(columnName)){
                            int nonUnique = resultSet.getInt("Non_unique");
                            if(nonUnique==0){
                                property.unique = true;
                            }else{
                                property.index = true;
                            }
                            break;
                        }
                    }
                }
                resultSet.close();
            }
            entity.properties = propertyList;
            entityList.add(entity);
        }
        tableRs.close();
        return entityList;
    }

    @Override
    public String getAutoIncrementSQL(Property property) {
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
        StringBuilder createTableBuilder = getCreateTableBuilder(entity);
        //添加表引擎
        String engine = entity.engine;
        if(null==engine||engine.isEmpty()){
            engine = quickDAOConfig.engine;
        }
        if(null!=engine&&!engine.isEmpty()) {
            createTableBuilder.append(" ENGINE="+engine);
        }
        //指定表编码
        String charset = entity.charset;
        if(null==charset||charset.isEmpty()){
            charset = quickDAOConfig.charset;
        }
        if(null!=charset&&!charset.isEmpty()){
            createTableBuilder.append(" DEFAULT CHARSET="+charset);
        }
        MDC.put("name","生成新表");
        MDC.put("sql",createTableBuilder.toString());
        connection.prepareStatement(MDC.get("sql")).executeUpdate();
    }

    @Override
    public boolean hasIndexExists(Entity entity, IndexType indexType) throws SQLException {
        String indexName = entity.tableName+"_"+indexType.name();
        String sql = "show index from "+quickDAOConfig.database.escape(entity.tableName)+" where key_name = '"+indexName+"'";

        MDC.put("name","查看索引是否存在");
        MDC.put("sql",sql);
        ResultSet resultSet = connection.prepareStatement(sql).executeQuery();
        boolean result = false;
        if (resultSet.next()) {
            result = true;
        }
        resultSet.close();
        return result;
    }

    @Override
    public void dropIndex(Entity entity, IndexType indexType) throws SQLException{
        String indexName = entity.tableName+"_"+indexType.name();
        String dropIndexSQL = "drop index "+quickDAOConfig.database.escape(indexName)+" on "+quickDAOConfig.database.escape(entity.tableName);
        MDC.put("name","删除索引");
        MDC.put("sql",dropIndexSQL);
        connection.prepareStatement(MDC.get("sql")).executeUpdate();
    }

    /**获取虚拟表信息*/
    protected List<Entity> getVirtualEntity(){
        Entity entity = new Entity();
        entity.tableName = "dual";
        entity.escapeTableName = "dual";
        entity.properties = new ArrayList<>();
        return Arrays.asList(entity);
    }
}
