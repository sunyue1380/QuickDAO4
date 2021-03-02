package cn.schoolwow.quickdao.query.condition;

import cn.schoolwow.quickdao.query.subCondition.SubCondition;
import cn.schoolwow.quickdao.util.LambdaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * lamda表达式动态代理对象
 * */
public class LambdaConditionInvocationHandler<T> implements InvocationHandler {
    private Logger logger = LoggerFactory.getLogger(LambdaConditionInvocationHandler.class);
    private Condition<T> condition;

    public LambdaConditionInvocationHandler(Condition condition) {
        this.condition = condition;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if(method.getName().equals("done")){
            return this.condition;
        }
        Object result = LambdaUtils.invokeMethod(args,method,this.condition);
        switch (method.getReturnType().getSimpleName()){
            case "LambdaCondition":{
                return proxy;
            }
            case "LambdaSubCondition":{
                SubCondition subCondition = (SubCondition) result;
                return subCondition.lambdaSubCondition();
            }
            default:{
                return result;
            }
        }
    }
}
