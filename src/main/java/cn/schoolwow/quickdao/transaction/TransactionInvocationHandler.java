package cn.schoolwow.quickdao.transaction;

import cn.schoolwow.quickdao.dao.sql.ddl.AbstractDDLDAO;
import cn.schoolwow.quickdao.dao.sql.dml.AbstractDMLDAO;
import cn.schoolwow.quickdao.domain.ConnectionExecutor;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import cn.schoolwow.quickdao.query.AbstractCompositQuery;
import cn.schoolwow.quickdao.query.CompositQuery;
import cn.schoolwow.quickdao.query.condition.AbstractCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;

/**
 * 事务操作代理模式对象
 */
public class TransactionInvocationHandler implements InvocationHandler {
    private Logger logger = LoggerFactory.getLogger(TransactionInvocationHandler.class);
    private QuickDAOConfig quickDAOConfig;
    private TransactionOperation transactionOperation;
    private CompositQuery compositQuery;
    private AbstractDDLDAO ddldao;
    private AbstractDMLDAO dmldao;
    private Connection connection;

    public TransactionInvocationHandler(QuickDAOConfig quickDAOConfig, Connection connection) {
        this.quickDAOConfig = quickDAOConfig;
        this.transactionOperation = new AbstractTransactionOperation(connection);
        this.compositQuery = new AbstractCompositQuery(quickDAOConfig);
        this.ddldao = new AbstractDDLDAO(quickDAOConfig);
        this.ddldao.sqlBuilder.connectionExecutor = new ConnectionExecutor(quickDAOConfig);
        this.ddldao.sqlBuilder.connectionExecutor.connection = connection;
        this.dmldao = new AbstractDMLDAO(quickDAOConfig);
        this.dmldao.sqlBuilder.connectionExecutor = new ConnectionExecutor(quickDAOConfig);
        this.dmldao.sqlBuilder.connectionExecutor.connection = connection;
        this.connection = connection;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String interfaceName = method.getDeclaringClass().getSimpleName();
        Object result = null;
        try {
            switch (interfaceName) {
                case "CompositQuery": {
                    result = method.invoke(compositQuery, args);
                    AbstractCondition abstractCondition = (AbstractCondition) result;
                    abstractCondition.query.transaction = true;
                    abstractCondition.query.dqlBuilder.connectionExecutor = new ConnectionExecutor(quickDAOConfig);
                    abstractCondition.query.dqlBuilder.connectionExecutor.connection = connection;
                }
                break;
                case "DDLDAO": {
                    result = method.invoke(ddldao, args);
                }
                break;
                case "DMLDAO": {
                    result = method.invoke(dmldao, args);
                }
                break;
                default: {
                    result = method.invoke(transactionOperation, args);
                }
                break;
            }
            return result;
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }
}
