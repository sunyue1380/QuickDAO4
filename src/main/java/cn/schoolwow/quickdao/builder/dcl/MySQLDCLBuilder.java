package cn.schoolwow.quickdao.builder.dcl;

import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import cn.schoolwow.quickdao.domain.dcl.DataBaseUser;
import cn.schoolwow.quickdao.domain.dcl.GrantOption;

public class MySQLDCLBuilder extends AbstractDCLBuilder{

    public MySQLDCLBuilder(QuickDAOConfig quickDAOConfig) {
        super(quickDAOConfig);
    }

    @Override
    public String getUserNameList() {
        return "select distinct user from mysql.user;";
    }

    @Override
    public String createUser(DataBaseUser dataBaseUser) {
        return "create user '" + dataBaseUser.username + "'@'" + dataBaseUser.host + "' identified by '" + dataBaseUser.password + "';";
    }

    @Override
    public String modifyPassword(String username, String newPassword) {
        return "set password for " + username + " = password('" + newPassword + "');";
    }

    @Override
    public String deleteUser(DataBaseUser dataBaseUser) {
        return "drop user " + dataBaseUser.username + "@'" + dataBaseUser.host + "';";
    }

    @Override
    public String grant(GrantOption grantOption) {
        return "grant " + grantOption.privileges + " on " + grantOption.databaseName + ".* to '" + grantOption.dataBaseUser.username + "'@'" + grantOption.dataBaseUser.host + "';";
    }

    @Override
    public String createUserAndGrant(GrantOption grantOption) {
        return "grant " + grantOption.privileges + " on " + grantOption.databaseName + ".* to '" + grantOption.dataBaseUser.username + "'@'" + grantOption.dataBaseUser.host + "' identified by '" + grantOption.dataBaseUser.password + "';";
        
    }

    @Override
    public String revoke(GrantOption grantOption) {
        return "revoke " + grantOption.privileges + " on " + grantOption.databaseName + ".* from '" + grantOption.dataBaseUser.username + "'@'" + grantOption.dataBaseUser.host + "';";
        
    }

    @Override
    public String flushPrivileges() {
        return "flush privileges;";
    }
}