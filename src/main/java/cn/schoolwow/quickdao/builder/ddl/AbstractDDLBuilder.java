package cn.schoolwow.quickdao.builder.ddl;

import cn.schoolwow.quickdao.annotation.IdStrategy;
import cn.schoolwow.quickdao.builder.AbstractSQLBuilder;
import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.IndexField;
import cn.schoolwow.quickdao.domain.Property;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractDDLBuilder extends AbstractSQLBuilder implements DDLBuilder {
    protected Logger logger = LoggerFactory.getLogger(DDLBuilder.class);

    public AbstractDDLBuilder(QuickDAOConfig quickDAOConfig) {
        super(quickDAOConfig);
    }

    @Override
    public void getDatabaseName() throws SQLException{
    }

    @Override
    public List<Entity> getVirtualEntity(){
        return new ArrayList<>();
    }

    @Override
    public List<Entity> getDatabaseEntity() throws SQLException{
        List<Entity> entityList = getEntityList();
        //清空表字段信息
        for(Entity entity:entityList){
            entity.properties.clear();
        }
        getEntityPropertyList(entityList);
        //清空表索引信息
        for(Entity entity:entityList){
            entity.indexFieldList.clear();
        }
        getIndex(entityList);
        return entityList;
    }

    @Override
    public abstract String hasTableExists(Entity entity);

    @Override
    public abstract String createTable(Entity entity);

    @Override
    public String createProperty(Property property){
        StringBuilder createPropertyBuilder = new StringBuilder("alter table " + quickDAOConfig.database.escape(property.entity.tableName) + " add ");
        if(property.id&&property.strategy== IdStrategy.AutoIncrement){
            createPropertyBuilder.append(getAutoIncrementSQL(property));
        }else{
            createPropertyBuilder.append(quickDAOConfig.database.escape(property.column) + " " + property.columnType + (null==property.length?"":"("+property.length+")"));
            if (null!=property.defaultValue&&!property.defaultValue.isEmpty()) {
                createPropertyBuilder.append(" default " + property.defaultValue);
            }
            if (property.notNull) {
                createPropertyBuilder.append(" not null");
            }
            if (null!=property.escapeCheck&&!property.escapeCheck.isEmpty()) {
                createPropertyBuilder.append(" check " + property.escapeCheck);
            }
            if (null != property.comment) {
                createPropertyBuilder.append(" "+quickDAOConfig.database.comment(property.comment));
            }
            if (null != property.after) {
                createPropertyBuilder.append(" after "+quickDAOConfig.database.escape(property.after));
            }
        }
        createPropertyBuilder.append(";");
        return createPropertyBuilder.toString();
    }

    @Override
    public String alterColumn(Property property){
        StringBuilder builder = new StringBuilder("alter table " + quickDAOConfig.database.escape(property.entity.tableName));
        builder.append(" modify column " + quickDAOConfig.database.escape(property.column) + " " + property.columnType + (null==property.length?"":"("+property.length+")")+";");
        return builder.toString();
    }

    @Override
    public String dropColumn(Property property){
        StringBuilder builder = new StringBuilder("alter table ");
        if(null!=quickDAOConfig.databaseName){
            builder.append(quickDAOConfig.database.escape(quickDAOConfig.databaseName)+".");
        }
        builder.append(quickDAOConfig.database.escape(property.entity.tableName));
        builder.append(" drop column "+quickDAOConfig.database.escape(property.column)+";");
        return builder.toString();
    }

    @Override
    public String dropTable(String tableName) {
        String dropTableSQL = "drop table " + quickDAOConfig.database.escape(tableName) + ";";
        return dropTableSQL;
    }

    @Override
    public abstract String hasIndexExists(String tableName, String indexName);

    @Override
    public String hasConstraintExists(String tableName, String constraintName) throws SQLException {
        String hasConstraintExistsSQL = "select constraint_name from information_schema.KEY_COLUMN_USAGE where constraint_name='" + constraintName + "';";
        return hasConstraintExistsSQL;
    }

    @Override
    public String createIndex(IndexField indexField) {
        StringBuilder builder = new StringBuilder("create");
        switch (indexField.indexType){
            case NORMAL:{}break;
            case UNIQUE:{builder.append(" unique");}break;
            case FULLTEXT:{builder.append(" fulltext");}break;
        }
        builder.append(" index " + quickDAOConfig.database.escape(indexField.indexName) + " on " + quickDAOConfig.database.escape(indexField.tableName) + "(");
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
        builder.append(";");
        return builder.toString();
    }

    @Override
    public String dropIndex(String tableName, String indexName){
        String dropIndexSQL = "drop index "+quickDAOConfig.database.escape(indexName) + ";";
        return dropIndexSQL;
    }

    @Override
    public String createForeignKey(Property property) throws SQLException {
        String operation = property.foreignKey.foreignKeyOption().getOperation();
        String reference = quickDAOConfig.database.escape(quickDAOConfig.getEntityByClassName(property.foreignKey.table().getName()).tableName) + "(" + quickDAOConfig.database.escape(property.foreignKey.field()) + ") ON DELETE " + operation + " ON UPDATE " + operation;
        String foreignKeyName = "FK_" + property.entity.tableName + "_" + property.foreignKey.field() + "_" + quickDAOConfig.getEntityByClassName(property.foreignKey.table().getName()).tableName + "_" + property.name;
        return "alter table " + quickDAOConfig.database.escape(property.entity.tableName) + " add constraint " + quickDAOConfig.database.escape(foreignKeyName) + " foreign key(" + quickDAOConfig.database.escape(property.column) + ") references " + reference + ";";
    }

    @Override
    public abstract Map<String,String> getTypeFieldMapping();

    /**
     * 获取自增语句
     * @param property 自增字段信息
     * */
    protected abstract String getAutoIncrementSQL(Property property);

    /**
     * 提取索引信息
     * */
    protected abstract void getIndex(List<Entity> entityList) throws SQLException;

    /**
     * 提取表字段信息
     * */
    protected abstract void getEntityPropertyList(List<Entity> entityList) throws SQLException;

    /**
     * 从数据库提取表信息
     * */
    protected abstract List<Entity> getEntityList() throws SQLException;
}