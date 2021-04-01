package cn.schoolwow.quickdao.postgre;

import cn.schoolwow.quickdao.DAOUtil;
import cn.schoolwow.quickdao.DatabaseTest;
import cn.schoolwow.quickdao.postgre.entity.Order;
import cn.schoolwow.quickdao.postgre.entity.Person;
import cn.schoolwow.quickdao.postgre.entity.Product;
import cn.schoolwow.quickdao.transaction.Transaction;
import org.junit.Assert;

import java.util.Date;
import java.util.UUID;

public class PostgreLTest extends DatabaseTest {
    static{
        dataSource = DAOUtil.getPostgreDataSource();
        dao = DAOUtil.getPostgreDAO(dataSource);
    }

    protected void initializeProduct(){
        dao.rebuild(Product.class);
        Transaction transaction = dao.startTransaction();
        String[] productNames = new String[]{"笔记本电脑","冰箱","电视机","智能音箱"};
        String[] types = new String[]{"电器","电器","电器","数码"};
        int[] prices = new int[]{4000,600,3000,1000};
        for(int i=0;i<productNames.length;i++){
            Product product = new Product();
            product.setName(productNames[i]);
            product.setType(types[i]);
            product.setPrice(prices[i]);
            product.setPublishTime(new Date());
            product.setPersonId(1);
            transaction.insert(product);
        }
        transaction.commit();
        transaction.endTransaction();
    }

    protected void initializePersonAndOrder(){
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
