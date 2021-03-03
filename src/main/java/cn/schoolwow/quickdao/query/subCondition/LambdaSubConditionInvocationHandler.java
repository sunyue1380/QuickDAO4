package cn.schoolwow.quickdao.query.subCondition;

import cn.schoolwow.quickdao.util.LambdaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * lamda表达式动态代理对象
 * */
public class LambdaSubConditionInvocationHandler<T,P> implements InvocationHandler {
    private Logger logger = LoggerFactory.getLogger(LambdaSubConditionInvocationHandler.class);
    private SubCondition<T,P> subCondition;

    public LambdaSubConditionInvocationHandler(SubCondition<T,P> subCondition) {
        this.subCondition = subCondition;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if(method.getName().equals("done")){
            return this.subCondition;
        }
        Object result = LambdaUtils.invokeMethod(args,method,this.subCondition);
        if(method.getName().equals("joinTable")){
            SubCondition subCondition = (SubCondition) result;
            return subCondition.lambdaSubCondition();
        }else if(method.getReturnType().getName().equals(LambdaSubCondition.class.getName())){
            return proxy;
        }else{
            return result;
        }
    }
}
