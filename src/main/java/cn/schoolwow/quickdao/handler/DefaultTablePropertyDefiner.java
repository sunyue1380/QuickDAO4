package cn.schoolwow.quickdao.handler;

import cn.schoolwow.quickdao.annotation.IdStrategy;
import cn.schoolwow.quickdao.annotation.IndexType;
import cn.schoolwow.quickdao.domain.IndexField;
import cn.schoolwow.quickdao.domain.Property;

/**实体类列定义*/
public class DefaultTablePropertyDefiner implements TablePropertyDefiner{
    /**当前属性*/
    private Property property;
    /**用于返回表*/
    private TableDefiner tableDefiner;

    public DefaultTablePropertyDefiner(Property property, TableDefiner tableDefiner) {
        this.property = property;
        this.tableDefiner = tableDefiner;
    }

    @Override
    public TablePropertyDefiner id(boolean id) {
        property.id = true;
        property.strategy = IdStrategy.AutoIncrement;
        return this;
    }

    @Override
    public TablePropertyDefiner strategy(IdStrategy idStrategy) {
        property.strategy = idStrategy;
        return this;
    }

    @Override
    public TablePropertyDefiner columnType(String columnType) {
        property.columnType = columnType;
        return this;
    }

    @Override
    public TablePropertyDefiner columnName(String columnName) {
        property.column = columnName;
        return this;
    }

    @Override
    public TablePropertyDefiner comment(String comment) {
        property.comment = comment;
        return this;
    }

    @Override
    public TablePropertyDefiner notNull(boolean notNull) {
        property.notNull = notNull;
        return this;
    }

    @Override
    public TablePropertyDefiner primaryKey(boolean primaryKey) {
        property.id = true;
        return this;
    }

    @Override
    public TablePropertyDefiner check(String check) {
        if(null!=check){
            property.check = check.replace("#{"+property.name+"}",property.column);
            if(!property.check.isEmpty()&&!property.check.contains("(")){
                property.check = "("+property.check+")";
            }
        }
        return this;
    }

    @Override
    public TablePropertyDefiner defaultValue(String defaultValue) {
        property.defaultValue = defaultValue;
        return this;
    }

    @Override
    public TablePropertyDefiner index(IndexType indexType, String indexName, String using, String comment) {
        IndexField indexField = new IndexField();
        indexField.tableName = property.entity.tableName;
        indexField.indexType = indexType;
        if(null==indexName||indexName.isEmpty()){
            indexField.indexName = indexField.tableName+"_"+indexType.name().toLowerCase()+"_"+property.column;
        }else{
            indexField.indexName = indexName;
        }
        indexField.using = using;
        indexField.comment = comment;
        indexField.columns.add(property.column);
        property.entity.indexFieldList.add(indexField);
        return this;
    }

    @Override
    public TableDefiner done() {
        return tableDefiner;
    }
}
