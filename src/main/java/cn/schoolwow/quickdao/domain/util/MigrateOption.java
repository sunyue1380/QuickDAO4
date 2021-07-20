package cn.schoolwow.quickdao.domain.util;

import cn.schoolwow.quickdao.dao.DAO;
import cn.schoolwow.quickdao.domain.Entity;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * 数据库迁移选项
 * */
public class MigrateOption {
    /**
     * 源数据库
     * */
    public DAO source;

    /**
     * 目标数据库
     * */
    public DAO target;

    /**
     * 单次批量插入个数,默认10000
     * */
    public int batchCount = 10000;

    /**
     * 过滤待迁移的表
     * */
    public Predicate<Entity> tableFilter;

    /**
     * 在源表和目标表执行函数,第一个参数为源表,第二个参数为目标表
     * */
    public BiConsumer<Entity,Entity> tableConsumer;
}
