package cn.schoolwow.quickdao.builder.ddl;

import cn.schoolwow.quickdao.annotation.IdStrategy;
import cn.schoolwow.quickdao.builder.AbstractSQLBuilder;
import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.IndexField;
import cn.schoolwow.quickdao.domain.Property;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public abstract class AbstractDDLBuilder extends AbstractSQLBuilder implements DDLBuilder {
    protected Logger logger = LoggerFactory.getLogger(DDLBuilder.class);

    public AbstractDDLBuilder(QuickDAOConfig quickDAOConfig) {
        super(quickDAOConfig);
    }

    @Override
    public String getDatabaseName() throws SQLException{
        return null;
    }

    @Override
    public List<Entity> getDatabaseEntity() throws SQLException{
        List<Entity> entityList = getEntityList();
        for(Entity entity:entityList){
            getEntityPropertyList(entity);
            getIndex(entity);
        }
        return entityList;
    }

    @Override
    public abstract boolean hasTableExists(Entity entity) throws SQLException;

    @Override
    public void createTable(Entity entity) throws SQLException {
        StringBuilder builder = getCreateTableBuilder(entity);
        connectionExecutor.executeUpdate("生成新表",builder.toString());
    }

    @Override
    public void createProperty(Property property) throws SQLException{
        StringBuilder createPropertyBuilder = new StringBuilder("alter table " + quickDAOConfig.database.escape(property.entity.tableName) + " add " + quickDAOConfig.database.escape(property.column) + " " + property.columnType);
        if (property.notNull) {
            createPropertyBuilder.append(" not null");
        }
        if (null!=property.defaultValue&&!property.defaultValue.isEmpty()) {
            createPropertyBuilder.append(" default " + property.defaultValue);
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
        createPropertyBuilder.append(";");
        connectionExecutor.executeUpdate("添加新列",createPropertyBuilder.toString());
    }

    @Override
    public void alterColumn(Property property) throws SQLException{
        StringBuilder builder = new StringBuilder("alter table " + quickDAOConfig.database.escape(property.entity.tableName));
        builder.append(" alter column "+quickDAOConfig.database.escape(property.column)+" "+property.columnType);
        connectionExecutor.executeUpdate("修改数据类型",builder.toString());
    }

    @Override
    public void dropColumn(Property property) throws SQLException{
        StringBuilder builder = new StringBuilder("alter table ");
        if(null!=quickDAOConfig.databaseName){
            builder.append(quickDAOConfig.database.escape(quickDAOConfig.databaseName)+".");
        }
        builder.append(quickDAOConfig.database.escape(property.entity.tableName));
        builder.append(" drop column "+quickDAOConfig.database.escape(property.column)+";");

        connectionExecutor.executeUpdate("删除列",builder.toString());
    }

    @Override
    public void dropTable(String tableName) throws SQLException {
        String sql = "drop table "+quickDAOConfig.database.escape(tableName);
        connectionExecutor.executeUpdate("删除表",sql);
    }

    @Override
    public void rebuild(Entity entity) throws SQLException {
        if(hasTableExists(entity)){
            dropTable(entity.tableName);
        }
        createTable(entity);
    }

    @Override
    public abstract boolean hasIndexExists(String tableName, String indexName) throws SQLException;

    @Override
    public boolean hasConstraintExists(String tableName, String constraintName) throws SQLException {
        String hasConstraintExistsSQL = "select count(1) from information_schema.KEY_COLUMN_USAGE where constraint_name='" + constraintName + "'";
        ResultSet resultSet = connectionExecutor.executeQuery("判断索引是否存在",hasConstraintExistsSQL);
        boolean result = false;
        if (resultSet.next()) {
            result = resultSet.getInt(1) > 0;
        }
        resultSet.close();
        return result;
    }

    @Override
    public void createIndex(IndexField indexField) throws SQLException {
        if(indexField.columns.isEmpty()){
            return;
        }
        StringBuilder builder = new StringBuilder("create ");
        switch (indexField.indexType){
            case NORMAL:{}break;
            case UNIQUE:{builder.append("unique");}break;
            case FULLTEXT:{builder.append("fulltext");}break;
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
        connectionExecutor.executeUpdate("添加索引",builder.toString());
    }

    @Override
    public void dropIndex(String tableName, String indexName) throws SQLException{
        String dropIndexSQL = "drop index "+quickDAOConfig.database.escape(indexName);
        connectionExecutor.executeUpdate("删除索引",dropIndexSQL);
    }

    @Override
    public void createForeignKey(Property property) throws SQLException{
        if(!quickDAOConfig.openForeignKey){
            return;
        }
        String operation = property.foreignKey.foreignKeyOption().getOperation();
        String reference = quickDAOConfig.database.escape(quickDAOConfig.getEntityByClassName(property.foreignKey.table().getName()).tableName) + "(" + quickDAOConfig.database.escape(property.foreignKey.field()) + ") ON DELETE " + operation + " ON UPDATE " + operation;
        String foreignKeyName = "FK_" + property.entity.tableName + "_" + property.foreignKey.field() + "_" + quickDAOConfig.getEntityByClassName(property.foreignKey.table().getName()).tableName + "_" + property.name;
        if (hasConstraintExists(property.entity.tableName,foreignKeyName)) {
            return;
        }
        String foreignKeySQL = "alter table " + quickDAOConfig.database.escape(property.entity.tableName) + " add constraint " + quickDAOConfig.database.escape(foreignKeyName) + " foreign key(" + quickDAOConfig.database.escape(property.column) + ") references " + reference;
        connectionExecutor.executeUpdate("生成外键约束",foreignKeySQL);
    }

    @Override
    public void automaticCreateTableAndColumn() throws SQLException {
        Collection<Entity> entityList = quickDAOConfig.entityMap.values();
        List<Entity> newEntityList = new ArrayList<>();
        Map<Entity,Entity> updateEntityMap = new HashMap<>();
        for (Entity entity : entityList) {
            Entity dbEntity = quickDAOConfig.dbEntityList.stream().filter(entity1 -> entity1.tableName.equalsIgnoreCase(entity.tableName)).findFirst().orElse(null);
            if(null==dbEntity){
                newEntityList.add(entity);
            }else{
                updateEntityMap.put(entity,dbEntity);
            }
        }
        //自动建表
        if (quickDAOConfig.autoCreateTable) {
            for(Entity entity: newEntityList){
                createTable(entity);
            }
        }
        //自动新增字段和索引
        if(quickDAOConfig.autoCreateProperty){
            for(Map.Entry<Entity,Entity> entry:updateEntityMap.entrySet()){
                List<Property> propertyList = entry.getKey().properties;
                for(Property property:propertyList){
                    if(entry.getValue().properties.stream().noneMatch(property1 -> property1.column.equals(property.column))){
                        createProperty(property);
                    }
                    if(null!=property.foreignKey){
                        createForeignKey(property);
                    }
                }

                List<IndexField> indexFieldList = entry.getKey().indexFieldList;
                for(IndexField indexField:indexFieldList){
                    if(entry.getValue().indexFieldList.stream().noneMatch(indexField1 -> indexField1.indexName.equalsIgnoreCase(indexField.indexName))){
                        createIndex(indexField);
                    }
                }
            }
        }
        //添加虚拟表
        if(null==quickDAOConfig.visualTableList||quickDAOConfig.visualTableList.isEmpty()){
            quickDAOConfig.visualTableList = getVirtualEntity();
        }
    }

    @Override
    public void refreshDbEntityList() throws SQLException {
        List<Entity> dbEntityList = getDatabaseEntity();
        for (Entity dbEntity : dbEntityList) {
            dbEntity.escapeTableName = quickDAOConfig.database.escape(dbEntity.tableName);
            dbEntity.clazz = JSONObject.class;
            for (Property property : dbEntity.properties) {
                if(null!=quickDAOConfig.columnTypeMapping){
                    Class type = quickDAOConfig.columnTypeMapping.columnMappingType(property);
                    if(null!=type){
                        property.className = type.getName();
                    }
                }
                property.entity = dbEntity;
            }
        }
        quickDAOConfig.dbEntityList = dbEntityList;
    }

    @Override
    public abstract Map<String,String> getTypeFieldMapping();

    /**获取虚拟表信息*/
    protected List<Entity> getVirtualEntity(){
        return new ArrayList<>();
    }


    /**
     * 获取自增语句
     * @param property 自增字段信息
     * */
    protected abstract String getAutoIncrementSQL(Property property);

    /**
     * 获取建表语句
     * @param entity 建表实体类
     * */
    protected StringBuilder getCreateTableBuilder(Entity entity){
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
        for(IndexField indexField:entity.indexFieldList){
            if(null==indexField.columns||indexField.columns.isEmpty()){
                logger.warn("[忽略索引]该索引字段信息为空!表:{},索引名称:{}",entity.tableName,indexField.indexName);
                continue;
            }
            switch (indexField.indexType){
                case NORMAL:{}break;
                case UNIQUE:{builder.append("unique");}break;
                case FULLTEXT:{builder.append("fulltext");}break;
            }
            builder.append(" index " + quickDAOConfig.database.escape(indexField.indexName) + " (");
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
        return builder;
    }

    /**
     * 提取索引信息
     * */
    protected abstract void getIndex(Entity entity) throws SQLException;

    /**
     * 提取表字段信息
     * */
    protected abstract void getEntityPropertyList(Entity entity) throws SQLException;

    /**
     * 从数据库提取表信息
     * */
    protected abstract List<Entity> getEntityList() throws SQLException;
}
