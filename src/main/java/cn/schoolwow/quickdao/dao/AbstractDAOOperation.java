package cn.schoolwow.quickdao.dao;

import cn.schoolwow.quickdao.domain.*;
import cn.schoolwow.quickdao.exception.SQLRuntimeException;
import cn.schoolwow.quickdao.transaction.Transaction;
import cn.schoolwow.quickdao.transaction.TransactionInvocationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AbstractDAOOperation implements DAOOperation{
    Logger logger = LoggerFactory.getLogger(AbstractDAOOperation.class);
    private QuickDAOConfig quickDAOConfig;

    public AbstractDAOOperation(QuickDAOConfig quickDAOConfig) {
        this.quickDAOConfig = quickDAOConfig;
    }

    @Override
    public void interceptor(Interceptor interceptor) {
        quickDAOConfig.interceptorList.add(interceptor);
    }

    @Override
    public Transaction startTransaction() {
        try {
            Connection connection = quickDAOConfig.dataSource.getConnection();
            connection.setAutoCommit(false);
            TransactionInvocationHandler transactionInvocationHandler = new TransactionInvocationHandler(quickDAOConfig,connection);
            Transaction transactionProxy = (Transaction) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{Transaction.class},transactionInvocationHandler);
            return transactionProxy;
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public boolean hasTable(final String tableName) {
        Collection<Entity> entityCollection = quickDAOConfig.entityMap.values();
        Entity entity = entityCollection.stream().filter(entity1 -> entity1.tableName.equalsIgnoreCase(tableName)).findFirst().orElse(null);
        if(null==entity){
            entity = quickDAOConfig.dbEntityList.stream().filter(entity1 -> entity1.tableName.equalsIgnoreCase(tableName)).findFirst().orElse(null);
        }
        return null!=entity;
    }

    @Override
    public boolean hasColumn(String tableName, String column) {
        Entity entity = quickDAOConfig.dbEntityList.stream().filter(entity1 -> entity1.tableName.equals(tableName)).findFirst().orElse(null);
        if(null==entity){
            return false;
        }
        return entity.properties.stream().anyMatch(property -> property.column.equals(column));
    }

    @Override
    public DataSource getDataSource() {
        return quickDAOConfig.dataSource;
    }

    @Override
    public Map<String, Entity> getEntityMap() {
        return quickDAOConfig.entityMap;
    }

    @Override
    public List<Entity> getDbEntityList() {
        return quickDAOConfig.dbEntityList;
    }

    @Override
    public Entity getEntity(Class clazz) {
        return quickDAOConfig.entityMap.values().stream().filter(entity -> entity.clazz.getName().equals(clazz.getName())).findFirst().orElse(null);
    }

    @Override
    public Entity getDbEntity(String tableName) {
        return quickDAOConfig.dbEntityList.stream().filter(entity -> entity.tableName.equals(tableName)).findFirst().orElse(null);
    }

    @Override
    public Property getProperty(String tableName, String column) {
        Entity entity = quickDAOConfig.dbEntityList.stream().filter(entity1 -> entity1.tableName.equals(tableName)).findFirst().orElse(null);
        if(null==entity){
            throw new IllegalArgumentException("表不存在!表名:"+tableName);
        }
        Property property = entity.properties.stream().filter(property1 -> property1.column.equals(column)).findFirst().orElse(null);
        if(null==property){
            throw new IllegalArgumentException("列不存在!表名:"+tableName+",字段名:"+column);
        }
        return property;
    }

    @Override
    public List<Property> getPropertyList(String tableName) {
        Entity entity = quickDAOConfig.dbEntityList.stream().filter(entity1 -> entity1.tableName.equals(tableName)).findFirst().orElse(null);
        if(null==entity){
            throw new IllegalArgumentException("表不存在!表名:"+tableName);
        }
        return entity.properties;
    }

    @Override
    public QuickDAOConfig getQuickDAOConfig() {
        return this.quickDAOConfig;
    }

    @Override
    public void generateEntityFile(GenerateEntityFileOption generateEntityFileOption) {
        quickDAOConfig.entityHandler.generateEntityFile(generateEntityFileOption);
    }

    @Override
    public void migrateFrom(DAO dao) {
        migrate(dao,quickDAOConfig.dao,null);
    }

    @Override
    public void migrateFrom(DAO dao, Class... entityClassList) {
        migrate(dao,quickDAOConfig.dao,entityClassList);
    }

    @Override
    public void migrateTo(DAO target) {
        migrate(quickDAOConfig.dao,target,null);
    }

    @Override
    public void migrateTo(DAO target, Class... entityClassList) {
        migrate(quickDAOConfig.dao,target,entityClassList);
    }

    /**
     * 迁移数据源
     * @param source 数据源
     * @param target 目标源
     * @param entityClassList 要迁移的表的表名
     * */
    private void migrate(DAO source , DAO target, Class[] entityClassList) {
        Collection<Entity> entityList = null;
        if(null==entityClassList||entityClassList.length==0){
            entityList = source.getEntityMap().values();
        }else{
            entityList = source.getEntityMap().values().stream().filter((entity)->{
                for(Class entityClass : entityClassList){
                    if(entityClass.getName().equalsIgnoreCase(entity.clazz.getName())){
                        return true;
                    }
                }
                return false;
            }).collect(Collectors.toList());
        }
        target.enableForeignConstraintCheck(false);
        //禁用外键约束
        Transaction transaction = target.startTransaction();
        try{
            //获取需要迁移的表
            final Map<String,String> typeFieldMapping = target.getTypeFieldMapping();
            final Map<String,Entity> targetEntityMap = target.getEntityMap();
            Database database = target.getQuickDAOConfig().database;
            for(Entity entity:entityList){
                long count = source.query(entity.tableName).execute().count();
                if(count==0){
                    logger.debug("[数据迁移]{}表不存在数据,跳过此表的迁移",entity.tableName);
                    continue;
                }

                if(target.hasTable(entity.tableName)){
                    logger.debug("[数据迁移]删除表:{}",entity.tableName);
                    transaction.dropTable(entity.tableName);
                }
                logger.debug("[数据迁移]创建表:{}",entity.tableName);
                Entity cloneEntity = entity.clone();

                cloneEntity.escapeTableName = database.escape(cloneEntity.tableName);
                for(Property property : cloneEntity.properties){
                    if(typeFieldMapping.containsKey(property.className)){
                        //处理类型转换
                        {
                            String columnType = property.columnType;
                            String length = null;
                            if(columnType.contains("(")){
                                length = columnType.substring(columnType.indexOf("(") + 1,columnType.lastIndexOf(")"));
                            }
                            columnType = typeFieldMapping.get(property.className);
                            //如果数据类型存在括号
                            if(null!=length){
                                if(columnType.contains("(")){
                                    columnType = columnType.substring(0,columnType.indexOf("(")) + "("+length+")";
                                }else{
                                    columnType = columnType + "("+length+")";
                                }
                            }
                            property.columnType= columnType;
                        }
                        if(null!=property.check){
                            property.escapeCheck = property.check.replace(property.column, database.escape(property.column));
                        }
                        property.createdAt = false;
                        property.updateAt = false;
                    }
                }
                targetEntityMap.put(cloneEntity.clazz.getName(),cloneEntity);
                transaction.create(cloneEntity);
                //传输数据
                long totalPage = count/1000+1;
                int effect = 0;
                for(int i=1;i<=totalPage;i++){
                    List list = source.query(entity.clazz)
                            .page(i,1000)
                            .execute()
                            .getList();
                    effect += transaction.insertBatch(list);
                }
                logger.info("[数据迁移]{}表数据迁移完毕,迁移源数据个数:{},迁移成功数据个数:{}", entity.tableName, count, effect);
            }
            transaction.commit();
        }finally {
            transaction.endTransaction();
            target.enableForeignConstraintCheck(true);
        }
    }
}