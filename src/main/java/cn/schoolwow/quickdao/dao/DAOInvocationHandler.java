package cn.schoolwow.quickdao.dao;

import cn.schoolwow.quickdao.dao.sql.AbstractSQLDAO;
import cn.schoolwow.quickdao.dao.sql.ddl.AbstractDDLDAO;
import cn.schoolwow.quickdao.dao.sql.ddl.DDLDAO;
import cn.schoolwow.quickdao.dao.sql.dml.AbstractDMLDAO;
import cn.schoolwow.quickdao.dao.sql.dql.AbstractDQLDAO;
import cn.schoolwow.quickdao.domain.Database;
import cn.schoolwow.quickdao.domain.Interceptor;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import cn.schoolwow.quickdao.query.AbstractCompositQuery;
import cn.schoolwow.quickdao.query.CompositQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

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
            case "DDLDAO": {
                instance = new AbstractDDLDAO(quickDAOConfig);
            }
            break;
            case "DQLDAO": {
                instance = new AbstractDQLDAO(quickDAOConfig);
            }
            break;
        }
        if(null==instance){
            return method.invoke(daoOperation, args);
        }else{
            Object result = null;
            try {
                if(quickDAOConfig.database.equals(Database.SQLite)){
                    quickDAOConfig.sqliteLock.lock();
                }
                instance.sqlBuilder.connection = quickDAOConfig.dataSource.getConnection();
                long startTime = System.currentTimeMillis();
                result = method.invoke(instance, args);
                long endTime = System.currentTimeMillis();
                if("DDLDAO".equals(interfaceName)){
                    DDLDAO ddldao = (DDLDAO) instance;
                    ddldao.refreshDbEntityList();
                }
                if(quickDAOConfig.database.equals(Database.SQLite)){
                    quickDAOConfig.sqliteLock.unlock();
                }
                if (null != MDC.get("name")) {
                    if (null == MDC.get("count")) {
                        logger.debug("[{}]耗时:{}ms,执行SQL:{}", MDC.get("name"), endTime - startTime, MDC.get("sql"));
                    } else {
                        logger.debug("[{}]行数:{},耗时:{}ms,执行SQL:{}", MDC.get("name"), MDC.get("count"), endTime - startTime, MDC.get("sql"));
                    }
                }
                for(Interceptor interceptor:quickDAOConfig.interceptorList){
                    interceptor.afterExecuteConnection(MDC.get("name"),MDC.get("sql"));
                }
                return result;
            }catch (InvocationTargetException e){
                if (null != MDC.get("name")) {
                    logger.warn("[{}]原始SQL:{}", MDC.get("name"), MDC.get("sql"));
                }
                for(Interceptor interceptor:quickDAOConfig.interceptorList){
                    interceptor.afterExecuteConnection(MDC.get("name"),MDC.get("sql"));
                }
                throw e.getTargetException();
            }finally {
                if(null!=instance.sqlBuilder.connection){
                    instance.sqlBuilder.connection.close();
                }
                MDC.clear();
            }
        }
    }
}
