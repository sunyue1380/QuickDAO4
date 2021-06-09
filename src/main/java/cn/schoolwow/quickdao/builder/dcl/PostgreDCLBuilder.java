package cn.schoolwow.quickdao.builder.dcl;

import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import cn.schoolwow.quickdao.domain.dcl.DataBaseUser;
import cn.schoolwow.quickdao.domain.dcl.GrantOption;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PostgreDCLBuilder extends AbstractDCLBuilder{

    public PostgreDCLBuilder(QuickDAOConfig quickDAOConfig) {
        super(quickDAOConfig);
    }

    @Override
    public List<String> getUserNameList() throws SQLException {
        String sql = "select usename from pg_user;";
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
        String sql = "create user " + dataBaseUser.username + " with password '" + dataBaseUser.password + "';";
        connectionExecutor.executeUpdate("创建用户",sql);
    }

    @Override
    public void modifyPassword(String username, String newPassword) throws SQLException {
        String sql = "alter user " + username + " with password '" + newPassword + "';";
        connectionExecutor.executeUpdate("修改密码",sql);
    }

    @Override
    public void deleteUser(DataBaseUser dataBaseUser) throws SQLException {
        String sql = "drop role " + dataBaseUser.username + ";";
        connectionExecutor.executeUpdate("删除用户",sql);
    }

    @Override
    public void grant(GrantOption grantOption) throws SQLException {
        String sql = "grant " + grantOption.privileges + " on database " + grantOption.databaseName + " to " + grantOption.dataBaseUser.username + ";";
        connectionExecutor.executeUpdate("数据库授权",sql);
    }

    @Override
    public void createUserAndGrant(GrantOption grantOption) throws SQLException {
        createUser(grantOption.dataBaseUser);
        grant(grantOption);
    }

    @Override
    public void revoke(GrantOption grantOption) throws SQLException {
        String sql = "revoke " + grantOption.privileges + " on database " + grantOption.databaseName + " from " + grantOption.dataBaseUser.username + ";";
        connectionExecutor.executeUpdate("收回权限",sql);
    }
}
