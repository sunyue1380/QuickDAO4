package cn.schoolwow.quickdao;

import cn.schoolwow.quickdao.dao.DAO;
import cn.schoolwow.quickdao.domain.dcl.DataBaseUser;
import cn.schoolwow.quickdao.domain.dcl.GrantOption;
import com.zaxxer.hikari.HikariDataSource;

public class DatabaseTest {
    protected static HikariDataSource dataSource;
    protected static DAO dao;

    protected DataBaseUser dataBaseUser = new DataBaseUser();
    protected GrantOption grantOption = new GrantOption();
    {
        dataBaseUser.username = "quickdao";
        dataBaseUser.password = "123456";

        grantOption.dataBaseUser = dataBaseUser;
        grantOption.privileges = "all privileges";
        grantOption.databaseName = "quickdao";
    }
}
