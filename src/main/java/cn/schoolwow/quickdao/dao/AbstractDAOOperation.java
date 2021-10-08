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
import java.util.function.Consumer;

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
    public void startTransaction(Consumer<Transaction> transactionConsumer) {
        Transaction transaction = startTransaction();
        try{
            transactionConsumer.accept(transaction);
            transaction.commit();
        } catch (SQLRuntimeException e){
            transaction.rollback();
            throw e;
        } finally {
            transaction.endTransaction();
        }
    }

    @Override
    public boolean hasTable(Class entityClass) {
        Entity entity = quickDAOConfig.getEntityByClassName(entityClass.getName());
        return hasTable(entity.tableName);
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
}