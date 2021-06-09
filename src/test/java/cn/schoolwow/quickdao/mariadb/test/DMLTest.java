package cn.schoolwow.quickdao.mariadb.test;

import cn.schoolwow.quickdao.domain.Property;
import cn.schoolwow.quickdao.mariadb.MariaDBTest;
import cn.schoolwow.quickdao.mariadb.entity.Order;
import cn.schoolwow.quickdao.mariadb.entity.Person;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

/**
 * DML操作测试
 * */
public class DMLTest extends MariaDBTest {

    public DMLTest(){
        dao.rebuild(Person.class);
        dao.rebuild("order");
    }

    @Test
    public void testDML(){
        insert();
        update();
        save();
        delete();
        alterColumn();
    }

    private void insert() {
        //单独插入
        {
            Person person = new Person();
            person.setPassword("123456");
            person.setFirstName("Bill");
            person.setLastName("Gates");
            person.setAddress("Xuanwumen 10");
            person.setCity("Beijing");
            int effect = dao.insert(person);
            Assert.assertEquals(1, effect);
            effect = dao.insertIgnore(person);
            Assert.assertEquals(0, effect);
        }
        //批量插入
        {
            Person[] persons = new Person[5];
            for(int i=0;i<persons.length;i++){
                Person person = new Person();
                person.setPassword("123456");
                person.setFirstName("Jack");
                person.setLastName("Ma "+i);
                person.setAddress("Xuanwumen 10");
                person.setCity("Beijing");
                persons[i] = person;
            }
            int effect = dao.insert(persons);
            Assert.assertEquals(5, effect);
            for(Person person:persons){
                Assert.assertTrue(person.getId()>0);
            }
        }
        //无实体类插入
        {
            int effect = dao.query("order")
                    .addInsert("id",UUID.randomUUID().toString())
                    .addInsert("order_no",100)
                    .addInsert("person_id",100)
                    .execute()
                    .insert();
            Assert.assertEquals(1, effect);
        }

        //插入Order类
        {
            {
                Order order = new Order();
                order.setId(UUID.randomUUID().toString());
                order.setPersonId(1);
                order.setOrderNo(1);
                int effect = dao.insert(order);
                Assert.assertEquals(1, effect);
            }
            {
                Order[] orders = new Order[5];
                for(int i=0;i<orders.length;i++){
                    Order order = new Order();
                    order.setId(UUID.randomUUID().toString());
                    order.setPersonId(i+2);
                    order.setOrderNo(i+2);
                    orders[i] = order;
                }
                int effect = dao.insert(orders);
                Assert.assertEquals(5,effect);
            }
        }
    }

