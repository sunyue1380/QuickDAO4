package cn.schoolwow.quickdao.dao.sql.dcl;

import cn.schoolwow.quickdao.builder.dcl.AbstractDCLBuilder;
import cn.schoolwow.quickdao.dao.sql.AbstractSQLDAO;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import cn.schoolwow.quickdao.domain.dcl.DataBaseUser;
import cn.schoolwow.quickdao.domain.dcl.GrantOption;
import cn.schoolwow.quickdao.exception.SQLRuntimeException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AbstractDCLDAO extends AbstractSQLDAO implements DCLDAO{
    private AbstractDCLBuilder dclBuilder;
    
    public AbstractDCLDAO(QuickDAOConfig quickDAOConfig) {
        super(quickDAOConfig);
        this.dclBuilder = quickDAOConfig.database.getDCLBuilderInstance(quickDAOConfig);
        super.sqlBuilder = this.dclBuilder;
    }

    @Override
    public List<String> getUserNameList() {
        String getUserNameListSQL = dclBuilder.getUserNameList();
        try {
            ResultSet resultSet = dclBuilder.connectionExecutor.executeQuery("获取用户列表",getUserNameListSQL);
            List<String> userNameList = new ArrayList<>();
            while(resultSet.next()){
                userNameList.add(resultSet.getString(1));
            }
            resultSet.close();
            return userNameList;
        }catch (SQLException e){
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void createUser(DataBaseUser dataBaseUser) {
        String createUserSQL = dclBuilder.createUser(dataBaseUser);
        try {
            dclBuilder.connectionExecutor.executeUpdate("创建用户",createUserSQL);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void modifyPassword(String username, String newPassword) {
        String modifyPasswordSQL = dclBuilder.modifyPassword(username, newPassword);
        try {
            dclBuilder.connectionExecutor.executeUpdate("更改密码",modifyPasswordSQL);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void deleteUser(DataBaseUser dataBaseUser) {
        String deleteUserSQL = dclBuilder.deleteUser(dataBaseUser);
        try {
            dclBuilder.connectionExecutor.executeUpdate("删除用户",deleteUserSQL);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void grant(GrantOption grantOption) {
        String grantSQL = dclBuilder.grant(grantOption);
        try {
            dclBuilder.connectionExecutor.executeUpdate("数据库授权",grantSQL);
            flushPrivileges();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void createUserAndGrant(GrantOption grantOption) {
        String createUserAndGrantSQL = dclBuilder.createUserAndGrant(grantOption);
        try {
            dclBuilder.connectionExecutor.executeUpdate("创建用户并授权",createUserAndGrantSQL);
            flushPrivileges();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void revoke(GrantOption grantOption) {
        String revokeSQL = dclBuilder.revoke(grantOption);
        try {
            dclBuilder.connectionExecutor.executeUpdate("收回权限",revokeSQL);
            flushPrivileges();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void flushPrivileges() {
        String flushPrivilegesSQL = dclBuilder.flushPrivileges();
        try {
            dclBuilder.connectionExecutor.executeUpdate("刷新权限",flushPrivilegesSQL);
        }catch (SQLException e){
            throw new SQLRuntimeException(e);
        }
    }
}