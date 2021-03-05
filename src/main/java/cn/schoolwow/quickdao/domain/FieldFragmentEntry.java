package cn.schoolwow.quickdao.domain;

import java.io.Serializable;

/**
 * 字段查询条件
 * */
public class FieldFragmentEntry implements Serializable {
    /**字段*/
    public String field;

    /**SQL片段*/
    public String fragment;

    public FieldFragmentEntry(String field, String fragment) {
        this.field = field;
        this.fragment = fragment;
    }
}
