package cn.schoolwow.quickdao.builder.typeFieldMapping;

import cn.schoolwow.quickdao.domain.SingleTypeFieldMapping;

import java.util.ArrayList;
import java.util.List;

public class AbstractTypeFieldMapping implements TypeFieldMapping {
    protected List<SingleTypeFieldMapping> typeFieldMappingList;

    public AbstractTypeFieldMapping() {
    }

    @Override
    public SingleTypeFieldMapping getSingleTypeFieldMapping(Class type) {
        List<SingleTypeFieldMapping> typeFieldMappingArrayList = new ArrayList<>();
        for (SingleTypeFieldMapping singleTypeFieldMapping : typeFieldMappingList) {
            for (Class clazz : singleTypeFieldMapping.clazzList) {
                if (clazz.getName().equals(type.getName())) {
                    typeFieldMappingArrayList.add(singleTypeFieldMapping);
                    break;
                }
            }
        }
        if (typeFieldMappingArrayList.isEmpty()) {
            throw new IllegalArgumentException("不支持的java字段类型映射!请使用@ColumnType注解手动指定该类型对应数据库类型!Java类型:" + type.getName());
        }
        if (typeFieldMappingArrayList.size() == 1) {
            return typeFieldMappingArrayList.get(0);
        }
        for (SingleTypeFieldMapping typeFieldMapping : typeFieldMappingArrayList) {
            if (typeFieldMapping.primary) {
                return typeFieldMapping;
            }
        }
        throw new IllegalArgumentException("未指定默认字段转换类型!当前java类型:" + type.getName());
    }

    @Override
    public SingleTypeFieldMapping getSingleTypeFieldMapping(String columnType) {
        if(columnType.contains("(")){
            columnType = columnType.substring(0,columnType.indexOf("("));
        }
        columnType = columnType.toUpperCase();
        for (SingleTypeFieldMapping singleTypeFieldMapping : typeFieldMappingList) {
            if (columnType.equals(singleTypeFieldMapping.columnType)) {
                return singleTypeFieldMapping;
            }
        }
        throw new IllegalArgumentException("当前数据库类型无对应Java类型!数据库类型:" + columnType);
    }

    @Override
    public List<SingleTypeFieldMapping> getSingleTypeFieldMappingList() {
        return typeFieldMappingList;
    }
}
