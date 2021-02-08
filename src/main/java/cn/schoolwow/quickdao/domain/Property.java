package cn.schoolwow.quickdao.domain;

import cn.schoolwow.quickdao.annotation.ForeignKey;
import cn.schoolwow.quickdao.annotation.IdStrategy;

/**
 * 实体类属性信息
 */
public class Property {
    /**
     * 是否是id
     */
    public boolean id;
    /**
     * id生成策略
     * */
    public IdStrategy strategy;
    /**
     * 返回列标签名称
     */
    public String columnLabel;
    /**
     * 列名
     */
    public String column;
    /**
     * 数据库类型
     */
    public String columnType;
    /**
     * 注释
     */
    public String comment;
    /**
     * 类名
     */
    public String className;
    /**
     * 属性名
     */
    public String name;
    /**
     * 是否非空
     */
    public boolean notNull;
    /**
     * check约束
     */
    public String check;
    /**
     * 默认值
     */
    public String defaultValue;
    /**
     * 属性所在位置
     */
    public int position;
    /**
     * 在哪个字段之后
     */
    public String after;
    /**
     * 字段函数
     */
    public String function;
    /**
     * 是否填充插入时间
     */
    public boolean createdAt;
    /**
     * 是否填充更新时间
     */
    public boolean updateAt;
    /**
     * 外键关联
     */
    public ForeignKey foreignKey;
    /**
     * 所属实体
     * */
    public Entity entity;
}
