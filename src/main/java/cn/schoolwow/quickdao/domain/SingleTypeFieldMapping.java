package cn.schoolwow.quickdao.domain;

/**类型字段映射*/
public class SingleTypeFieldMapping {
    /**java数据类型*/
    public Class[] clazzList;
    /**int类型*/
    public int types;
    /**数据库类型*/
    public String columnType;
    /**详细数据库类型*/
    public String columnTypeDetail;
    /**是否首选*/
    public boolean primary;

    //TODO
    public SingleTypeFieldMapping(String columnType, Class... clazzList) {
        this.clazzList = clazzList;
        this.columnType = columnType;
        this.columnTypeDetail = columnType;
    }

    public SingleTypeFieldMapping(int types, String columnType, Class... clazzList) {
        this.types = types;
        this.columnType = columnType;
        this.columnTypeDetail = columnType;
        this.clazzList = clazzList;
    }

    public SingleTypeFieldMapping(int types, String columnType, String columnTypeDetail, Class... clazzList) {
        this.types = types;
        this.columnType = columnType;
        this.columnTypeDetail = columnTypeDetail;
        this.clazzList = clazzList;
    }

    public SingleTypeFieldMapping(int types, String columnType, boolean primary, Class... clazzList) {
        this.types = types;
        this.columnType = columnType;
        this.columnTypeDetail = columnType;
        this.primary = primary;
        this.clazzList = clazzList;
    }

    public SingleTypeFieldMapping(int types, String columnType, String columnTypeDetail, boolean primary, Class... clazzList) {
        this.types = types;
        this.columnType = columnType;
        this.columnTypeDetail = columnTypeDetail;
        this.primary = primary;
        this.clazzList = clazzList;
    }
}
