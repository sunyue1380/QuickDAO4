package cn.schoolwow.quickdao.domain.util;

import cn.schoolwow.quickdao.dao.DAO;
import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.Property;

import java.util.function.Predicate;

/**
 * 数据库表结构同步选项
 * */
public class TableStructureSynchronizedOption {
    /**
     * 源数据库
     * */
    public DAO source;

    /**
     * 目标数据库
     * */
    public DAO target;

    /**
     * 是否新增该表
     * */
    public Predicate<Entity> createTablePredicate;

    /**
     * 是否新增该属性
     * */
    public Predicate<Property> createPropertyPredicate;
}
