package cn.schoolwow.quickdao.builder.ddl;

import cn.schoolwow.quickdao.annotation.IdStrategy;
import cn.schoolwow.quickdao.builder.AbstractSQLBuilder;
import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.IndexType;
import cn.schoolwow.quickdao.domain.Property;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractDDLBuilder extends AbstractSQLBuilder implements DDLBuilder {
    protected Logger logger = LoggerFactory.getLogger(DDLBuilder.class);

    public AbstractDDLBuilder(QuickDAOConfig quickDAOConfig) {
        super(quickDAOConfig);
    }

    public String getDatabaseName() throws SQLException{
        return null;
    }

    public abstract List<Entity> getDatabaseEntity() throws SQLException;

    public abstract boolean hasTableExists(Entity entity) throws SQLException;

    @Override
    public void createTable(Entity entity) throws SQLException {
        StringBuilder createTableBuilder = getCreateTableBuilder(entity);
        MDC.put("name","生成新表");
        MDC.put("sql",createTableBuilder.toString());
        connection.prepareStatement(MDC.get("sql")).executeUpdate();
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
        if (null!=property.check&&!property.check.isEmpty()) {
            createPropertyBuilder.append(" check " + property.check);
        }
        if (null != property.comment) {
            createPropertyBuilder.append(" "+quickDAOConfig.database.comment(property.comment));
        }
        if (null != property.after) {
            createPropertyBuilder.append(" after "+quickDAOConfig.database.escape(property.after));
        }
        createPropertyBuilder.append(";");

        MDC.put("name","添加新列");
        MDC.put("sql",createPropertyBuilder.toString());
        connection.prepareStatement(MDC.get("sql")).executeUpdate();
    }

    @Override
    public void alterColumn(Property property) throws SQLException{
        StringBuilder builder = new StringBuilder("alter table " + quickDAOConfig.database.escape(property.entity.tableName));
        builder.append(" alter column "+quickDAOConfig.database.escape(property.column)+" "+property.columnType);

        MDC.put("name","修改数据类型");
        MDC.put("sql",builder.toString());
        connection.prepareStatement(MDC.get("sql")).executeUpdate();
    }

    @Override
    public void dropColumn(Property property) throws SQLException{
        StringBuilder builder = new StringBuilder("alter table ");
        if(null!=quickDAOConfig.databaseName){
            builder.append(quickDAOConfig.database.escape(quickDAOConfig.databaseName)+".");
        }
        builder.append(quickDAOConfig.database.escape(property.entity.tableName));
        builder.append(" drop column "+quickDAOConfig.database.escape(property.column)+";");

        MDC.put("name","删除列");
        MDC.put("sql",builder.toString());
        connection.prepareStatement(MDC.get("sql")).executeUpdate();
    }

    @Override
    public void dropTable(String tableName) throws SQLException {
        String sql = "drop table "+quickDAOConfig.database.escape(tableName);
        MDC.put("name","删除表");
        MDC.put("sql",sql);
        connection.prepareStatement(MDC.get("sql")).executeUpdate();
    }

    @Override
    public void rebuild(Entity entity) throws SQLException {
        if(hasTableExists(entity)){
            dropTable(entity.tableName);
        }
        createTable(entity);
        createIndex(entity, IndexType.Index);
        createIndex(entity, IndexType.Unique);
    }

    @Override
    public void createIndex(Entity entity, IndexType indexType) throws SQLException {
        if(null==entity||null==indexType){
            return;
        }
        String indexName = entity.tableName+"_"+indexType.name();
        switch (indexType){
            case Index:{
                if (null == entity.indexProperties || entity.indexProperties.size() == 0) {
                    return;
                }
                StringBuilder indexBuilder = new StringBuilder("create index " + quickDAOConfig.database.escape(indexName)+" on " + entity.escapeTableName + " (");
                for (Property property : entity.indexProperties) {
                    indexBuilder.append(quickDAOConfig.database.escape(property.column)+",");
                }
                indexBuilder.deleteCharAt(indexBuilder.length() - 1);
                indexBuilder.append(");");

                MDC.put("name","添加索引");
                MDC.put("sql",indexBuilder.toString());
                connection.prepareStatement(MDC.get("sql")).executeUpdate();
            }break;
            case Unique:{
                if (null == entity.uniqueKeyProperties || entity.uniqueKeyProperties.size() == 0) {
                    return;
                }
                StringBuilder indexUniqueBuilder = new StringBuilder("create unique index " + quickDAOConfig.database.escape(indexName)+" on " + entity.escapeTableName + " (");
                for (Property property : entity.uniqueKeyProperties) {
                    indexUniqueBuilder.append(quickDAOConfig.database.escape(property.column)+",");
                }
                indexUniqueBuilder.deleteCharAt(indexUniqueBuilder.length() - 1);
                indexUniqueBuilder.append(");");

                MDC.put("name","添加唯一性约束");
                MDC.put("sql",indexUniqueBuilder.toString());
                connection.prepareStatement(MDC.get("sql")).executeUpdate();
            }break;
        }
    }

    @Override
    public void dropIndex(Entity entity, IndexType indexType) throws SQLException{
        String indexName = entity.tableName+"_"+indexType.name();
        String dropIndexSQL = "drop index "+quickDAOConfig.database.escape(indexName);
        MDC.put("name","删除索引");
        MDC.put("sql",dropIndexSQL);
        connection.prepareStatement(MDC.get("sql")).executeUpdate();
    }

    @Override
    public void createForeignKey(Property property) throws SQLException{
        String operation = property.foreignKey.foreignKeyOption().getOperation();
        String reference = quickDAOConfig.database.escape(quickDAOConfig.getEntityByClassName(property.foreignKey.table().getName()).tableName) + "(" + quickDAOConfig.database.escape(property.foreignKey.field()) + ") ON DELETE " + operation + " ON UPDATE " + operation;
        String foreignKeyName = "FK_" + property.entity.tableName + "_" + property.foreignKey.field() + "_" + quickDAOConfig.getEntityByClassName(property.foreignKey.table().getName()).tableName + "_" + property.name;
        if (hasConstraintExists(property.entity.tableName,foreignKeyName)) {
            return;
        }
        String foreignKeySQL = "alter table " + quickDAOConfig.database.escape(property.entity.tableName) + " add constraint " + quickDAOConfig.database.escape(foreignKeyName) + " foreign key(" + quickDAOConfig.database.escape(property.column) + ") references " + reference;
        MDC.put("name","生成外键约束");
        MDC.put("sql",foreignKeySQL);
        connection.prepareStatement(MDC.get("sql")).executeUpdate();
    }

    @Override
    public void automaticCreateTableAndField() throws SQLException {
        //确定需要新增的表和更新的表
        Collection<Entity> entityList = quickDAOConfig.entityMap.values();
        List<Entity> newEntityList = new ArrayList<>();
        List<Entity> updateEntityList = new ArrayList<>();
        for (Entity entity : entityList) {
            for(Property property:entity.properties){
                if(null!=property.check&&!property.check.isEmpty()){
                    property.check = property.check.replace("#{"+property.name+"}",quickDAOConfig.database.escape(property.column));
                    if(!property.check.contains("(")){
                        property.check = "("+property.check+")";
                    }
                }
            }
            for (Entity dbEntity : quickDAOConfig.dbEntityList) {
                if (entity.tableName.toLowerCase().equals(dbEntity.tableName.toLowerCase())) {
                    updateEntityList.add(entity);
                    break;
                }
            }
            if(!updateEntityList.contains(entity)){
                newEntityList.add(entity);
            }
        }
        List<Entity> finalNewEntityList = new ArrayList<>(newEntityList.size());
        for(Entity entity:newEntityList){
            changeNewEntityCreateOrder(entity,finalNewEntityList);
        }
        //自动建表
        if (quickDAOConfig.autoCreateTable) {
            for(Entity entity: newEntityList){
                createTable(entity);
                createIndex(entity, IndexType.Index);
                createIndex(entity, IndexType.Unique);
            }
        }
        //自动新增字段
        if(quickDAOConfig.autoCreateProperty){
            //更新表
            for(Entity entity : updateEntityList){
                for (Entity dbEntity : quickDAOConfig.dbEntityList) {
                    if (entity.tableName.equals(dbEntity.tableName)) {
                        compareEntityDatabase(entity,dbEntity);
                        break;
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
                property.entity = dbEntity;
                property.singleTypeFieldMapping = quickDAOConfig.database.typeFieldMapping.getSingleTypeFieldMapping(property.columnType);
                property.className = property.singleTypeFieldMapping.clazzList[0].getName();
            }
        }
        quickDAOConfig.dbEntityList = dbEntityList;
    }

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
     * 判断索引是否存在
     * @param entity 表信息
     * @param indexType 索引类型
     * */
    protected abstract boolean hasIndexExists(Entity entity, IndexType indexType) throws SQLException;

    /**
     * 获取建表语句
     * @param entity 建表实体类
     * */
    protected StringBuilder getCreateTableBuilder(Entity entity){
        StringBuilder createTableBuilder = new StringBuilder("create table " + entity.escapeTableName + "(");
        for (Property property : entity.properties) {
            if(null==property.columnType||property.columnType.isEmpty()){
                continue;
            }
            if(property.id&&property.strategy== IdStrategy.AutoIncrement){
                createTableBuilder.append(getAutoIncrementSQL(property));
            }else{
                createTableBuilder.append(quickDAOConfig.database.escape(property.column) + " " + property.columnType);
                if (property.notNull) {
                    createTableBuilder.append(" not null");
                }
                if (null!=property.defaultValue&&!property.defaultValue.isEmpty()) {
                    createTableBuilder.append(" default " + property.defaultValue);
                }
                if (null != property.comment) {
                    createTableBuilder.append(" "+quickDAOConfig.database.comment(property.comment));
                }
                if (null!=property.check&&!property.check.isEmpty()) {
                    createTableBuilder.append(" check " + property.check);
                }
                if (property.unique&&!property.unionUnique){
                    createTableBuilder.append(" unique ");
                }
            }
            createTableBuilder.append(",");
        }
        if (quickDAOConfig.openForeignKey&&null!=entity.foreignKeyProperties&&entity.foreignKeyProperties.size()>0) {
            for (Property property : entity.foreignKeyProperties) {
                createTableBuilder.append("foreign key(" + quickDAOConfig.database.escape(property.column) + ") references ");
                String operation = property.foreignKey.foreignKeyOption().getOperation();
                createTableBuilder.append(quickDAOConfig.database.escape(quickDAOConfig.getEntityByClassName(property.foreignKey.table().getName()).tableName) + "(" + quickDAOConfig.database.escape(property.foreignKey.field()) + ") ON DELETE " + operation+ " ON UPDATE " + operation);
                createTableBuilder.append(",");
            }
        }
        createTableBuilder.deleteCharAt(createTableBuilder.length() - 1);
        createTableBuilder.append(")");
        if (null != entity.comment) {
            createTableBuilder.append(" "+quickDAOConfig.database.comment(entity.comment));
        }
        return createTableBuilder;
    }

    /**
     * 判断约束是否已经存在
     * @param tableName 表名
     * @param constraintName 约束名称
     * */
    protected boolean hasConstraintExists(String tableName, String constraintName) throws SQLException {
        ResultSet resultSet = connection.prepareStatement("select count(1) from information_schema.KEY_COLUMN_USAGE where constraint_name='" + constraintName + "'").executeQuery();
        boolean result = false;
        if (resultSet.next()) {
            result = resultSet.getInt(1) > 0;
        }
        resultSet.close();
        return result;
    }

    /**
     * 更新字段索引信息
     * @param executeSQL 待执行SQL语句
     * @param propertyList 字段列表
     * */
    protected void updateTableIndex(String executeSQL, List<Property> propertyList) throws SQLException{
        ResultSet resultSet = connection.prepareStatement(executeSQL).executeQuery();
        while (resultSet.next()) {
            //判断是普通索引还是唯一性约束
            String sql = resultSet.getString(1);
            if(null==sql){
                continue;
            }
            sql = sql.toLowerCase();
            String[] columns = sql.substring(sql.indexOf("(")+1,sql.indexOf(")")).toLowerCase().split(",");
            if(sql.contains("create unique index")){
                for(String column:columns){
                    for(Property property:propertyList){
                        if(property.column.equals(column.substring(1,column.length()-1))){
                            property.unique = true;
                            break;
                        }
                    }
                }
            }else if(sql.contains("create index")){
                for(String column:columns){
                    for(Property property:propertyList){
                        if(property.column.equals(column.substring(1,column.length()-1))){
                            property.index = true;
                            break;
                        }
                    }
                }
            }
        }
        resultSet.close();
    }

    /**
     * 对比实体类和数据表并创建新列
     */
    private void compareEntityDatabase(Entity entity, Entity dbEntity) throws SQLException{
        boolean hasIndexProperty = false;
        boolean hasUniqueProperty = false;
        List<Property> foreignKeyPropertyList = new ArrayList<>();
        for (Property entityProperty : entity.properties) {
            boolean columnExist = false;
            for (Property dbEntityProperty : dbEntity.properties) {
                if (dbEntityProperty.column.equals(entityProperty.column)) {
                    columnExist = true;
                    if(entityProperty.id){
                        break;
                    }
                    //判断有无唯一性约束的改变
                    if(dbEntityProperty.unique != entityProperty.unique){
                        hasUniqueProperty = true;
                    }
                    //判断有无索引的改变
                    if(dbEntityProperty.index != entityProperty.index){
                        hasIndexProperty = true;
                    }
                    break;
                }
            }
            if (!columnExist&&null!=entityProperty.columnType&&!entityProperty.columnType.isEmpty()) {
                createProperty(entityProperty);
                if(entityProperty.index){
                    hasIndexProperty = true;
                }
                if(entityProperty.unique){
                    hasUniqueProperty = true;
                }
                if(null!=entityProperty.foreignKey){
                    foreignKeyPropertyList.add(entityProperty);
                }
            }
        }
        //如果新增属性中有索引属性,则重新建立联合索引
        if (hasIndexProperty) {
            if(hasIndexExists(entity, IndexType.Index)){
                dropIndex(entity, IndexType.Index);
            }
            createIndex(entity, IndexType.Index);
        }
        //如果新增属性中有唯一约束,则重新建立联合唯一约束
        if (hasUniqueProperty) {
            if(hasIndexExists(entity, IndexType.Unique)){
                dropIndex(entity, IndexType.Unique);
            }
            createIndex(entity, IndexType.Unique);
        }
        //建立外键
        for(Property property:foreignKeyPropertyList){
            createForeignKey(property);
        }
    }

    /**
     * 根据外键依赖关系调整外键创建顺序
     * @param entity 当前要创建的实体类
     * @param finalNewEntityList 最终实体类顺序
     * */
    private void changeNewEntityCreateOrder(Entity entity, List<Entity> finalNewEntityList){
        if(null!=entity.foreignKeyProperties&&entity.foreignKeyProperties.size()>0){
            for(Property property : entity.foreignKeyProperties){
                changeNewEntityCreateOrder(quickDAOConfig.getEntityByClassName(property.foreignKey.table().getName()),finalNewEntityList);
            }
        }
        finalNewEntityList.add(entity);
    }
}
