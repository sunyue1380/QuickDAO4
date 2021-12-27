# DCL

> Currently DCL only support MySQL,MariaDB and Postgre.

Define User infomation.

## DatabaseUser

```java
DataBaseUser dataBaseUser = new DataBaseUser();
dataBaseUser.username = "quickdao";//username
dataBaseUser.password = "123456";//password
dataBaseUser.host = "localhost";//host,default value is '%'
```

Define Grant Infomation.

## GrantOption

```java
GrantOption grantOption = new GrantOption();
grantOption.databaseName = "quickdao";//database name
grantOption.privileges = "all privileges";//privilege type
grantOption.dataBaseUser = dataBaseUser;//grant user
```

# DCL Operation

## getUserNameList

```java
List<String> userNameList = dao.getUserNameList();
System.out.println(userNameList);
```

## createUser

```java
dao.createUser(dataBaseUser);
```

## modifyPassword

```java
dao.modifyPassword("quickdao","654321");
```

## grant

```java
dao.grant(grantOption);
```

## revoke

```java
dao.revoke(grantOption);
```

## deleteUser

```java
dao.deleteUser(dataBaseUser);
```

## createUserAndGrant

```java
dao.createUserAndGrant(grantOption);
```