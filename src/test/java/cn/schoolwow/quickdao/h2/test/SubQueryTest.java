package cn.schoolwow.quickdao.h2.test;

import cn.schoolwow.quickdao.h2.H2Test;
import cn.schoolwow.quickdao.h2.entity.Product;
import cn.schoolwow.quickdao.query.condition.Condition;
import com.alibaba.fastjson.JSONArray;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**子查询测试*/
public class SubQueryTest extends H2Test {

    public SubQueryTest(){
        initializeProduct();
    }

    @Test
    public void testExistSubQuery(){
        List<String> productNameList = dao.query(Product.class)
                .addExistSubQuery(
                        dao.query(Product.class)
                                .addQuery("price",">=",5000)
                                .addColumn("id")
                )
                .addColumn("name")
                .execute()
                .getSingleColumnList(String.class);
        Assert.assertEquals(0,productNameList.size());
    }

    @Test
    public void testSelectSubQuery(){
        Condition selectCondition = dao.query(Product.class)
                .addQuery("type","电器")
                .addQuery("price",4000)
                .addColumn("name");
        List<String> productNameList = dao.query(Product.class)
                .addColumn(selectCondition,"nameAlias")
                .execute()
                .getSingleColumnList(String.class);
        Assert.assertEquals(4,productNameList.size());
    }

    @Test
    public void testFromSubQuery(){
        Condition<Product> fromCondition = dao.query(Product.class)
                .addQuery("type","电器")
                .groupBy("type")
                .addColumn("type")
                .addColumn("avg(price) avgPrice")
                .limit(0,100);
        JSONArray array = dao.query(fromCondition)
                .addQuery("avgPrice",">=",2000)
                .addColumn("type","avgPrice")
                .execute()
                .getArray();
        Assert.assertEquals(1,array.size());
    }

    @Test
    public void testHavingSubQuery(){
        Condition havingCondition = dao.query("dual")
                .addColumn("1");
        long count = (long) dao.query(Product.class)
                .groupBy("type")
                .having("count(type)",">",havingCondition)
                .addColumn("count(type) count")
                .execute()
                .getSingleColumn(Long.class);
        Assert.assertEquals(3,count);
    }

    @Test
    public void testWhereSubQuery(){
        Condition whereCondition = dao.query(Product.class).addColumn("avg(price)");
        List<Product> productList = dao.query(Product.class)
                .addSubQuery("price","<",whereCondition)
                .execute()
                .getList();
        Assert.assertEquals(2,productList.size());
    }
}
