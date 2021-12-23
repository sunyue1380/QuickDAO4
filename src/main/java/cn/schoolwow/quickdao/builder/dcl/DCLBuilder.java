package cn.schoolwow.quickdao.builder.dcl;

import cn.schoolwow.quickdao.domain.dcl.DataBaseUser;
import cn.schoolwow.quickdao.domain.dcl.GrantOption;

/**
 * 负责数据库权限相关
 */
public interface DCLBuilder {
    /**
     * 获取用户列表
     */
    String getUserNameList();

    /**
     * 创建用户
     * @param dataBaseUser 数据库用户
     */
    String createUser(DataBaseUser dataBaseUser);

    /**
     * 修改用户密码
     * @param username 用户名
     * @param newPassword 新密码
     */
    String modifyPassword(String username, String newPassword);

    /**
     * 删除用户
     * @param dataBaseUser 数据库用户
     */
    String deleteUser(DataBaseUser dataBaseUser);

    /**
     * 授予权限
     * @param grantOption 授权信息
     */
    String grant(GrantOption grantOption);

    /**
     * 创建用户并授予权限
     * @param grantOption 授权信息
     */
    String createUserAndGrant(GrantOption grantOption);

    /**
     * 收回权限
     * @param grantOption 授权信息
     */
    String revoke(GrantOption grantOption);
    
    /**
     * 刷新权限信息
     * @param grantOption 授权信息
     */
    String flushPrivileges();
}
