package cn.schoolwow.quickdao.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * 线程上下文Map
 * */
public class ThreadLocalMap {
    /**
     * 用于日志记录
     * */
    private static final ThreadLocal<Map<String, String>> threadLocal = new ThreadLocal<Map<String, String>>();

    /**
     * 获取线程上下文日志记录值
     * */
    public static String get(String key){
        Map<String,String> map = getThreadLocal();
        return map.get(key);
    }

    /**
     * 设置线程上下文日志记录值
     * */
    public static void put(String key, String val){
        Map<String,String> map = getThreadLocal();
        map.put(key,val);
    }

    /**
     * 设置线程上下文日志记录值
     * */
    public static void clear(){
        Map<String,String> map = getThreadLocal();
        map.clear();
    }

    /**
     * 获取ThreadLocal对象,用于日志记录
     * */
    private static Map<String,String> getThreadLocal(){
        Map<String,String> map = threadLocal.get();
        if(null==map){
            map = new HashMap<>();
            threadLocal.set(map);
        }
        return map;
    }
}
