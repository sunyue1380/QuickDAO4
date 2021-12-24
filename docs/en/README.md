# QuickDAO4

A simple, Quick, Powerful Java ORM Framework. 

Advantage:

* Cover all db operation by only one dao object
* Automatically creating tables and fields
* Support foreign key query
* Entity annotation allows you specify database index, constraint, column type, column name and so on

# Support database

* MySQL(5.0 above)
* MariaDB
* SQLite
* H2
* Postgre(9.0.0 above)
* SQL Server(2012 above)
* Oracle(11g above)(supported from 4.1.7)

# Quick start

## 1 Import QuickDAO

To use QuickDAO, you must choose a DataSource implement(eg commons-dbcp)

```xml
<dependency>
   <groupId>commons-dbcp</groupId>
   <artifactId>commons-dbcp</artifactId>
   <version>1.4</version>
</dependency>
<dependency>
  <groupId>cn.schoolwow</groupId>
  <artifactId>QuickDAO</artifactId>
  <version>{latestVersion}</version>
</dependency>
```

> [Query QuickDAO latest Version](https://search.maven.org/search?q=a:QuickDAO)

## 2 QuickDAO Configuration
```java
BasicDataSource mysqlDataSource = new BasicDataSource();
mysqlDataSource.setDriverClassName("com.mysql.jdbc.Driver");
mysqlDataSource.setUrl("jdbc:mysql://127.0.0.1:3306/quickdao");
mysqlDataSource.setUsername("root");
mysqlDataSource.setPassword("123456");
cn.schoolwow.quickdao.dao.DAO dao = QuickDAO.newInstance()
                    .dataSource(mysqlDataSource)
                    //specify entity package name
                    .packageName("cn.schoolwow.quickdao.entity")
                    .build();
//then you can use this dao object for any database operation
```

## 3 use QuickDAO

```java
//select by id
User user1 = dao.fetch(User.class,1);
//select by specific column
User user2 = dao.fetch(User.class,"username","quickdao");
//insert user entity
dao.insert(user);
//update user entity (by unique constraint or by id)
dao.update(user);
//save user entity(if exists then update else insert)
dao.save(user);
//delete by id
dao.delete(User.class,1);
//delete by specific column
dao.delete(User.class,"username","quickdao");

//complex query
List<User> userList = dao.query(User.class)
    .addQuery("name","quickdao")
    .addNotNullQuery("password")
    .page(1,10)
    .orderBy("id")
    .execute()
    .getList();

//foreign key query
List<User> userList2 = dao.query(User.class)
    .joinTable(Address.class,"addressId","id")
    .addQuery("name","BeiJing")
    .done()
    .addQuery("name","quickdao")
    .page(1,10)
    .orderBy("id")
    .compositField()
    .execute()
    .getList();

//more use cases please refer to document
```

# Feedback

if you have any suggestions please Pull Request or mailto 648823596@qq.com.

# LICENSE

[LGPL](http://www.gnu.org/licenses/lgpl-3.0-standalone.html)