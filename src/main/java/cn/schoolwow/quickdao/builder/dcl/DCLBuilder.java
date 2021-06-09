package cn.schoolwow.quickdao.builder.dcl;

import cn.schoolwow.quickdao.domain.dcl.DataBaseUser;
import cn.schoolwow.quickdao.domain.dcl.GrantOption;

import java.sql.SQLException;
import java.util.List;

/**
 * 负责数据库权限相关
 */
public interface DCLBuilder {
    /**
     * 获取用户列表
     */
    List<String> getUserNameList() throws SQLException;

    /**
     * 创建用户
     * @param dataBaseUser 数据库用户
     */
    void createUser(DataBaseUser dataBaseUser) throws SQLException;

    /**
     * 修改用户密码
     * @param username 用户名
     * @param newPassword 新密码
     */
    void modifyPassword(String username, String newPassword) throws SQLException;

    /**
     * 删除用户
     * @param dataBaseUser 数据库用户
     */
    void deleteUser(DataBaseUser dataBaseUser) throws SQLException;

    /**
     * 授予权限
     * @param grantOption 授权信息
     */
    void grant(GrantOption grantOption) throws SQLException;

    /**
     * 创建用户并授予权限
     * @param grantOption 授权信息
     */
    void createUserAndGrant(GrantOption grantOption) throws SQLException;

    /**
     * 收回权限
     * @param grantOption 授权信息
     */
    void revoke(GrantOption grantOption) throws SQLException;
}
