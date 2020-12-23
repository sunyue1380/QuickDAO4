package cn.schoolwow.quickdao.mysql;

import cn.schoolwow.quickdao.DatabaseTest;
import cn.schoolwow.quickdao.QuickDAO;
import cn.schoolwow.quickdao.mysql.entity.Product;
import cn.schoolwow.quickdao.transaction.Transaction;
import com.zaxxer.hikari.HikariDataSource;

import java.util.Date;

public class MySQLTest extends DatabaseTest {
    static{
        dataSource = new HikariDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setJdbcUrl("jdbc:mysql://127.0.0.1:3306/quickdao");
        dataSource.setUsername("root");
        dataSource.setPassword("123456");

        dao = QuickDAO.newInstance().dataSource(dataSource)
                .packageName("cn.schoolwow.quickdao.mysql.entity")
                .charset("utf8")
                .engine("InnoDB")
                .build();
    }

    /**初始化数据*/
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
}
