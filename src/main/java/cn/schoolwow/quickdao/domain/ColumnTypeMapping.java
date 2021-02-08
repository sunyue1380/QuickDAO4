package cn.schoolwow.quickdao.domain;

public interface ColumnTypeMapping {
    /**
     * 列类型转换
     * @param property 列信息
     * @return 要转换的类
     * */
    Class columnMappingType(Property property);
}
