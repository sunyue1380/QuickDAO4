package cn.schoolwow.quickdao.domain;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 实体类信息
 */
public class Entity {
    /**
     * 实体类对象
     */
    public Class clazz;
    /**
     * 原始表名
     */
    public String tableName;
    /**
     * 转义后表名
     */
    public String escapeTableName;
    /**
     * 表注释
     */
    public String comment;
    /**
     * Id属性
     */
    public Property id;
    /**
     * 索引列表
     */
    public List<IndexField> indexFieldList = new ArrayList<>();
    /**
     * 属性字段(排除ignore字段和实体包内字段)
     */
    public List<Property> properties;
    /**
     * 外键约束字段
     */
    public List<Property> foreignKeyProperties = new ArrayList<>();
    /**
     * Field数组(实体包类)
     */
    public Field[] compositFields;
    /**
     * 表编码格式
     * */
    public String charset;
    /**
     * 表引擎
     * */
    public String engine;
    /**
     * 判断记录是否唯一的字段列表
     * */
    public List<Property> uniqueProperties = new ArrayList<>();

    /**
     * 根据字段名查询数据库列名,只返回列名
     * */
    public String getColumnNameByFieldName(String field) {
        Property property = getPropertyByFieldName(field);
        return null==property?field:property.column;
    }

    /**
     * 根据字段名返回对应属性
     * */
    public Property getPropertyByFieldName(String field) {
        if(null==field||field.isEmpty()){
            return null;
        }
        for(Property property:properties){
            if(field.equals(property.name)||field.equals(property.column)){
                return property;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return tableName.equals(entity.tableName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableName);
    }
}
