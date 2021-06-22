package cn.schoolwow.quickdao.oracle.test;

import cn.schoolwow.quickdao.oracle.OracleTest;
import cn.schoolwow.quickdao.oracle.entity.Person;
import cn.schoolwow.quickdao.transaction.Transaction;
import com.alibaba.fastjson.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**事务测试*/
public class TransactionTest extends OracleTest {

    @Before
    public void before(){
        dao.rebuild(Person.class);
    }

    @Test
    public void commit(){
        {
            Person person = dao.fetch(Person.class,"lastName","Gates");
            Assert.assertNull(person);
        }

        Transaction transaction = dao.startTransaction();
        {
            //一般事务
            Person person = new Person();
            person.setPassword("123456");
            person.setFirstName("Bill");
            person.setLastName("Gates");
            person.setAddress("Xuanwumen 10");
            person.setCity("Beijing");
            transaction.insert(person);

            person = dao.fetch(Person.class,"lastName","Gates");
            Assert.assertNull(person);

            transaction.commit();
            person = dao.fetch(Person.class,"lastName","Gates");
            Assert.assertNotNull(person);
        }
        {
            //Condition事务
            transaction.query("person")
                    .addInsert("password","123456")
                    .addInsert("first_name","John")
                    .addInsert("last_name","Adams")
                    .addInsert("address","Oxford Street")
                    .addInsert("city","London")
                    .execute()
                    .insert();
            JSONObject person = dao.fetch("person","last_name","Adams");
            Assert.assertNull(person);

            transaction.commit();
            person = dao.fetch("person","last_name","Adams");
            Assert.assertNotNull(person);
            Assert.assertEquals("Oxford Street",person.getString("address"));
        }
        transaction.endTransaction();
    }

    @Test
    public void rollback(){
        {
            Person person = dao.fetch(Person.class,"lastName","Carter");
            Assert.assertNull(person);
        }

        Transaction transaction = dao.startTransaction();
        {
            //一般事务
            Person person = new Person();
            person.setPassword("123456");
            person.setFirstName("Thomas");
            person.setLastName("Carter");
            person.setAddress("Changan Street");
            person.setCity("Beijing");
            int effect = transaction.insert(person);
            Assert.assertEquals(1,effect);

            transaction.rollback();
            person = dao.fetch(Person.class,"lastName","Carter");
            Assert.assertNull(person);
        }
        {
            //Condition事务
            int effect = transaction.query("person")
                    .addInsert("password","123456")
                    .addInsert("first_name","Bill")
                    .addInsert("last_name","Gates")
                    .addInsert("address","Xuanwumen 10")
                    .addInsert("city","Beijing")
                    .execute()
                    .insert();
            Assert.assertEquals(1,effect);
            transaction.rollback();
            JSONObject person = dao.fetch("person","last_name","Gates");
            Assert.assertNull(person);
        }
        transaction.endTransaction();
    }
}
