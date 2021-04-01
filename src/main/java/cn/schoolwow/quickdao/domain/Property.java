package cn.schoolwow.quickdao.domain;

import cn.schoolwow.quickdao.annotation.ForeignKey;
import cn.schoolwow.quickdao.annotation.IdStrategy;

import java.io.*;

/**
 * 实体类属性信息
 */
public class Property implements Serializable,Cloneable {
    /**
     * 是否是id
     */
    public boolean id;
    /**
     * id生成策略
     * */
    public transient IdStrategy strategy;
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
     * 类
     */
    public transient Class clazz;
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
     * 转义后的check约束
     */
    public String escapeCheck;
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
    public transient ForeignKey foreignKey;
    /**
     * 所属实体
     * */
    public transient Entity entity;

    /**复制拷贝transient字段*/
    public void copyTransientField(Property target){
        this.strategy = target.strategy;
        this.clazz = target.clazz;
        this.foreignKey = target.foreignKey;
        this.entity = target.entity;
    }

    @Override
    public Property clone(){
        ByteArrayInputStream bais = null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()){
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);
            oos.close();

            bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            Property property = (Property) ois.readObject();
            property.copyTransientField(this);
            bais.close();
            return property;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(null!=bais){
                try {
                    bais.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
