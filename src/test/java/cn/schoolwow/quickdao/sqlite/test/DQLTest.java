package cn.schoolwow.quickdao.sqlite.test;

import cn.schoolwow.quickdao.domain.PageVo;
import cn.schoolwow.quickdao.query.condition.Condition;
import cn.schoolwow.quickdao.query.response.Response;
import cn.schoolwow.quickdao.sqlite.SQLiteTest;
import cn.schoolwow.quickdao.sqlite.entity.Order;
import cn.schoolwow.quickdao.sqlite.entity.Person;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.List;

public class DQLTest extends SQLiteTest {

    public DQLTest(){
        initializePersonAndOrder();
    }
    
    @Test
    public void fetch(){
        {
            Person person = dao.fetch(Person.class,1);
            Assert.assertEquals("Gates",person.getLastName());
            Assert.assertTrue(dao.exist(person));
        }
        {
            Person person = dao.fetch(Person.class,"lastName","Carter");
            Assert.assertEquals("Thomas",person.getFirstName());
            Assert.assertTrue(dao.exist(person));
        }
        {
            List<Person> personList = dao.fetchList(Person.class,"password","123456");
            Assert.assertEquals(3,personList.size());
            Assert.assertTrue(dao.existAll(personList));
        }
    }

    @Test
    public void query(){
        {
            Response response = dao.query(Person.class)
                    .distinct()
                    .addNullQuery("lastName")
                    .addNotNullQuery("lastName")
                    .addEmptyQuery("lastName")
                    .addNotEmptyQuery("lastName")
                    .addInQuery("lastName","1","2")
                    .addNotInQuery("lastName","3","4")
                    .addBetweenQuery("id",1,2)
                    .addLikeQuery("lastName","%a%")
                    .addNotLikeQuery("lastName","b%")
                    .addQuery("lastName","=","a")
                    .addQuery("updatedAt","<=",new Date())
                    .execute();
            Assert.assertEquals(0,response.count());
        }
        {
            long count = dao.query(Person.class)
                    .addQuery("password","123456")
                    .execute()
                    .count();
            Assert.assertEquals(3,count);
        }
        //or查询
        {
            Condition condition = dao.query(Person.class)
                    .distinct()
                    .addQuery("lastName","Gates");
            condition.or().addQuery("lastName","Carter");
            condition.or().addQuery("lastName","Wilson");
            Assert.assertEquals(3,condition.execute().count());
        }
        {
            Condition condition = dao.query(Person.class)
                    .distinct()
                    .addQuery("lastName","Gates")
                    .or("t.last_name = ?","Carter")
                    .or("t.last_name = ?","Wilson");
            Assert.assertEquals(3,condition.execute().count());
        }
    }

    @Test
    public void page(){
        {
            Person person = (Person) dao.query(Person.class)
                    .orderByDesc("lastName")
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
                    .joinTable(Order.class,"id","personId")
                    .on("lastName","lastName")
                    .addQuery("personId",1)
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
                    .addColumn("lastName","firstName")
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
            List<Person> personList = dao.query(Person.class)
                    .addColumnExclude("createdAt","updatedAt")
                    .execute()
                    .getList();
            Assert.assertEquals(3,personList.size());
            for(Person person:personList){
                Assert.assertNull(person.getCreatedAt());
                Assert.assertNull(person.getUpdatedAt());
            }
        }
        {
            Response response = dao.query(Person.class)
                    .order("lastName","desc")
                    .addColumn("lastName")
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
            Person person = condition.clone().addQuery("lastName","Gates").execute().getOne();
            Assert.assertEquals("Gates",person.getLastName());
        }
        {
            Person person = condition.clone().addQuery("lastName","Carter").execute().getOne();
            Assert.assertEquals("Carter",person.getLastName());
        }
    }

    @Test
    public void select() {
        JSONArray array = dao.rawSelect("select * from person where password = ?","123456");
        Assert.assertEquals(3,array.size());
        array = dao.rawSelect("select * from person where last_name = ?","Gates");
        Assert.assertEquals(1,array.size());
        Assert.assertEquals("Gates",array.getJSONObject(0).getString("last_name"));
    }
}