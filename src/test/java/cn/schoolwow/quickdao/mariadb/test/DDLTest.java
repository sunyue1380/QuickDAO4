package cn.schoolwow.quickdao.mariadb.test;

import cn.schoolwow.quickdao.QuickDAO;
import cn.schoolwow.quickdao.annotation.IndexType;
import cn.schoolwow.quickdao.dao.DAO;
import cn.schoolwow.quickdao.domain.Entity;
import cn.schoolwow.quickdao.domain.IndexField;
import cn.schoolwow.quickdao.domain.Property;
import cn.schoolwow.quickdao.mariadb.MariaDBTest;
import cn.schoolwow.quickdao.mariadb.entity.Order;
import cn.schoolwow.quickdao.mariadb.entity.Person;
import org.junit.Assert;
import org.junit.Test;

/**
 * DDL操作测试
 * */
public class DDLTest extends MariaDBTest {
    @Test
    public void index(){
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

    /**
     * 实体类同步
     * */
    @Test
    public void syncEntityList() {
        DAO dao = QuickDAO.newInstance()
                .dataSource(dataSource)
                .entity(Person.class)
                .entity(Order.class)
                .build();
        dao.rebuild(Person.class);
        //缺少字段时同步
        {
            dao.dropColumn("person","city");
            dao.syncEntityList();
            Entity dbEntity = dao.getDbEntityList().stream().filter(entity -> entity.tableName.equals("person")).findFirst().orElse(null);
            Assert.assertNotNull("同步新增实体字段信息失败!",dbEntity);
            Property dbProperty = dbEntity.properties.stream().filter(property -> property.column.equals("city")).findFirst().orElse(null);
            Assert.assertNotNull("同步新增实体字段信息失败!",dbProperty);
        }
        //多余字段时同步
        {
            Property property = new Property();
            property.column = "phone_number";
            property.columnType = "varchar(16)";
            property.comment = "手机号码";
            dao.createColumn("person",property);
            dao.syncEntityList();
            Entity dbEntity = dao.getDbEntityList().stream().filter(entity -> entity.tableName.equals("person")).findFirst().orElse(null);
            Assert.assertNotNull("同步删除实体字段信息失败!",dbEntity);
            Property dbProperty = dbEntity.properties.stream().filter(property1 -> property1.column.equals("phone_number")).findFirst().orElse(null);
            Assert.assertNull("同步删除实体字段信息失败!",dbProperty);
        }
        {
            Property property = dao.getProperty("person","last_name");
            Assert.assertEquals("last_name",property.column);
            Assert.assertEquals("varchar(64)",property.columnType+"("+property.length+")");
            Assert.assertEquals("姓",property.comment);
        }
    }
}
