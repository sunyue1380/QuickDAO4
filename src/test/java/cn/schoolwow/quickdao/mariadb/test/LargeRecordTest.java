package cn.schoolwow.quickdao.mariadb.test;

import cn.schoolwow.quickdao.domain.generator.IDGenerator;
import cn.schoolwow.quickdao.domain.generator.SnowflakeIdGenerator;
import cn.schoolwow.quickdao.mariadb.MariaDBTest;
import cn.schoolwow.quickdao.mariadb.entity.Product;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

/**大数据量测试*/
public class LargeRecordTest extends MariaDBTest {

    @Test
    public void insertBatch(){
        dao.rebuild(Product.class);
        Product[] products = new Product[1000];
        for(int i=0;i<products.length;i++){
            Product product = new Product();
            product.setName("电冰箱");
            product.setType("电器");
            product.setPrice(1000);
            product.setPersonId(1);
            products[i] = product;
        }
        dao.insertBatch(products);
        long count = dao.query(Product.class).execute().count();
        Assert.assertEquals(products.length,count);
    }

    @Test
    public void insertBatchArray(){
        dao.rebuild(Product.class);
        JSONArray array = new JSONArray();
        IDGenerator idGenerator = new SnowflakeIdGenerator();
        for(int i=0;i<1000;i++){
            JSONObject o = new JSONObject();
            o.put("id", idGenerator.getNextId());
            o.put("name", "电冰箱");
            o.put("type", "电器");
            o.put("price", 1000);
            o.put("person_id", 1);
            o.put("publish_time", new Date());
            array.add(o);
        }
        dao.query("product")
                .addInsert(array)
                .execute()
                .insertBatch();
        long count = dao.query(Product.class).execute().count();
        Assert.assertEquals(array.size(),count);
    }
}
