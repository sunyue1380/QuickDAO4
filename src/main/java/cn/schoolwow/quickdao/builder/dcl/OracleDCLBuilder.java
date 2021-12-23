package cn.schoolwow.quickdao.builder.dcl;

import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import cn.schoolwow.quickdao.domain.dcl.DataBaseUser;
import cn.schoolwow.quickdao.domain.dcl.GrantOption;

public class OracleDCLBuilder extends AbstractDCLBuilder{

    public OracleDCLBuilder(QuickDAOConfig quickDAOConfig) {
        super(quickDAOConfig);
    }

    @Override
    public String getUserNameList(){
        return "select username from all_users";
    }

    @Override
    public String createUser(DataBaseUser dataBaseUser) {
        return "create user " + dataBaseUser.username + " identified by " + dataBaseUser.password;
    }

    @Override
    public String modifyPassword(String username, String newPassword) {
        return "alter user " + username + " identified by " + newPassword;
    }

    @Override
    public String deleteUser(DataBaseUser dataBaseUser) {
        return "drop user " + dataBaseUser.username;
    }

    @Override
    public String grant(GrantOption grantOption) {
        return "grant " + grantOption.privileges + " to " + grantOption.dataBaseUser.username;
    }

    @Override
    public String createUserAndGrant(GrantOption grantOption) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String revoke(GrantOption grantOption) {
        return "revoke " + grantOption.privileges + " from " + grantOption.dataBaseUser.username;
    }

    @Override
    public String flushPrivileges() {
        return "alter profile default limit password_life_time unlimited";
    }
}