# DCL操作

QuickDAO支持DCL相关操作.

> 目前DCL操作仅支持MySQL,MariaDB和Postgre,其他数据库类型暂不支持

DCL操作涉及以下两个实体类,DatabaseUser定义了用户信息,GrantOption定义授权信息

## DatabaseUser类

```java
DataBaseUser dataBaseUser = new DataBaseUser();
dataBaseUser.username = "quickdao";//指定用户名
dataBaseUser.password = "123456";//指定密码
dataBaseUser.host = "localhost";//指定访问主机,默认为'%'
```

## GrantOption类

```java
GrantOption grantOption = new GrantOption();
grantOption.databaseName = "quickdao";//指定数据库名称
grantOption.privileges = "all privileges";//指定授予权限类型,默认所有权限
grantOption.dataBaseUser = dataBaseUser;//指定授予用户
```

# DCL相关操作

## 获取数据库用户名列表

```java
List<String> userNameList = dao.getUserNameList();
System.out.println(userNameList);
```

## 创建用户

```java
dao.createUser(dataBaseUser);
```

## 修改用户密码

```java
dao.modifyPassword("quickdao","654321");
```

## 数据库授权

```java
dao.grant(grantOption);
```

## 收回数据库授权

```java
dao.revoke(grantOption);
```

## 删除用户

```java
dao.deleteUser(dataBaseUser);
```

## 直接创建用户并授权

```java
dao.createUserAndGrant(grantOption);
```