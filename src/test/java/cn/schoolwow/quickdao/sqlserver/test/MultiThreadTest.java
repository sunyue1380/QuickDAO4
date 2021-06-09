package cn.schoolwow.quickdao.sqlserver.test;

import cn.schoolwow.quickdao.sqlserver.SQLServerTest;
import cn.schoolwow.quickdao.sqlserver.entity.Order;
import cn.schoolwow.quickdao.sqlserver.entity.Person;
import cn.schoolwow.quickdao.transaction.Transaction;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MultiThreadTest extends SQLServerTest {

    @Test
    public void testMultiThread() throws InterruptedException {
        dao.rebuild(Person.class);
        dao.rebuild(Order.class);
        ThreadPoolExecutor poolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        for(int i=0;i<100;i++){
            //100个线程,每个线程新建10条数据
            final int index = i;
            poolExecutor.execute(()->{
                Transaction transaction = dao.startTransaction();
                for(int j=index*10;j<(index+1)*10;j++){
                    //新建Person
                    {
                        Person person = new Person();
                        person.setPassword("123456");
                        person.setFirstName("Bill");
                        person.setLastName("Gates "+j);
                        person.setAddress("Xuanwumen 10");
                        person.setCity("Beijing");
                        int effect = transaction.insert(person);
                        Assert.assertEquals(1,effect);
                    }
                }
                transaction.commit();
                transaction.endTransaction();
                //新建Order
                Order order = new Order();
                order.setId(UUID.randomUUID().toString());
                order.setOrderNo(index);
                order.setPersonId(index);
                int effect = dao.insert(order);
                Assert.assertEquals(1,effect);
            });
        }
        poolExecutor.shutdown();
        poolExecutor.awaitTermination(1,TimeUnit.HOURS);
        {
            long count = dao.query(Person.class).execute().count();
            Assert.assertEquals(1000,count);
        }
        {
            long count = dao.query(Order.class).execute().count();
            Assert.assertEquals(100,count);
        }
    }
}
