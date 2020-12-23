package cn.schoolwow.quickdao.query.response;

import cn.schoolwow.quickdao.domain.Interceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

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
                abstractResponse.query.dqlBuilder.connection = abstractResponse.query.quickDAOConfig.dataSource.getConnection();
            }
            long startTime = System.currentTimeMillis();
            Object result = method.invoke(abstractResponse, args);
            long endTime = System.currentTimeMillis();
            if(null!=MDC.get("name")){
                if(null==MDC.get("count")){
                    logger.debug("[{}]耗时:{}ms,执行SQL:{}",MDC.get("name"), endTime-startTime,MDC.get("sql"));
                }else{
                    logger.debug("[{}]行数:{},耗时:{}ms,执行SQL:{}",MDC.get("name"), MDC.get("count"),endTime-startTime,MDC.get("sql"));
                }
            }
            for(Interceptor interceptor:abstractResponse.query.quickDAOConfig.interceptorList){
                interceptor.afterExecuteConnection(MDC.get("name"),MDC.get("sql"));
            }
            return result;
        }catch (InvocationTargetException e){
            if(null!=MDC.get("name")){
                logger.warn("[{}]原始SQL:{}",MDC.get("name"),MDC.get("sql"));
            }
            for(Interceptor interceptor:abstractResponse.query.quickDAOConfig.interceptorList){
                interceptor.afterExecuteConnection(MDC.get("name"),MDC.get("sql"));
            }
            throw e.getTargetException();
        }finally {
            abstractResponse.query.parameterIndex = 1;
            if(!abstractResponse.query.transaction){
                abstractResponse.query.dqlBuilder.connection.close();
            }
            MDC.clear();
        }
    }
}
