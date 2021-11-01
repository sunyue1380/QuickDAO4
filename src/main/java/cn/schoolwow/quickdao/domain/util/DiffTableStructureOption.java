package cn.schoolwow.quickdao.domain.util;

import cn.schoolwow.quickdao.dao.DAO;
import cn.schoolwow.quickdao.domain.Property;

import java.util.function.BiPredicate;

public class DiffTableStructureOption {
    /**
     * 源数据库
     * */
    public DAO source;

    /**
     * 目标数据库
     * */
    public DAO target;

    /**
     * 指定要比对的表,为空则比对所有表
     * */
    public String[] tableNames;

    /**
     * 判断两个列是否相同,默认情况使用列类型和长度匹配
     * <p>返回true表示两列不同,false表示两列相同</p>
     * */
    public BiPredicate<Property,Property> diffPropertyPredicate = (source,target)->{
        if(!source.columnType.equals(target.columnType)){
            return true;
        }
        if(null==source.length&&null==target.length){
            return false;
        }
        if(null!=source.length){
            return !source.length.equals(target.length);
        }else{
            return !target.length.equals(source.length);
        }
    };

    /**
     * 是否执行列结构同步SQL语句
     * */
    public boolean executeSQL;

    /**
     * 执行修改的SQL语句
     * */
    public String sql;
}