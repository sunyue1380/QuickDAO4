package cn.schoolwow.quickdao.domain;

import java.sql.PreparedStatement;

/**
 * SQL执行子项
 * */
public class ConnectionExecutorItem {
    /**
     * 预执行语句
     * */
    public PreparedStatement preparedStatement;

    /**
     * 名称
     * */
    public String name;

    /**
     * 用于打印的SQL语句
     * */
    public String sql;
}
