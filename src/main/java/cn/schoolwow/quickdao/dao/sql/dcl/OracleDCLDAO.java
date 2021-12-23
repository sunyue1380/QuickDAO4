package cn.schoolwow.quickdao.dao.sql.dcl;

import cn.schoolwow.quickdao.domain.QuickDAOConfig;
import cn.schoolwow.quickdao.domain.dcl.GrantOption;

public class OracleDCLDAO extends AbstractDCLDAO{

    public OracleDCLDAO(QuickDAOConfig quickDAOConfig) {
        super(quickDAOConfig);
    }

    @Override
    public void createUserAndGrant(GrantOption grantOption){
        createUser(grantOption.dataBaseUser);
        grant(grantOption);
    }
}