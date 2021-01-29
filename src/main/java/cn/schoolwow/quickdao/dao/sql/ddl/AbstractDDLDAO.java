package cn.schoolwow.quickdao.dao.sql.ddl;

import cn.schoolwow.quickdao.builder.ddl.AbstractDDLBuilder;
import cn.schoolwow.quickdao.dao.sql.AbstractSQLDAO;
import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.IndexField;
import cn.schoolwow.quickdao.domain.Property;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import cn.schoolwow.quickdao.exception.SQLRuntimeException;

import java.sql.SQLException;
import java.util.Collection;
import java.util.function.Predicate;

public class AbstractDDLDAO extends AbstractSQLDAO implements DDLDAO {
    private AbstractDDLBuilder ddlBuilder;
    public AbstractDDLDAO(QuickDAOConfig quickDAOConfig) {
        super(quickDAOConfig);
        this.ddlBuilder = quickDAOConfig.database.getDDLBuilderInstance(quickDAOConfig);
        super.sqlBuilder = this.ddlBuilder;
    }

    @Override
    public void create(Class clazz) {
        create(this.quickDAOConfig.getEntityByClassName(clazz.getName()));
    }

    @Override
    public void create(Entity entity) {
        try {
            ddlBuilder.createTable(entity);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void drop(Class clazz) {
        drop(this.quickDAOConfig.getEntityByClassName(clazz.getName()).tableName);
    }

    @Override
    public void drop(String tableName) {
        try {
            ddlBuilder.dropTable(tableName);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void rebuild(Class clazz) {
        try {
            Entity entity = this.quickDAOConfig.getEntityByClassName(clazz.getName());
            ddlBuilder.rebuild(entity);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void rebuild(String tableName) {
        try {
            Collection<Entity> entityList = quickDAOConfig.entityMap.values();
            Predicate<Entity> findByTableName = entity->entity.tableName.equals(tableName);
            Entity entity = entityList.stream().filter(findByTableName).findFirst().orElse(null);
            if(null==entity){
                entity = quickDAOConfig.dbEntityList.stream().filter(findByTableName).findFirst().orElse(null);
            }
            if(null==entity){
                throw new IllegalArgumentException("表不存在!表名:"+tableName);
            }
            ddlBuilder.rebuild(entity);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void createColumn(String tableName, Property property) {
        try {
            Entity entity = new Entity();
            entity.tableName = tableName;
            property.entity = entity;
            if(null!=property.check){
                property.check = property.check.replace("#{"+property.name+"}",quickDAOConfig.database.escape(property.column));
                if(!property.check.isEmpty()&&!property.check.contains("(")){
                    property.check = "("+property.check+")";
                }
            }
            ddlBuilder.createProperty(property);
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
            ddlBuilder.dropColumn(deleteProperty);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
        return deleteProperty;
    }

    @Override
    public boolean hasIndex(String tableName, String indexName) {
        try {
            return ddlBuilder.hasIndexExists(tableName,indexName);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void createIndex(IndexField indexField) {
        try {
            ddlBuilder.createIndex(indexField);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void dropIndex(String tableName, String indexName) {
        try {
            ddlBuilder.dropIndex(tableName,indexName);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void syncEntityList() {
        if(quickDAOConfig.packageNameMap.isEmpty()&&quickDAOConfig.entityClassMap.isEmpty()){
            throw new IllegalArgumentException("请先指定要扫描的实体类包或者实体类!");
        }
        try {
            ddlBuilder.automaticCreateTableAndColumn();
            //删除数据库多余的表和字段
            Collection<Entity> entityList = quickDAOConfig.entityMap.values();
            for(Entity dbEntity:quickDAOConfig.dbEntityList){
                Entity entity = entityList.stream().filter(entity1 -> entity1.tableName.equals(dbEntity.tableName)).findFirst().orElse(null);
                if(null==entity){
                    drop(dbEntity.tableName);
                    continue;
                }
                for(Property dbProperty:dbEntity.properties){
                    Property property = entity.properties.stream().filter(property1 -> property1.column.equals(dbProperty.column)).findFirst().orElse(null);
                    if(null==property){
                        dropColumn(dbEntity.tableName,dbProperty.column);
                    }
                }
            }
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void automaticCreateTableAndColumn(){
        try {
            ddlBuilder.automaticCreateTableAndColumn();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void refreshDbEntityList(){
        try {
            ddlBuilder.refreshDbEntityList();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }
}
