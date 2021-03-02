package cn.schoolwow.quickdao.util;

import cn.schoolwow.quickdao.domain.SFunction;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Locale;

/**
 * lambda表达式工具类
 * */
public class LambdaUtils {
    /**
     * 转换参数类型并调用实例方法
     * @param args 方法参数
     * @param method 调用方法
     * @param instance 实例
     * */
    public static Object invokeMethod(Object[] args, Method method, Object instance) throws Exception {
        //转换lambda参数
        Class[] parameterTypes = method.getParameterTypes();
        for(int i=0;i<parameterTypes.length;i++){
            Class parameterType = parameterTypes[i];
            if(parameterType.isArray()&&parameterType.getComponentType().getName().equals(SFunction.class.getName())){
                SFunction[] sFunctions = (SFunction[]) args[i];
                String[] convertParameterValues = new String[sFunctions.length];
                for(int j=0;j<sFunctions.length;j++){
                    convertParameterValues[j] = resolveLambdaProperty(sFunctions[j]);
                }
                args[i] = convertParameterValues;
                parameterTypes[i] = String[].class;
            }else if(parameterType.getName().equals(SFunction.class.getName())){
                args[i] = resolveLambdaProperty((SFunction) args[i]);
                parameterTypes[i] = String.class;
            }
        }
        Method invokeMethod = instance.getClass().getMethod(method.getName(),parameterTypes);
        Object result = invokeMethod.invoke(instance,args);
        return result;
    }

    /**
     * 将Lambda表达式转化为属性名称
     * @param sFunction lambda表达式
     */
    public static String resolveLambdaProperty(SFunction sFunction) throws Exception {
        Method writeReplace = sFunction.getClass().getDeclaredMethod("writeReplace");
        writeReplace.setAccessible(true);
        SerializedLambda serializedLambda = (SerializedLambda) writeReplace.invoke(sFunction);
        String methodName = serializedLambda.getImplMethodName();
        if (methodName.startsWith("is")) {
            methodName = methodName.substring(2);
        } else if(methodName.startsWith("get")||methodName.startsWith("set")){
            methodName = methodName.substring(3);
        }else{
            throw new IllegalArgumentException("lambda参数错误,方法名需以is,get,set开头!当前方法名:"+methodName);
        }
        if (methodName.length() == 1 || (methodName.length() > 1 && !Character
                .isUpperCase(methodName.charAt(1)))) {
            methodName = methodName.substring(0, 1).toLowerCase(Locale.ENGLISH) + methodName.substring(1);
        }
        return methodName;
    }
}
