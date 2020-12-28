package cn.schoolwow.quickdao.dao;

import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.Interceptor;
import cn.schoolwow.quickdao.domain.Property;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import cn.schoolwow.quickdao.exception.SQLRuntimeException;
import cn.schoolwow.quickdao.transaction.Transaction;
import cn.schoolwow.quickdao.transaction.TransactionInvocationHandler;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AbstractDAOOperation implements DAOOperation{
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
        Entity entity = entityCollection.stream().filter(entity1 -> entity1.tableName.equals(tableName)).findFirst().orElse(null);
        if(null==entity){
            entity = quickDAOConfig.dbEntityList.stream().filter(entity1 -> entity1.tableName.equals(tableName)).findFirst().orElse(null);
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
    public Map<String, String> getFieldMapping() {
        return quickDAOConfig.fieldMapping;
    }

    @Override
    public QuickDAOConfig getQuickDAOConfig() {
        return this.quickDAOConfig;
    }

    @Override
    public void generateEntityFile(String sourcePath) {
        quickDAOConfig.entityHandler.generateEntityFile(sourcePath,null);
    }

    @Override
    public void generateEntityFile(String sourcePath, String[] tableNames) {
        quickDAOConfig.entityHandler.generateEntityFile(sourcePath,tableNames);
    }
}
