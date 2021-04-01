package cn.schoolwow.quickdao.postgre.test;

import cn.schoolwow.quickdao.DAOUtil;
import cn.schoolwow.quickdao.dao.DAO;
import cn.schoolwow.quickdao.postgre.PostgreLTest;
import cn.schoolwow.quickdao.sqlite.entity.Order;
import cn.schoolwow.quickdao.sqlite.entity.Person;
import cn.schoolwow.quickdao.sqlite.entity.Product;
import org.junit.Test;

public class MigrateTest extends PostgreLTest {
    public MigrateTest(){
        initializeProduct();
        initializePersonAndOrder();
    }

    @Test
    public void migrateToMySQL(){
        DAO mysqlDAO = DAOUtil.getMySQLDAO();
        dao.migrateTo(mysqlDAO, Product.class, Person.class, Order.class);
    }
}
