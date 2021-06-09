package cn.schoolwow.quickdao.builder.dcl;

import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import cn.schoolwow.quickdao.domain.dcl.DataBaseUser;
import cn.schoolwow.quickdao.domain.dcl.GrantOption;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MySQLDCLBuilder extends AbstractDCLBuilder{

    public MySQLDCLBuilder(QuickDAOConfig quickDAOConfig) {
        super(quickDAOConfig);
    }

    @Override
    public List<String> getUserNameList() throws SQLException {
        String sql = "select distinct user from mysql.user;";
        ResultSet resultSet = connectionExecutor.executeQuery("获取用户列表",sql);
        List<String> userNameList = new ArrayList<>();
        while(resultSet.next()){
            userNameList.add(resultSet.getString(1));
        }
        resultSet.close();
        return userNameList;
    }

    @Override
    public void createUser(DataBaseUser dataBaseUser) throws SQLException {
        String sql = "create user '" + dataBaseUser.username + "'@'" + dataBaseUser.host + "' identified by '" + dataBaseUser.password + "';";
        connectionExecutor.executeUpdate("创建用户",sql);
    }

    @Override
    public void modifyPassword(String username, String newPassword) throws SQLException {
        String sql = "set password for " + username + " = password('" + newPassword + "');";
        connectionExecutor.executeUpdate("更改密码",sql);
    }

    @Override
    public void deleteUser(DataBaseUser dataBaseUser) throws SQLException {
        String sql = "drop user " + dataBaseUser.username + "@'" + dataBaseUser.host + "';";
        connectionExecutor.executeUpdate("删除用户",sql);
    }

    @Override
    public void grant(GrantOption grantOption) throws SQLException {
        String sql = "grant " + grantOption.privileges + " on " + grantOption.databaseName + ".* to '" + grantOption.dataBaseUser.username + "'@'" + grantOption.dataBaseUser.host + "';";
        connectionExecutor.executeUpdate("数据库授权",sql);
        connectionExecutor.executeUpdate("刷新权限","flush privileges;");
    }

    @Override
    public void createUserAndGrant(GrantOption grantOption) throws SQLException {
        String sql = "grant " + grantOption.privileges + " on " + grantOption.databaseName + ".* to '" + grantOption.dataBaseUser.username + "'@'" + grantOption.dataBaseUser.host + "' identified by '" + grantOption.dataBaseUser.password + "';";
        connectionExecutor.executeUpdate("创建用户并授权",sql);
        connectionExecutor.executeUpdate("刷新权限","flush privileges;");
    }

    @Override
    public void revoke(GrantOption grantOption) throws SQLException {
        String sql = "revoke " + grantOption.privileges + " on " + grantOption.databaseName + ".* from '" + grantOption.dataBaseUser.username + "'@'" + grantOption.dataBaseUser.host + "';";
        connectionExecutor.executeUpdate("收回权限",sql);
        connectionExecutor.executeUpdate("刷新权限","flush privileges;");
    }
}
