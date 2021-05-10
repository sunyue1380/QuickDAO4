package cn.schoolwow.quickdao.sqlserver.test;

import cn.schoolwow.quickdao.sqlserver.SQLServerTest;
import cn.schoolwow.quickdao.sqlserver.entity.Person;
import org.junit.Assert;
import org.junit.Test;

import java.time.ZoneId;

/**TableField注解测试*/
public class AnnotationTest extends SQLServerTest {

    @Test
    public void testTableField(){
        dao.rebuild(Person.class);
        //function
        {
            Person person = new Person();
            person.setPassword("123456");
            person.setFirstName("Bill");
            person.setLastName("Gates");
            person.setAddress("Xuanwumen 10");
            person.setCity("Beijing");
            int effect = dao.insert(person);
            Assert.assertEquals(1, effect);

            person = dao.fetch(Person.class,1);
            Assert.assertEquals("123456", person.getPassword());
        }
        //createdAt
        {
            Person person = dao.fetch(Person.class,1);
            Assert.assertTrue(System.currentTimeMillis()-person.getCreatedAt().atZone(ZoneId.systemDefault()).toEpochSecond()*1000<3000);
        }
        //updatedAt
        {
            Person person = dao.fetch(Person.class,1);
            person.setAddress("Xuanwumen 11");
            int effect = dao.update(person);
            Assert.assertEquals(1, effect);

            person = dao.fetch(Person.class,1);
            Assert.assertTrue(System.currentTimeMillis()-person.getUpdatedAt().atZone(ZoneId.systemDefault()).toEpochSecond()*1000<3000);
        }
    }
}
