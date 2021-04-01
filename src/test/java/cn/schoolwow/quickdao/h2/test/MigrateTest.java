package cn.schoolwow.quickdao.h2.test;

import cn.schoolwow.quickdao.DAOUtil;
import cn.schoolwow.quickdao.dao.DAO;
import cn.schoolwow.quickdao.h2.H2Test;
import cn.schoolwow.quickdao.sqlite.entity.Order;
import cn.schoolwow.quickdao.sqlite.entity.Person;
import cn.schoolwow.quickdao.sqlite.entity.Product;
import org.junit.Test;

public class MigrateTest extends H2Test {
    public MigrateTest(){
        initializeProduct();
        initializePersonAndOrder();
    }

    @Test
    public void migrateToMySQL(){
        DAO mySQLDAO = DAOUtil.getMySQLDAO();
        dao.migrateTo(mySQLDAO, Product.class, Person.class, Order.class);
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