    private void update(){
        //根据唯一性约束更新
        {
            {
                Person person = new Person();
                person.setPassword("123456");
                person.setLastName("Gates");
                person.setAddress("Xuanwumen 11");
                int effect = dao.update(person);
                Assert.assertEquals(1, effect);
                person = dao.fetch(Person.class,"lastName","Gates");
                Assert.assertEquals("Xuanwumen 11", person.getAddress());
            }
            {
                Person[] persons = new Person[5];
                for(int i=0;i<persons.length;i++){
                    Person person = new Person();
                    person.setPassword("123456");
                    person.setFirstName("Jack");
                    person.setLastName("Ma "+i);
                    person.setAddress("Xuanwumen "+i);
                    person.setCity("Beijing");
                    persons[i] = person;
                }
                int effect = dao.update(persons);
                Assert.assertEquals(5, effect);
                List<Person> personList = dao.fetchList(Person.class,"firstName","Jack");
                Assert.assertEquals(5, personList.size());
                for(int i=0;i<personList.size();i++){
                    Person person = personList.get(i);
                    Assert.assertEquals("Jack",person.getFirstName());
                    Assert.assertEquals("Xuanwumen "+i,person.getAddress());
                }
            }
        }
        //根据id更新
        {
            {
                Order order = dao.fetch(Order.class,"orderNo",1);
                order.setPersonId(2);
                int effect = dao.update(order);
                Assert.assertEquals(1, effect);
                order = dao.fetch(Order.class,"orderNo",1);
                Assert.assertEquals(2, order.getPersonId());
            }
            {
                List<Order> orderList = dao.query(Order.class)
                        .addBetweenQuery("orderNo",2,7)
                        .execute()
                        .getList();
                for(Order order:orderList){
                    order.setPersonId(order.getOrderNo()+10);
                }
                int effect = dao.update(orderList);
                Assert.assertEquals(5, effect);
                orderList = dao.query(Order.class)
                        .addBetweenQuery("orderNo",2,7)
                        .execute()
                        .getList();
                for(Order order:orderList){
                    Assert.assertEquals(order.getOrderNo()+10,order.getPersonId());
                }
            }
        }
        //关联更新
        {
            int effect = dao.query(Order.class)
                    .joinTable(Person.class,"person_id","id")
                    .addQuery("id",1)
                    .done()
                    .addUpdate("orderNo",1)
                    .execute()
                    .update();
            Assert.assertEquals(0, effect);
        }
        {
            int effect = dao.query(Person.class)
                    .addUpdate("password","654321")
                    .execute()
                    .update();
            Assert.assertEquals(6,effect);
            List<Person> personList = dao.fetchList(Person.class,"password","654321");
            Assert.assertEquals(6,personList.size());
        }
    }

    private void save() {
        //新增记录
        {
            long count = dao.query(Person.class).execute().count();
            Person person = new Person();
            person.setPassword("123456");
            person.setFirstName("John");
            person.setLastName("Adams");
            person.setAddress("Oxford Street");
            person.setCity("London");
            int effect = dao.save(person);
            Assert.assertEquals(1, effect);
            Assert.assertEquals(count+1,dao.query(Person.class).execute().count());
        }
        //更新记录(根据唯一性约束更新)
        {
            Person person = new Person();
            person.setPassword("123456");
            person.setFirstName("Bill");
            person.setLastName("Adams");
            person.setAddress("Xuanwumen 100");
            person.setCity("TianJin");
            int effect = dao.save(person);
            Assert.assertEquals(1, effect);
            person = dao.fetch(Person.class,"lastName","Adams");
            Assert.assertEquals("Xuanwumen 100",person.getAddress());
        }
        //更新记录(根据id更新)
        {
            Order order = dao.fetch(Order.class,"orderNo",1);
            order.setPersonId(10);
            int effect = dao.save(order);
            Assert.assertEquals(1, effect);
            order = dao.fetch(Order.class,"orderNo",1);
            Assert.assertEquals(10, order.getPersonId());
        }
    }

    private void delete() {
        {
            long count = dao.query(Person.class).execute().count();
            int effect = dao.delete(Person.class,1);
            Assert.assertEquals(1,effect);
            Assert.assertEquals(count-1,dao.query(Person.class).execute().count());
        }
        {
            long count = dao.query(Order.class).execute().count();
            int effect = dao.delete(Order.class,"orderNo",1);
            Assert.assertEquals(1,effect);
            Assert.assertEquals(count-1,dao.query(Order.class).execute().count());
        }
        {
            long count = dao.query(Order.class).execute().count();
            int effect = dao.query(Order.class)
                    .addBetweenQuery("orderNo",2,7)
                    .execute()
                    .delete();
            Assert.assertEquals(5,effect);
            Assert.assertEquals(count-5,dao.query(Order.class).execute().count());
        }
    }

    private void alterColumn(){
        Property dropProperty = dao.dropColumn("person","city");
        Assert.assertTrue("表不存在!",dao.hasTable("person"));
        Assert.assertFalse("指定列已存在!",dao.hasColumn("person","city"));
        dao.createColumn(dropProperty.entity.tableName,dropProperty);
        Assert.assertTrue("表不存在!",dao.hasTable("person"));
        Assert.assertTrue("指定列不存在!",dao.hasColumn("person","city"));
    }
}
