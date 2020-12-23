package cn.schoolwow.quickdao.sqlite.test;

import cn.schoolwow.quickdao.sqlite.SQLiteTest;
import cn.schoolwow.quickdao.sqlite.entity.Person;
import org.junit.Assert;
import org.junit.Test;

/**TableField注解测试*/
public class AnnotationTest extends SQLiteTest {

    @Test
    public void testTableField(){
        dao.rebuild(Person.class);
        {
            Person person = new Person();
            person.setPassword("123456");
            person.setFirstName("Bill");
            person.setLastName("Gates");
            person.setAddress("Xuanwumen 10");
            person.setCity("Beijing");
            int effect = dao.insert(person);
            Assert.assertEquals(1, effect);
        }
        //createdAt
        {
            Person person = dao.fetch(Person.class,1);
            Assert.assertTrue(System.currentTimeMillis()-person.getCreatedAt().getTime()<3000);
        }
        //updatedAt
        {
            Person person = dao.fetch(Person.class,1);
            person.setAddress("Xuanwumen 11");
            int effect = dao.update(person);
            Assert.assertEquals(1, effect);

            person = dao.fetch(Person.class,1);
            Assert.assertTrue(System.currentTimeMillis()-person.getUpdatedAt().getTime()<3000);
        }
    }
}
