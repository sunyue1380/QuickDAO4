package cn.schoolwow.quickdao.builder.dcl;

import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import cn.schoolwow.quickdao.domain.dcl.DataBaseUser;
import cn.schoolwow.quickdao.domain.dcl.GrantOption;

public class PostgreDCLBuilder extends AbstractDCLBuilder{

    public PostgreDCLBuilder(QuickDAOConfig quickDAOConfig) {
        super(quickDAOConfig);
    }

    @Override
    public String getUserNameList() {
        return "select usename from pg_user;";
    }

    @Override
    public String createUser(DataBaseUser dataBaseUser) {
        return "create user " + dataBaseUser.username + " with password '" + dataBaseUser.password + "';";
    }

    @Override
    public String modifyPassword(String username, String newPassword) {
        return "alter user " + username + " with password '" + newPassword + "';";
    }

    @Override
    public String deleteUser(DataBaseUser dataBaseUser) {
        return "drop role " + dataBaseUser.username + ";";
    }

    @Override
    public String grant(GrantOption grantOption) {
        return "grant " + grantOption.privileges + " on database " + grantOption.databaseName + " to " + grantOption.dataBaseUser.username + ";";
    }

    @Override
    public String createUserAndGrant(GrantOption grantOption) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String revoke(GrantOption grantOption) {
        return "revoke " + grantOption.privileges + " on database " + grantOption.databaseName + " from " + grantOption.dataBaseUser.username + ";";
    }

    @Override
    public String flushPrivileges() {
        throw new UnsupportedOperationException();
    }
}