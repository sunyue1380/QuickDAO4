package cn.schoolwow.quickdao.sqlite.test;

import cn.schoolwow.quickdao.annotation.IndexType;
import cn.schoolwow.quickdao.domain.IndexField;
import cn.schoolwow.quickdao.sqlite.SQLiteTest;
import cn.schoolwow.quickdao.sqlite.entity.Person;
import org.junit.Assert;
import org.junit.Test;

/**
 * DDL操作测试
 * */
public class DDLTest extends SQLiteTest {
    @Test
    public void index(){
        dao.rebuild(Person.class);
        Assert.assertTrue("指定索引不存在",dao.hasIndex("person","last_name_index"));
        dao.dropIndex("person","last_name_index");
        Assert.assertFalse("指定索引已存在",dao.hasIndex("person","last_name_index"));
        IndexField indexField = new IndexField();
        indexField.tableName = "person";
        indexField.indexType = IndexType.NORMAL;
        indexField.indexName = "last_name_index";
        indexField.comment = "姓氏索引";
        indexField.columns.add("last_name");
        dao.createIndex(indexField);
        Assert.assertTrue("指定索引不存在",dao.hasIndex("person","last_name_index"));
    }
}
