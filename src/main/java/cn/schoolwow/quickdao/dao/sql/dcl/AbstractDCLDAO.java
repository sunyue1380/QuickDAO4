package cn.schoolwow.quickdao.dao.sql.dcl;

import cn.schoolwow.quickdao.builder.dcl.AbstractDCLBuilder;
import cn.schoolwow.quickdao.dao.sql.AbstractSQLDAO;
import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import cn.schoolwow.quickdao.domain.dcl.DataBaseUser;
import cn.schoolwow.quickdao.domain.dcl.GrantOption;
import cn.schoolwow.quickdao.exception.SQLRuntimeException;

import java.sql.SQLException;
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
        try {
            return dclBuilder.getUserNameList();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void createUser(DataBaseUser dataBaseUser) {
        try {
            dclBuilder.createUser(dataBaseUser);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void modifyPassword(String username, String newPassword) {
        try {
            dclBuilder.modifyPassword(username, newPassword);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void deleteUser(DataBaseUser dataBaseUser) {
        try {
            dclBuilder.deleteUser(dataBaseUser);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void grant(GrantOption grantOption) {
        try {
            dclBuilder.grant(grantOption);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void createUserAndGrant(GrantOption grantOption) {
        try {
            dclBuilder.createUserAndGrant(grantOption);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    @Override
    public void revoke(GrantOption grantOption) {
        try {
            dclBuilder.revoke(grantOption);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }
}
