package cn.schoolwow.quickdao.domain;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 实体类生成选项
 * */
public class GenerateEntityFileOption {
    /**
     * java源文件路径
     * */
    public String sourceClassPath;

    /**
     * 数据库表过滤
     * */
    public Predicate<Entity> tableFilter;

    /**
     * 数据库类型映射
     * <p>参数为数据库列类型</p>
     * <p>返回值为对应Java类型字符串</p>
     * */
    public Function<String,String> columnFieldTypeMapping;

    /**
     * 实体类类名映射
     * <p>第一个参数为数据库表信息</p>
     * <p>第二个参数默认实体类名</p>
     * <p返回值该类映射类名,下划线会转为驼峰式.返回值支持带包名,例如user.User</p>
     * */
    public BiFunction<Entity,String,String> entityClassNameMapping;
}
