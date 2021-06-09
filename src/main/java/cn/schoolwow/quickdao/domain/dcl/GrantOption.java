package cn.schoolwow.quickdao.domain.dcl;

/**
 * 数据库授权选项
 * */
public class GrantOption {
    /**授权数据库名称*/
    public String databaseName;

    /**授予权限,默认全部权限,若有多个权限用逗号隔开*/
    public String privileges = "all privileges";

    /**授予用户*/
    public DataBaseUser dataBaseUser;
}
