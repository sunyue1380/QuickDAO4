package cn.schoolwow.quickdao.builder.typeFieldMapping;

import cn.schoolwow.quickdao.domain.SingleTypeFieldMapping;

import java.util.List;

public interface TypeFieldMapping {
    /**
     * 根据Java类型获取字段映射对象
     *
     * @param type Java类型
     */
    SingleTypeFieldMapping getSingleTypeFieldMapping(Class type);

    /**
     * 根据数据库类型获取字段映射对象
     *
     * @param columnType 数据库类型
     */
    SingleTypeFieldMapping getSingleTypeFieldMapping(String columnType);

    /**
     * 获取类型字段映射列表
     */
    List<SingleTypeFieldMapping> getSingleTypeFieldMappingList();
}
