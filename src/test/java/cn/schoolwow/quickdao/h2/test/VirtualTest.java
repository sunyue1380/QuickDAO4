package cn.schoolwow.quickdao.h2.test;

import cn.schoolwow.quickdao.domain.PageVo;
import cn.schoolwow.quickdao.h2.H2Test;
import cn.schoolwow.quickdao.h2.entity.Person;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.junit.Assert;
import org.junit.Test;

/**虚表查询测试*/
public class VirtualTest extends H2Test {

    public VirtualTest(){
        initializeProduct();
    }

    @Test
    public void testDML(){
        {
            int effect = dao.query("PRODUCT")
                    .addInsert("id",System.nanoTime())
                    .addInsert("name","洗衣机")
                    .addInsert("type","家电")
                    .addInsert("price",1600)
                    .addInsert("person_id",1)
                    .execute()
                    .insert();
            Assert.assertEquals(1,effect);
        }
        {
            JSONObject product = new JSONObject();
            product.put("ID",System.nanoTime());
            product.put("NAME","双肩背包");
            product.put("TYPE","箱包");
            product.put("PRICE",100);
            product.put("PERSON_ID",1);
            int effect = dao.query("PRODUCT")
                    .addInsert(product)
                    .execute()
                    .insert();
            Assert.assertEquals(1,effect);
            JSONObject result = dao.fetch("PRODUCT","PRICE",100);
            Assert.assertEquals("双肩背包",result.getString("NAME"));
            Assert.assertEquals("箱包",result.getString("TYPE"));
            Assert.assertEquals(1,result.getIntValue("PERSON_ID"));
        }
        dao.rebuild(Person.class);
        //插入JSONObject
        {
            JSONObject person = new JSONObject();
            person.put("PASSWORD","123456");
            person.put("LAST_NAME","托马斯");
            int effect = dao.query("PERSON")
                    .addInsert(person)
                    .execute()
                    .insert();
            Assert.assertEquals(1,effect);
            Assert.assertTrue(person.containsKey("generatedKeys"));
            Assert.assertTrue(person.getIntValue("generatedKeys")>0);
        }
        //批量插入JSONArray
        {
            JSONArray array = new JSONArray(10);
            for(int i=0;i<10;i++){
                JSONObject person = new JSONObject();
                person.put("PASSWORD","123456");
                person.put("LAST_NAME","托尼"+i);
                array.add(person);
            }
            int effect = dao.query("PERSON")
                    .addInsert(array)
                    .execute()
                    .insert();
            Assert.assertEquals(10,effect);
            for(int i=0;i<array.size();i++){
                JSONObject person = array.getJSONObject(i);
                Assert.assertTrue(person.containsKey("generatedKeys"));
                Assert.assertTrue(person.getIntValue("generatedKeys")>0);
            }
        }
        {
            int effect = dao.query("PRODUCT")
                    .addQuery("name","洗衣机")
                    .addQuery("type","家电")
                    .addUpdate("price",2000)
                    .execute()
                    .update();
            Assert.assertEquals(1,effect);
        }
    }

    @Test
    public void testDQL(){
        {
            int price = (int) dao.query("PRODUCT")
                    .addQuery("name","冰箱")
                    .addColumn("price")
                    .execute()
                    .getSingleColumn(Integer.class);
            Assert.assertEquals(600,price);
        }
        {
            JSONObject result = dao.query("PRODUCT")
                    .addQuery("type","电器")
                    .execute()
                    .getObject();
            Assert.assertEquals("电器",result.getString("TYPE"));
        }
        {
            PageVo<JSONObject> pageVo = dao.query("PRODUCT")
                    .addQuery("type","电器")
                    .page(1,2)
                    .execute()
                    .getPagingList();
            Assert.assertEquals(2,pageVo.getPageSize());
            Assert.assertEquals(1,pageVo.getCurrentPage());
            Assert.assertEquals(2,pageVo.getTotalPage());
            Assert.assertEquals(2,pageVo.getList().size());
        }
    }

    @Test
    public void testFetch(){
        JSONObject product = dao.fetch("PRODUCT","type","数码");
        Assert.assertEquals(1000,product.getIntValue("PRICE"));

        JSONArray productArray = dao.fetchList("PRODUCT","type","电器");
        Assert.assertEquals(3,productArray.size());
        Assert.assertEquals("电器",productArray.getJSONObject(0).getString("TYPE"));
    }

    @Test
    public void testDelete(){
        int effect = dao.delete("PRODUCT","type","数码");
        Assert.assertEquals(1,effect);
    }
}