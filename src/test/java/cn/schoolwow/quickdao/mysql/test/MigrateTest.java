package cn.schoolwow.quickdao.mysql.test;

import cn.schoolwow.quickdao.DAOUtil;
import cn.schoolwow.quickdao.dao.DAO;
import cn.schoolwow.quickdao.mysql.MySQLTest;
import cn.schoolwow.quickdao.sqlite.entity.Order;
import cn.schoolwow.quickdao.sqlite.entity.Person;
import cn.schoolwow.quickdao.sqlite.entity.Product;
import org.junit.Test;

public class MigrateTest extends MySQLTest {
    public MigrateTest(){
        initializeProduct();
        initializePersonAndOrder();
    }

    @Test
    public void migrateToH2(){
        DAO h2DAO = DAOUtil.getH2DAO();
        dao.migrateTo(h2DAO, Product.class, Person.class, Order.class);
    }

    @Test
    public void migrateToPostgre(){
        DAO postgreDAO = DAOUtil.getPostgreDAO();
        dao.migrateTo(postgreDAO, Product.class, Person.class, Order.class);
    }

    @Test
    public void migrateToSQLite(){
        DAO sqLiteDAO = DAOUtil.getSQLiteDAO();
        dao.migrateTo(sqLiteDAO, Product.class, Person.class, Order.class);
    }
}
