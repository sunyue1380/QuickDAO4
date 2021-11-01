package cn.schoolwow.quickdao.dao.sql.ddl;

import cn.schoolwow.quickdao.builder.ddl.AbstractDDLBuilder;
import cn.schoolwow.quickdao.dao.sql.AbstractSQLDAO;
import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.IndexField;
import cn.schoolwow.quickdao.domain.Property;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import cn.schoolwow.quickdao.exception.SQLRuntimeException;
import com.alibaba.fastjson.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class AbstractDDLDAO extends AbstractSQLDAO implements DDLDAO {
    private AbstractDDLBuilder ddlBuilder;
    public AbstractDDLDAO(QuickDAOConfig quickDAOConfig) {
        super(quickDAOConfig);
        this.ddlBuilder = quickDAOConfig.database.getDDLBuilderInstance(quickDAOConfig);
        super.sqlBuilder = this.ddlBuilder;
    }

    @Override
    public boolean hasTableExists(Class clazz) {
        Entity entity = this.quickDAOConfig.getEntityByClassName(clazz.getName());
        boolean result = false;
        try {
            String hasTableExistsSQL = ddlBuilder.hasTableExists(entity);
            ResultSet resultSet = ddlBuilder.connectionExecutor.executeQuery("判断表是否存在",hasTableExistsSQL);
            if(resultSet.next()){
                result = true;
            }
            resultSet.close();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
        return result;
    }

    @Override
    public void create(Class clazz) {
        create(this.quickDAOConfig.getEntityByClassName(clazz.getName()));
    }

    @Override
    public void create(Entity entity) {
        try {
            String createTableSQL = ddlBuilder.createTable(entity);
            ddlBuilder.connectionExecutor.executeUpdate("生成新表",createTableSQL);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void dropTable(Class clazz) {
        dropTable(this.quickDAOConfig.getEntityByClassName(clazz.getName()).tableName);
    }

    @Override
    public void dropTable(String tableName) {
        try {
            String dropTableSQL = ddlBuilder.dropTable(tableName);
            ddlBuilder.connectionExecutor.executeUpdate("删除表",dropTableSQL);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void rebuild(Class clazz) {
        Entity entity = this.quickDAOConfig.getEntityByClassName(clazz.getName());
        if(hasTableExists(clazz)){
            dropTable(clazz);
        }
        create(clazz);
    }

    @Override
    public void rebuild(String tableName) {
        Entity entity = quickDAOConfig.dao.getDbEntity(tableName);
        if(null==entity){
            throw new IllegalArgumentException("表不存在!表名:" + tableName);
        }
        dropTable(tableName);
        create(entity);
    }

    @Override
    public void createColumn(String tableName, Property property) {
        try {
            Entity entity = new Entity();
            entity.tableName = tableName;
            property.entity = entity;
            if(null!=property.check){
                if(!property.check.isEmpty()&&!property.check.contains("(")){
                    property.check = "(" + property.check + ")";
                }
                property.check = property.check.replace("#{" + property.name + "}", property.column);
                property.escapeCheck = property.check.replace(property.column, quickDAOConfig.database.escape(property.column));
            }
            String createPropertySQL = ddlBuilder.createProperty(property);
            ddlBuilder.connectionExecutor.executeUpdate("新增列",createPropertySQL);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void alterColumn(Property property) {
        try {
            String alterColumnSQL = ddlBuilder.alterColumn(property);
            ddlBuilder.connectionExecutor.executeUpdate("修改列",alterColumnSQL);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public Property dropColumn(String tableName, String column) {
        Entity dbEntity = quickDAOConfig.dbEntityList.stream().filter(entity->entity.tableName.equals(tableName)).findFirst().orElse(null);
        if(null==dbEntity){
            throw new IllegalArgumentException("表不存在!表名:"+tableName);
        }
        Property deleteProperty = dbEntity.properties.stream().filter(property -> property.column.equals(column)).findFirst().orElse(null);
        if(null==deleteProperty){
            throw new IllegalArgumentException("列不存在!表名:"+tableName+",列名:"+column);
        }
        try {
            String dropColumnSQL = ddlBuilder.dropColumn(deleteProperty);
            ddlBuilder.connectionExecutor.executeUpdate("删除列",dropColumnSQL);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
        return deleteProperty;
    }

    @Override
    public boolean hasIndex(String tableName, String indexName) {
        try {
            String hasIndexExistsSQL = ddlBuilder.hasIndexExists(tableName,indexName);
            ResultSet resultSet = ddlBuilder.connectionExecutor.executeQuery("判断索引是否存在",hasIndexExistsSQL);
            boolean result = false;
            if (resultSet.next()) {
                result = true;
            }
            resultSet.close();
            return result;
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public boolean hasConstraintExists(String tableName, String indexName) {
        try {
            String hasConstraintExistsSQL =  ddlBuilder.hasConstraintExists(tableName,indexName);
            ResultSet resultSet = ddlBuilder.connectionExecutor.executeQuery("判断约束是否存在",hasConstraintExistsSQL);
            boolean result = false;
            if (resultSet.next()) {
                result = true;
            }
            resultSet.close();
            return result;
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void createIndex(IndexField indexField) {
        try {
            String createIndexSQL = ddlBuilder.createIndex(indexField);
            ddlBuilder.connectionExecutor.executeUpdate("创建索引",createIndexSQL);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void dropIndex(String tableName, String indexName) {
        try {
            String dropIndexSQL = ddlBuilder.dropIndex(tableName,indexName);
            ddlBuilder.connectionExecutor.executeUpdate("删除索引",dropIndexSQL);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void createForeignKey(Property property) {
        try {
            String createForeignKeySQL = ddlBuilder.createForeignKey(property);
            String foreignKeyName = "FK_" + property.entity.tableName + "_" + property.foreignKey.field() + "_" + quickDAOConfig.getEntityByClassName(property.foreignKey.table().getName()).tableName + "_" + property.name;
            if(hasConstraintExists(property.entity.tableName,foreignKeyName)){
                logger.warn("[外键约束已存在]表名:{},外键约束名:{}",property.entity.tableName,foreignKeyName);
                return;
            }
            ddlBuilder.connectionExecutor.executeUpdate("创建外键",createForeignKeySQL);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void enableForeignConstraintCheck(boolean enable) {
        try {
            ddlBuilder.enableForeignConstraintCheck(enable);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public Map<String, String> getTypeFieldMapping() {
        return ddlBuilder.getTypeFieldMapping();
    }

    @Override
    public void syncEntityList() {
        if(quickDAOConfig.packageNameMap.isEmpty()&&quickDAOConfig.entityClassMap.isEmpty()){
            throw new IllegalArgumentException("请先指定要扫描的实体类包或者实体类!");
        }
        automaticCreateTableAndColumn();
        //删除数据库多余的表和字段
        Collection<Entity> entityList = quickDAOConfig.entityMap.values();
        for(Entity dbEntity:quickDAOConfig.dbEntityList){
            Entity entity = entityList.stream().filter(entity1 -> entity1.tableName.equals(dbEntity.tableName)).findFirst().orElse(null);
            if(null==entity){
                dropTable(dbEntity.tableName);
                continue;
            }
            for(Property dbProperty:dbEntity.properties){
                Property property = entity.properties.stream().filter(property1 -> property1.column.equals(dbProperty.column)).findFirst().orElse(null);
                if(null==property){
                    dropColumn(dbEntity.tableName,dbProperty.column);
                }
            }
        }
    }

    @Override
    public void automaticCreateTableAndColumn(){
        refreshDbEntityList();
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
                create(entity);
            }
        }
        //自动新增字段和索引
        if(quickDAOConfig.autoCreateProperty){
            for(Map.Entry<Entity,Entity> entry:updateEntityMap.entrySet()){
                List<Property> propertyList = entry.getKey().properties;
                for(Property property:propertyList){
                    if(entry.getValue().properties.stream().noneMatch(property1 -> property1.column.equals(property.column))){
                        createColumn(property.entity.tableName,property);
                    }
                    if(null!=property.foreignKey&&quickDAOConfig.openForeignKey){
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
            quickDAOConfig.visualTableList = ddlBuilder.getVirtualEntity();
        }
    }

    @Override
    public void refreshDbEntityList(){
        try {
            ddlBuilder.getDatabaseName();
            List<Entity> dbEntityList = ddlBuilder.getDatabaseEntity();
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
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }
}