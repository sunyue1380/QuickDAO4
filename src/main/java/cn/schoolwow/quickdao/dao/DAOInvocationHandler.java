package cn.schoolwow.quickdao.dao;

import cn.schoolwow.quickdao.dao.sql.AbstractSQLDAO;
import cn.schoolwow.quickdao.dao.sql.ddl.AbstractDDLDAO;
import cn.schoolwow.quickdao.dao.sql.dml.AbstractDMLDAO;
import cn.schoolwow.quickdao.dao.sql.dql.AbstractDQLDAO;
import cn.schoolwow.quickdao.domain.ConnectionExecutor;
import cn.schoolwow.quickdao.domain.Database;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import cn.schoolwow.quickdao.query.AbstractCompositQuery;
import cn.schoolwow.quickdao.query.CompositQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * DAO接口调用代理模式对象
 */
public class DAOInvocationHandler implements InvocationHandler {
    private Logger logger = LoggerFactory.getLogger(DAOInvocationHandler.class);
    private QuickDAOConfig quickDAOConfig;
    private DAOOperation daoOperation;
    private CompositQuery compositQuery;

    public DAOInvocationHandler(QuickDAOConfig quickDAOConfig) {
        this.quickDAOConfig = quickDAOConfig;
        this.daoOperation = new AbstractDAOOperation(quickDAOConfig);
        this.compositQuery = new AbstractCompositQuery(quickDAOConfig);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if(!quickDAOConfig.initialized&&method.getDeclaringClass().getName().startsWith("cn.schoolwow.quickdao")){
            logger.info("[QuickDAO懒加载初始化]");
            quickDAOConfig.initialized = true;
            quickDAOConfig.dao.automaticCreateTableAndColumn();
        }
        String interfaceName = method.getDeclaringClass().getSimpleName();
        if("CompositQuery".equals(interfaceName)){
            return method.invoke(compositQuery, args);
        }
        AbstractSQLDAO instance = null;
        switch (interfaceName) {
            case "SQLDAO":
            case "DMLDAO": {
                instance = new AbstractDMLDAO(quickDAOConfig);
            }
            break;
            case "DCLDAO": {
                instance = quickDAOConfig.database.getDCLDAOInstance(quickDAOConfig);
            }
            break;
            case "DDLDAO": {
                instance = new AbstractDDLDAO(quickDAOConfig);
            }
            break;
            case "DQLDAO": {
                instance = new AbstractDQLDAO(quickDAOConfig);
            }
            break;
            default:{
                return method.invoke(daoOperation, args);
            }
        }

        Object result = null;
        try {
            if(quickDAOConfig.database.equals(Database.SQLite)){
                quickDAOConfig.sqliteLock.lock();
            }
            instance.sqlBuilder.connectionExecutor = new ConnectionExecutor(quickDAOConfig);
            instance.sqlBuilder.connectionExecutor.connection = quickDAOConfig.dataSource.getConnection();
            result = method.invoke(instance, args);
            if("DDLDAO".equals(interfaceName)&&!method.getName().equals("refreshDbEntityList")){
                if(!"refreshDbEntityList".equals(method.getName())&&!"automaticCreateTableAndColumn".equals(method.getName())){
                    quickDAOConfig.dao.refreshDbEntityList();
                }
            }
            return result;
        } catch (InvocationTargetException e){
            throw e.getTargetException();
        } finally {
            if(null!=instance.sqlBuilder.connectionExecutor.connection){
                instance.sqlBuilder.connectionExecutor.connection.close();
            }
            if(quickDAOConfig.database.equals(Database.SQLite)){
                quickDAOConfig.sqliteLock.unlock();
            }
        }
    }
}
