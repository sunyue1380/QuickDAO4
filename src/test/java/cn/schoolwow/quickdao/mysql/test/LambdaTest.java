package cn.schoolwow.quickdao.mysql.test;

import cn.schoolwow.quickdao.domain.PageVo;
import cn.schoolwow.quickdao.mysql.MySQLTest;
import cn.schoolwow.quickdao.mysql.entity.Order;
import cn.schoolwow.quickdao.mysql.entity.Person;
import cn.schoolwow.quickdao.query.condition.Condition;
import cn.schoolwow.quickdao.query.response.Response;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class LambdaTest extends MySQLTest {

    public LambdaTest(){
        initialize();
    }
    
    @Test
    public void fetch(){
        {
            Person person = dao.fetch(Person.class,1);
            Assert.assertEquals("Gates",person.getLastName());
            Assert.assertTrue(dao.exist(person));
        }
        {
            Person person = dao.fetch(Person.class,Person::getLastName,"Carter");
            Assert.assertEquals("Thomas",person.getFirstName());
            Assert.assertTrue(dao.exist(person));
        }
        {
            List<Person> personList = dao.fetchList(Person.class,Person::getPassword,"123456");
            Assert.assertEquals(3,personList.size());
            Assert.assertTrue(dao.existAll(personList));
        }
    }

    @Test
    public void query(){
        {
            Response<Person> response = dao.query(Person.class)
                    .distinct()
                    .lambdaCondition()
                    .addNullQuery(Person::getLastName)
                    .addNotNullQuery(Person::getLastName)
                    .addEmptyQuery(Person::getLastName)
                    .addNotEmptyQuery(Person::getLastName)
                    .addInQuery(Person::getLastName,"1","2")
                    .addNotInQuery(Person::getLastName,"3","4")
                    .addBetweenQuery(Person::getId,1,2)
                    .addLikeQuery(Person::getLastName,"%a%")
                    .addNotLikeQuery(Person::getLastName,"b%")
                    .addQuery(Person::getLastName,"=","a")
                    .addQuery(Person::getUpdatedAt,"<=",new Date())
                    .done()
                    .execute();
            Assert.assertEquals(0,response.count());
        }
        {
            long count = dao.query(Person.class)
                    .lambdaCondition()
                    .addQuery(Person::getPassword,"123456")
                    .execute()
                    .count();
            Assert.assertEquals(3,count);
        }
    }

    @Test
    public void page(){
        {
            Person person = dao.query(Person.class)
                    .lambdaCondition()
                    .orderByDesc(Person::getLastName)
                    .done()
                    .limit(0,1)
                    .execute()
                    .getOne();
            Assert.assertEquals("Wilson",person.getLastName());
        }
        {
            PageVo<Person> personPageVo = dao.query(Person.class)
                    .page(1,10)
                    .execute()
                    .getPagingList();
            Assert.assertEquals(1,personPageVo.getCurrentPage());
            Assert.assertEquals(10,personPageVo.getPageSize());
            Assert.assertEquals(1,personPageVo.getTotalPage());
            Assert.assertEquals(3,personPageVo.getTotalSize());
            Assert.assertEquals(3,personPageVo.getList().size());
        }
    }

    @Test
    public void joinTable() {
        {
            List<Person> personList = dao.query(Person.class)
                    .lambdaCondition()
                    .joinTable(Order.class,Person::getId,Order::getPersonId)
                    .on(Person::getLastName,Order::getLastName)
                    .addQuery(Order::getPersonId,1)
                    .done()
                    .done()
                    .compositField()
                    .execute()
                    .getList();
            Assert.assertEquals(1,personList.size());
            Assert.assertNotNull(personList.get(0).getOrder());
        }
        {
            Condition joinCondition = dao.query(Person.class)
                    .addQuery("last_name","Gates")
                    .addColumn("id");
            List<Order> orderList = dao.query(Order.class)
                    .joinTable(joinCondition,"personId","id")
                    .done()
                    .execute()
                    .getList();
            Assert.assertEquals(1,orderList.size());
        }
        {
            List<Person> personList = dao.query(Person.class)
                    .crossJoinTable(Order.class)
                    .done()
                    .addRawQuery("t.id = t1.person_id")
                    .execute()
                    .getList();
            Assert.assertEquals(1,personList.size());
        }
    }

    @Test
    public void groupBy(){
        {
            int cityCount = (int) dao.query(Person.class)
                    .addColumn("count(city) cityCount")
                    .groupBy("city")
                    .having("count(city) > ?",1)
                    .execute()
                    .getSingleColumn(Integer.class);
            Assert.assertEquals(2,cityCount);
        }
    }

    @Test
    public void column(){
        {
            List<Person> personList = dao.query(Person.class)
                    .lambdaCondition()
                    .addColumn(Person::getLastName,Person::getFirstName)
                    .execute()
                    .getList();
            Assert.assertEquals(3,personList.size());
            for(Person person:personList){
                Assert.assertNotNull(person.getLastName());
                Assert.assertNull(person.getAddress());
                Assert.assertNull(person.getCity());
            }
        }
        {
            Response response = dao.query(Person.class)
                    .lambdaCondition()
                    .order(Person::getLastName,"desc")
                    .addColumn(Person::getLastName)
                    .execute();
            JSONArray array = response.getArray();
            Assert.assertEquals(3,array.size());
            List<Person> personList = response.getList();
            Assert.assertEquals(3,personList.size());
            List<String> lastNameList = response.getSingleColumnList(String.class);
            Assert.assertEquals(3,lastNameList.size());
            String lastName = (String) response.getSingleColumn(String.class);
            Assert.assertEquals("Wilson",lastName);
            JSONObject personObject = response.getObject();
            Assert.assertEquals("Wilson",personObject.getString("last_name"));
            Person person = (Person) response.getOne();
            Assert.assertEquals("Wilson",person.getLastName());
        }
    }

    @Test
    public void cloneable() {
        Condition<Person> condition = dao.query(Person.class).addQuery("password","123456");
        {
            Person person = condition.clone().lambdaCondition().addQuery(Person::getLastName,"Gates").execute().getOne();
            Assert.assertEquals("Gates",person.getLastName());
        }
        {
            Person person = condition.clone().lambdaCondition().addQuery(Person::getLastName,"Carter").execute().getOne();
            Assert.assertEquals("Carter",person.getLastName());
        }
    }

    public void initialize(){
        dao.rebuild(Person.class);
        dao.rebuild(Order.class);
        Person[] persons = new Person[3];
        //初始化数据
        {
            Person person = new Person();
            person.setPassword("123456");
            person.setFirstName("Bill");
            person.setLastName("Gates");
            person.setAddress("Xuanwumen 10");
            person.setCity("Beijing");
            persons[0] = person;
        }
        {
            Person person = new Person();
            person.setPassword("123456");
            person.setFirstName("Thomas");
            person.setLastName("Carter");
            person.setAddress("Changan Street");
            person.setCity("Beijing");
            persons[1] = person;
        }
        {
            Person person = new Person();
            person.setPassword("123456");
            person.setLastName("Wilson");
            person.setAddress("Champs-Elysees");
            persons[2] = person;
        }
        {
            int effect = dao.insert(persons);
            Assert.assertEquals(3, effect);
        }
        {
            Order order = new Order();
            order.setId(UUID.randomUUID().toString());
            order.setPersonId(1);
            order.setLastName("Gates");
            order.setOrderNo(1);
            int effect = dao.insert(order);
            Assert.assertEquals(1, effect);
        }
    }
}
