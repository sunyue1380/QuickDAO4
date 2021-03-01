package cn.schoolwow.quickdao.domain;

import java.util.*;

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
     * 实体类成员变量
     */
    public Map<String,List<String>> compositFieldMap = new HashMap<>();
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
        if(field.contains(" ")){
            field = field.substring(0,field.indexOf(" "));
        }
        for(Property property:properties){
            if(field.equals(property.name)||field.equals(property.column)){
                return property;
            }
        }
        return null;
    }

    /**
     * 获取对应实体类成员变量
     * */
    public String getCompositeFieldName(String className) {
        if(!compositFieldMap.containsKey(className)){
            return null;
        }
        List<String> fieldNameList = compositFieldMap.get(className);
        if(fieldNameList.isEmpty()){
            return null;
        }
        if(fieldNameList.size()==1){
            return fieldNameList.get(0);
        }
        throw new IllegalArgumentException("类[" + clazz.getName() + "]存在[" + fieldNameList.size() + "]个类型为[" + className + "]的成员变量!请手动指定需要关联的实体类成员变量!");
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
