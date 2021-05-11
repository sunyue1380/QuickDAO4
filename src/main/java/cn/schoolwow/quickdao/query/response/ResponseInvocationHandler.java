package cn.schoolwow.quickdao.query.response;

import cn.schoolwow.quickdao.domain.ConnectionExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ResponseInvocationHandler implements InvocationHandler {
    private Logger logger = LoggerFactory.getLogger(ResponseInvocationHandler.class);
    private AbstractResponse abstractResponse;

    public ResponseInvocationHandler(AbstractResponse abstractResponse) {
        this.abstractResponse = abstractResponse;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            if(!abstractResponse.query.transaction){
                abstractResponse.query.dqlBuilder.connectionExecutor = new ConnectionExecutor(abstractResponse.query.quickDAOConfig);
                abstractResponse.query.dqlBuilder.connectionExecutor.connection = abstractResponse.query.quickDAOConfig.dataSource.getConnection();
            }
            Object result = method.invoke(abstractResponse, args);
            return result;
        } catch (InvocationTargetException e){
            throw e.getTargetException();
        } finally {
            abstractResponse.query.parameterIndex = 1;
            if(!abstractResponse.query.transaction&&null!=abstractResponse.query.dqlBuilder.connectionExecutor){
                abstractResponse.query.dqlBuilder.connectionExecutor.connection.close();
            }
        }
    }
}
