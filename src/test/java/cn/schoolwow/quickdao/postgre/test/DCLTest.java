package cn.schoolwow.quickdao.postgre.test;

import cn.schoolwow.quickdao.postgre.PostgreLTest;
import org.junit.Test;

import java.util.List;

/**
 * DCL操作测试
 * */
public class DCLTest extends PostgreLTest {
    @Test
    public void dcl(){
        List<String> userNameList = dao.getUserNameList();
        if(userNameList.contains("quickdao")){
            dao.deleteUser(dataBaseUser);
        }
        dao.createUser(dataBaseUser);
        dao.modifyPassword("quickdao","654321");

        //授权
        dao.grant(grantOption);
        dao.revoke(grantOption);
        dao.revoke(grantOption);
        dao.deleteUser(dataBaseUser);

        //直接创建用户并授权
        dao.createUserAndGrant(grantOption);
        dao.revoke(grantOption);
        dao.deleteUser(dataBaseUser);
    }
}
