package cn.schoolwow.quickdao.oracle.test;

import cn.schoolwow.quickdao.oracle.OracleTest;
import org.junit.Test;

import java.util.List;

/**
 * DCL操作测试
 * */
public class DCLTest extends OracleTest {
    @Test
    public void dcl(){
        List<String> userNameList = dao.getUserNameList();
        System.out.println(userNameList);
        dataBaseUser.username = "quickdao_user";
        if(userNameList.contains(dataBaseUser.username.toUpperCase())){
            dao.deleteUser(dataBaseUser);
        }
        dao.createUser(dataBaseUser);
        dao.modifyPassword(dataBaseUser.username,"654321");

        //授权
        dao.grant(grantOption);
        dao.revoke(grantOption);
        dao.deleteUser(dataBaseUser);

        //直接创建用户并授权
        dao.createUserAndGrant(grantOption);
        dao.revoke(grantOption);
        dao.deleteUser(dataBaseUser);
    }
}
